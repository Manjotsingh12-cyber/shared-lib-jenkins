// Usage: notify()  |  notify(slackChannel: '#ci')  |  notify(email: 'you@example.com')
def call(Map config = [:]) {
    String status = currentBuild.currentResult ?: 'UNKNOWN'
    String msg = "${env.JOB_NAME} #${env.BUILD_NUMBER}: ${status} (${env.BUILD_URL})"
    echo msg

    if (config.slackChannel) {
        slackSend channel: config.slackChannel, message: msg
    }
    if (config.email) {
        mail to: config.email, subject: "[Jenkins] ${status}: ${env.JOB_NAME} #${env.BUILD_NUMBER}", body: msg
    }
}