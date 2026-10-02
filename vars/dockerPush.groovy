// Usage: dockerPush(registry: 'docker.io/youruser', credentialsId: 'dockerhub-creds')
def call(Map config = [:]) {
    String registry = config.registry ?: error('dockerPush: registry is required')
    String credsId = config.credentialsId ?: error('dockerPush: credentialsId is required')
    String local = "${env.IMAGE_NAME}:${env.IMAGE_TAG}"
    String remote = "${registry}/${local}"

    withEnv(["REGISTRY_HOST=${registry.tokenize('/')[0]}"]) {
        withCredentials([usernamePassword(credentialsId: credsId, usernameVariable: 'REG_USER', passwordVariable: 'REG_PASS')]) {
            sh 'echo "$REG_PASS" | docker login "$REGISTRY_HOST" -u "$REG_USER" --password-stdin'
        }
    }
    sh "docker tag ${local} ${remote}"
    sh "docker push ${remote}"
    env.IMAGE_REMOTE = remote
}