def call(Map config = [:]) {
    String report = config.report ?: 'gitleaks-report.json'
    sh "gitleaks detect --source . --report-path ${report}"
}