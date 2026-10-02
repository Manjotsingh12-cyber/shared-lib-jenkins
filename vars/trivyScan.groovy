// Usage: trivyScan(failOn: 'CRITICAL')
// Reports HIGH+CRITICAL, fails the build only on failOn severity.
def call(Map config = [:]) {
    String image = config.image ?: "${env.IMAGE_NAME}:${env.IMAGE_TAG}"
    String failOn = config.failOn ?: 'CRITICAL'
    String cacheDir = config.cacheDir ?: '/mnt/trivy'
    String unfixed = (config.ignoreUnfixed == false) ? '' : '--ignore-unfixed'

    try {
        // JSON report for archiving
        sh "trivy --cache-dir ${cacheDir} image --exit-code 0 --severity HIGH,CRITICAL ${unfixed} --format json --output trivy-report.json ${image}"
        // readable table in console log
        sh "trivy --cache-dir ${cacheDir} image --exit-code 0 --severity HIGH,CRITICAL ${unfixed} ${image}"
        // the gate
        sh "trivy --cache-dir ${cacheDir} image --exit-code 1 --severity ${failOn} ${unfixed} --quiet ${image}"
    } finally {
        archiveArtifacts artifacts: 'trivy-report.json', allowEmptyArchive: true
    }
}