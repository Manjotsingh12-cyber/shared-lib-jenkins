def call(Map config = [:]) {
    String tfDir  = config.terraformDir ?: error('waitForEcs: terraformDir is required')
    String region = config.region ?: 'ap-south-1'

    withEnv(["REGION_NAME=${region}"]) {
        dir(tfDir) {
            sh '''
                CLUSTER=$(terraform output -raw ecs_cluster_name)
                SERVICE=$(terraform output -raw ecs_service_name)

                echo "ECS Cluster: $CLUSTER"
                echo "ECS Service: $SERVICE"
                echo "Waiting for ECS service to stabilize..."

                aws ecs wait services-stable \
                  --cluster "$CLUSTER" \
                  --services "$SERVICE" \
                  --region "$REGION_NAME"

                echo "ECS service is stable."
            '''
        }
    }
}