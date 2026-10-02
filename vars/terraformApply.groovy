def call(Map config = [:]) {
    String tfDir = config.dir ?: error('terraformApply: dir is required')
    Map tfVars   = config.vars ?: [:]
    String varArgs = tfVars.collect { k, v -> "-var=\"${k}=${v}\"" }.join(' ')

    dir(tfDir) {
        sh 'terraform init'
        sh "terraform apply -auto-approve ${varArgs}"
    }
}