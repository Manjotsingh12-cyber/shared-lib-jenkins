def call(Map config = [:]) {
    String appName = config.appName ?: error('dockerBuild: appName is required')

    env.GIT_SHA     = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
    env.IMAGE_TAG   = env.GIT_SHA
    env.APP_VERSION = "0.1.${env.BUILD_NUMBER}"

    sh """
        docker build \\
          --build-arg GIT_SHA=${env.GIT_SHA} \\
          --build-arg BUILD_NUMBER=${env.BUILD_NUMBER} \\
          --build-arg APP_VERSION=${env.APP_VERSION} \\
          -t ${appName}:${env.IMAGE_TAG} .
    """
}