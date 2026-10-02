def call(Map config = [:]) {

    def cfg = [
        appName        : null,
        sonarProjectKey: null,
        sonarEnv       : 'sonar',
        terraformDir   : '/home/mbrar/terraform',
        awsRegion      : 'ap-south-1',
        ciLabel        : 'ci',
        buildLabel     : 'sast',
        deployLabel    : 'node-2'
    ] + config

    if (!cfg.appName)         { error('ecsPipeline: appName is required') }
    if (!cfg.sonarProjectKey) { error('ecsPipeline: sonarProjectKey is required') }

    pipeline {
        agent none

        stages {

            stage('Test') {
                agent { label cfg.ciLabel }
                steps { runTests() }
            }

            stage('SAST') {
                agent { label cfg.ciLabel }
                steps { banditScan() }
                post {
                    always {
                        archiveArtifacts artifacts: 'bandit-report.txt', allowEmptyArchive: true
                    }
                }
            }

            stage('Secret Scan') {
                agent { label cfg.ciLabel }
                steps { gitleaksScan() }
                post {
                    always {
                        archiveArtifacts artifacts: 'gitleaks-report.json', allowEmptyArchive: true
                    }
                }
            }

            stage('SonarQube Scan') {
                agent { label cfg.ciLabel }
                steps {
                    sonarScan(projectKey: cfg.sonarProjectKey, sonarEnv: cfg.sonarEnv)
                }
            }

            stage('Docker Build') {
                agent { label cfg.buildLabel }
                steps { dockerBuild(appName: cfg.appName) }
            }

            stage('Trivy Scan') {
                agent { label cfg.buildLabel }
                steps { trivyScan(image: "${cfg.appName}:${env.IMAGE_TAG}") }
            }

            stage('SBOM') {
                agent { label cfg.buildLabel }
                steps { generateSbom(image: "${cfg.appName}:${env.IMAGE_TAG}") }
                post {
                    always {
                        archiveArtifacts artifacts: 'sbom.json', allowEmptyArchive: true
                    }
                }
            }

            stage('Terraform Infrastructure') {
                agent { label cfg.deployLabel }
                steps { terraformApply(dir: cfg.terraformDir) }
            }

            stage('Push Image to ECR') {
                agent { label cfg.deployLabel }
                steps {
                    pushToEcr(appName: cfg.appName, terraformDir: cfg.terraformDir, region: cfg.awsRegion)
                }
            }

            stage('Deploy to ECS') {
                agent { label cfg.deployLabel }
                steps {
                    terraformApply(dir: cfg.terraformDir, vars: [image_tag: env.IMAGE_TAG])
                }
            }

            stage('Wait for ECS') {
                agent { label cfg.deployLabel }
                steps { waitForEcs(terraformDir: cfg.terraformDir, region: cfg.awsRegion) }
            }

            stage('Validate') {
                agent { label cfg.deployLabel }
                steps { validateDeployment(terraformDir: cfg.terraformDir) }
            }

            stage('OWASP ZAP Scan') {
                agent { label cfg.deployLabel }
                steps { zapScan(terraformDir: cfg.terraformDir) }
            }
        }
    }
}