// DAST via OWASP ZAP baseline scan. Target must already be running.
// Options: failOnWarn (default false)
def call(String targetUrl, boolean failOnWarn = false) {
    sh "mkdir -p zap-reports"
    int rc = sh(
        script: """
            docker run --rm -v \$(pwd)/zap-reports:/zap/wrk:rw \
              ghcr.io/zaproxy/zaproxy:stable zap-baseline.py \
              -t ${targetUrl} -r zap-report.html -J zap-report.json
        """,
        returnStatus: true
    )
    archiveArtifacts artifacts: 'zap-reports/*', allowEmptyArchive: true
    // zap-baseline.py exits 2 on WARN-level findings by default
    if (failOnWarn && rc != 0) {
        error("OWASP ZAP baseline scan found issues (exit ${rc})")
    }
    echo "ZAP scan complete, exit code ${rc}"
}