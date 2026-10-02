def call(Map config = [:]) {
    String image    = config.image ?: error('trivyScan: image is required')
    String severity = config.severity ?: 'HIGH,CRITICAL'
    int exitCode    = config.exitCode != null ? config.exitCode : 0

    sh "trivy image --exit-code ${exitCode} --severity ${severity} ${image} || true"
}