// Logs into Vault via AppRole, reads dynamic AWS creds, returns a map.
// Required Jenkins credentials: vault-role-id, vault-secret-id (Secret text)
def call(String vaultAddr, String awsRolePath = 'aws/creds/terraform-role') {
    withCredentials([
        string(credentialsId: 'vault-role-id', variable: 'ROLE_ID'),
        string(credentialsId: 'vault-secret-id', variable: 'SECRET_ID')
    ]) {
        withEnv(["VAULT_ADDR=${vaultAddr}", "VAULT_SKIP_VERIFY=true"]) {
            def token = sh(
                script: 'vault write -field=token auth/approle/login role_id="$ROLE_ID" secret_id="$SECRET_ID"',
                returnStdout: true
            ).trim()

            withEnv(["VAULT_TOKEN=${token}"]) {
                def creds = sh(
                    script: "vault read -format=json ${awsRolePath}",
                    returnStdout: true
                ).trim()
                def json = readJSON text: creds
                return [
                    accessKey: json.data.access_key,
                    secretKey: json.data.secret_key,
                    sessionToken: json.data.security_token
                ]
            }
        }
    }
}