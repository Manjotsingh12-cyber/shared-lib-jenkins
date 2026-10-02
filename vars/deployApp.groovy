// Usage (inside vaultAwsCreds { }): deployApp(dir: 'terraform')
def call(Map config = [:]) {
    String tfDir = config.dir ?: 'terraform'
    String tag   = config.imageTag ?: env.IMAGE_TAG

    dir(tfDir) {
        sh 'terraform init -input=false'
        sh "terraform apply -input=false -auto-approve -var image_tag=${tag}"
        env.APP_URL = sh(script: 'terraform output -raw app_url', returnStdout: true).trim()
    }
    echo "Deployed ${tag} -> ${env.APP_URL}"
}