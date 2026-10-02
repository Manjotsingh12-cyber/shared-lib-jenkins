def call(Map config = [:]) {
    String image  = config.image ?: error('generateSbom: image is required')
    String output = config.output ?: 'sbom.json'

    sh "syft ${image} -o json > ${output}"
}