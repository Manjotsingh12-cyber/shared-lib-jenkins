// Usage (in post { failure { rollback() } })
// Only acts if THIS build already switched traffic.
def call(Map config = [:]) {
    if (env.BG_SWITCHED != 'true') {
        echo 'Rollback skipped: this build never switched traffic.'
        return
    }
    String label = config.label ?: 'node-2'

    node(label) {
        String prev = env.BG_PREVIOUS
        String failed = env.BG_ACTIVE
        String stateDir = env.BG_STATE_DIR

        if (!prev || prev == 'none' || !env.BG_PREV_PORT) {
            echo 'Rollback skipped: no previous color exists (first deploy).'
            return
        }
        if (sh(script: "docker ps -q -f name=^app-${prev}\$ | grep -q .", returnStatus: true) != 0) {
            error "Rollback impossible: app-${prev} is not running."
        }

        echo "ROLLBACK: ${failed} -> ${prev}"
        writeFile file: "${stateDir}/conf.d/default.conf", text: """server {
    listen 80;
    location / {
        proxy_pass http://host.docker.internal:${env.BG_PREV_PORT};
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
    }
}
"""
        sh 'docker exec bg-proxy nginx -s reload'
        sh "echo ${prev} > ${stateDir}/active; echo ${failed} > ${stateDir}/previous"
        echo "Rolled back. Live color is now ${prev}."
    }
}