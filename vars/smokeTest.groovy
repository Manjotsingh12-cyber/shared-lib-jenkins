// Usage: smokeTest()  or  smokeTest(url: 'http://localhost:8080')
def call(Map config = [:]) {
    String baseUrl = config.url ?: 'http://localhost:8080'
    int retries = (config.retries ?: 5) as int

    boolean ok = false
    for (int i = 1; i <= retries && !ok; i++) {
        ok = (sh(script: "curl -sf ${baseUrl}/health", returnStatus: true) == 0)
        if (!ok) {
            echo "Health check attempt ${i}/${retries} failed"
            sleep time: 3, unit: 'SECONDS'
        }
    }
    if (!ok) {
        error "Smoke test failed: ${baseUrl}/health not healthy"
    }

    String body = sh(script: "curl -sf ${baseUrl}/version", returnStdout: true).trim()
    echo "Live /version: ${body}"

    if (env.GIT_SHA && !body.contains(env.GIT_SHA)) {
        error "Smoke test failed: live version does not contain expected SHA ${env.GIT_SHA}"
    }
    echo "Smoke test passed"
}