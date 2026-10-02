def call(Map config = [:]) {
    String tfDir   = config.terraformDir ?: error('zapScan: terraformDir is required')
    String workDir = config.workDir ?: '/mnt/trivy/zap-work'

    withEnv(["TF_DIR=${tfDir}", "ZAP_DIR=${workDir}"]) {
        sh '''
            sudo rm -rf "$ZAP_DIR"
            sudo mkdir -p "$ZAP_DIR"
            sudo chmod 0777 "$ZAP_DIR"

            ALB_DNS=$(cd "$TF_DIR" && terraform output -raw alb_dns_name)

            echo "Running OWASP ZAP against: http://$ALB_DNS"

            docker run --rm \
              --network host \
              -v "$ZAP_DIR":/zap/wrk/:rw \
              ghcr.io/zaproxy/zaproxy:stable \
              zap-baseline.py \
              -t "http://$ALB_DNS" \
              -r zap-report.html \
              -J zap-report.json \
              || true

            cp "$ZAP_DIR/zap-report.html" "$WORKSPACE/"
            cp "$ZAP_DIR/zap-report.json" "$WORKSPACE/"
        '''
    }
    archiveArtifacts artifacts: 'zap-report.html,zap-report.json', allowEmptyArchive: false
}