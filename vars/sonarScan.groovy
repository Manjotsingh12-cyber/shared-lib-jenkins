def call(String projectKey, String sonarEnvName = 'sonar') {
    withSonarQubeEnv(sonarEnvName) {
        sh "sonar-scanner -Dsonar.projectKey=${projectKey} -Dsonar.sources=."
    }
}