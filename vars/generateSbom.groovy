// Usage: generateSbom()
def call(Map config = [:]) {
    String image = config.image ?: "${env.IMAGE_NAME}:${env.IMAGE_TAG}"
    String out = config.output ?: 'sbom.cdx.json'

    sh "syft ${image} -o cyclonedx-json=${out}"
    archiveArtifacts artifacts: out, fingerprint: true
}