def call(Map config = [:]) {
    String projectKey = config.projectKey ?: error('sonarScan: projectKey is required')
    String sonarEnv   = config.sonarEnv ?: 'sonar'

    withSonarQubeEnv(sonarEnv) {
        sh "sonar-scanner -Dsonar.projectKey=${projectKey} -Dsonar.sources=."
    }
}