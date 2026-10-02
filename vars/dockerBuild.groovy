// Usage: dockerBuild(image: 'billing-payment')
def call(Map config = [:]) {
    String image = config.image ?: error('dockerBuild: image is required')
    String sha = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
    String tag = config.tag ?: sha
    String version = config.version ?: "0.1.${env.BUILD_NUMBER}"

    sh """
        docker build \
          --build-arg GIT_SHA=${sha} \
          --build-arg BUILD_NUMBER=${env.BUILD_NUMBER} \
          --build-arg APP_VERSION=${version} \
          -t ${image}:${tag} .
    """

    // available to later stages in the same build
    env.IMAGE_NAME = image
    env.IMAGE_TAG = tag
    env.GIT_SHA = sha
    env.APP_VERSION = version
    echo "Built ${image}:${tag} (version ${version})"
}