// Usage (inside vaultAwsCreds { }):
//   dockerPush(region: 'ap-south-1', accountId: '123456789012')
def call(Map config = [:]) {
    String region   = config.region    ?: error('dockerPush: region is required')
    String account  = config.accountId ?: error('dockerPush: accountId is required')
    String repo     = config.repo      ?: env.IMAGE_NAME
    String registry = "${account}.dkr.ecr.${region}.amazonaws.com"
    String local    = "${env.IMAGE_NAME}:${env.IMAGE_TAG}"
    String remote   = "${registry}/${repo}:${env.IMAGE_TAG}"

    sh "aws ecr get-login-password --region ${region} | docker login --username AWS --password-stdin ${registry}"
    sh "docker tag ${local} ${remote}"
    sh "docker push ${remote}"
    env.IMAGE_URI = remote
    echo "Pushed ${remote}"
}