def call(Map config = [:]) {
    String appName = config.appName ?: error('pushToEcr: appName is required')
    String tfDir   = config.terraformDir ?: error('pushToEcr: terraformDir is required')
    String region  = config.region ?: 'ap-south-1'

    withEnv(["APP_NAME=${appName}", "REGION_NAME=${region}"]) {
        dir(tfDir) {
            sh '''
                ECR_REPO=$(terraform output -raw ecr_repository_url)

                echo "ECR Repository: $ECR_REPO"
                echo "Image Tag: $IMAGE_TAG"

                aws ecr get-login-password --region "$REGION_NAME" | \
                  docker login --username AWS --password-stdin "$ECR_REPO"

                docker tag "${APP_NAME}:${IMAGE_TAG}" "$ECR_REPO:${IMAGE_TAG}"
                docker push "$ECR_REPO:${IMAGE_TAG}"
            '''
        }
    }
}