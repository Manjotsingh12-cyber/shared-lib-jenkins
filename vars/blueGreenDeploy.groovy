// Usage: blueGreenDeploy()
// blue -> :8001, green -> :8002, nginx proxy "bg-proxy" serves live traffic on :8080
def call(Map config = [:]) {
    String image    = config.image ?: "${env.IMAGE_NAME}:${env.IMAGE_TAG}"
    int bluePort    = (config.bluePort ?: 8001) as int
    int greenPort   = (config.greenPort ?: 8002) as int
    int livePort    = (config.livePort ?: 8080) as int
    String envVar   = config.envVar ?: 'ENVIRONMENT'   // env var your app.py reads for "environment"
    String stateDir = config.stateDir ?: (sh(script: 'echo $HOME', returnStdout: true).trim() + '/bluegreen')

    Map ports = [blue: bluePort, green: greenPort]

    sh "mkdir -p ${stateDir}/conf.d"
    String active = sh(script: "cat ${stateDir}/active 2>/dev/null || echo none", returnStdout: true).trim()
    String idle = (active == 'blue') ? 'green' : 'blue'
    int idlePort = ports[idle]

    echo "Live color: ${active} | Deploying ${image} to: ${idle} (port ${idlePort})"

    // 1. start new version on the idle color
    sh "docker rm -f app-${idle} || true"
    sh "docker run -d --name app-${idle} --restart unless-stopped -p ${idlePort}:8000 -e ${envVar}=${idle} ${image}"

    // 2. wait until it is healthy
    boolean healthy = false
    for (int i = 0; i < 15 && !healthy; i++) {
        sleep time: 2, unit: 'SECONDS'
        healthy = (sh(script: "curl -sf http://localhost:${idlePort}/health", returnStatus: true) == 0)
    }
    if (!healthy) {
        sh "docker logs app-${idle} || true"
        sh "docker rm -f app-${idle} || true"
        error "Deploy aborted: app-${idle} failed health check. Live traffic untouched."
    }

    // 3. point nginx at the new color
    writeFile file: "${stateDir}/conf.d/default.conf", text: """server {
    listen 80;
    location / {
        proxy_pass http://host.docker.internal:${idlePort};
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
    }
}
"""
    boolean proxyExists = (sh(script: 'docker inspect bg-proxy', returnStatus: true) == 0)
    if (proxyExists) {
        sh 'docker exec bg-proxy nginx -s reload'
    } else {
        sh "docker run -d --name bg-proxy --restart unless-stopped -p ${livePort}:80 --add-host=host.docker.internal:host-gateway -v ${stateDir}/conf.d:/etc/nginx/conf.d:ro nginx:alpine"
        sleep time: 3, unit: 'SECONDS'
    }

    // 4. record state (old color keeps running for instant rollback)
    sh "echo ${active} > ${stateDir}/previous; echo ${idle} > ${stateDir}/active"
    env.BG_STATE_DIR = stateDir
    env.BG_ACTIVE = idle
    env.BG_PREVIOUS = active
    env.BG_PREV_PORT = (active == 'none') ? '' : "${ports[active]}"
    env.BG_SWITCHED = 'true'
    echo "Traffic switched: ${active} -> ${idle} (live on :${livePort})"
}