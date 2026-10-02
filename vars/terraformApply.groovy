// Runs terraform init/plan/apply using dynamic AWS creds from Vault.
// Required: dir (terraform working directory), vaultAddr
def call(Map cfg) {
    def creds = vaultAwsCreds(cfg.vaultAddr)
    withEnv([
        "AWS_ACCESS_KEY_ID=${creds.accessKey}",
        "AWS_SECRET_ACCESS_KEY=${creds.secretKey}",
        "AWS_SESSION_TOKEN=${creds.sessionToken}"
    ]) {
        sh "aws sts get-caller-identity"
        dir(cfg.dir) {
            sh '''
                set -e
                terraform init -input=false
                terraform plan -input=false -out=tfplan
                terraform apply -input=false tfplan
            '''
        }
    }
}