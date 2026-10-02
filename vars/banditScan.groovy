def call(Map config = [:]) {
    String report = config.report ?: 'bandit-report.txt'
    sh "bandit -r . -f txt -o ${report} --exit-zero"
    sh "cat ${report}"
}