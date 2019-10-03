def call(Map args = [:]) {
podTemplate(yaml: """
apiVersion: v1
kind: Pod
spec:
  containers:
  - name: maven
    image: maven:3.3.9-jdk-8-alpine
    command: ['cat']
    tty: true
  - name: awscli
    image: amazon/aws-cli:2.0.41
    command: ['cat']
    tty: true
"""
  ){
        properties([[$class: 'JiraProjectProperty', siteName: 'https://diginex.atlassian.net/'], parameters([[$class: 'JiraVersionParameterDefinition', description: 'List of Jira Releases in project TJI', jiraProjectKey: 'TJI', jiraReleasePattern: '', jiraShowArchived: 'false', jiraShowReleased: 'false', name: 'jiraRelease'], choice(choices: ['dev', 'qa', 'uat', 'test', 'production'], description: 'Environment to deploy to', name: 'environment')])])
        node(POD_LABEL) {
            stage("deploying to selected environment") {
                sh "echo 'release chosen: ${params.jiraRelease}'"
                currentBuild.description = "${params.environment}"
                container('awscli') {
                    deployFromS3(releaseVersion:params.jiraRelease)
                }
            }
            stage('test 1') {
                echo 'doing something'
            }
            stage('test 2') {
                echo 'doing something'
            }
            stage('test 3') {
                echo 'doing something'
            }
        }
    }
}

