def call(Map config = [:]) {
    String tfDir = config.terraformDir ?: error('validateDeployment: terraformDir is required')

    dir(tfDir) {
        sh '''
            ALB_DNS=$(terraform output -raw alb_dns_name)

            echo "ALB: http://$ALB_DNS"

            echo "Checking /health..."
            curl -sf "http://$ALB_DNS/health"

            echo ""
            echo "Checking /version..."
            curl -sf "http://$ALB_DNS/version"

            echo ""
            echo "ECS deployment validated successfully."
        '''
    }
    echo "Deployed and validated: ${env.APP_VERSION} (${env.GIT_SHA})"
}