def call(Map args = [:]) {
    pipeline {
        agent {
            kubernetes {
                containerTemplate {
                    name 'maven'
                    image 'maven:3.6.3-openjdk-11'
                    ttyEnabled true
                    command 'cat'
                }
                defaultContainer 'maven'
            }
        }
        parameters {
            choice(name: 'environment', choices: ['dev','qa','test','uat','production'], description: 'Environment to deploy to')
            gitParameter(name: 'BRANCH_TAG', 
                     type: 'PT_BRANCH_TAG',
                     defaultValue: 'master')
            extendedChoice(bindings: '', description: '', groovyClasspath: '', groovyScript: '''import com.cloudbees.plugins.credentials.CredentialsProvider;
            import com.cloudbees.plugins.credentials.common.StandardUsernamePasswordCredentials;
            import jenkins.model.Jenkins;
            import groovy.json.JsonSlurper;

            def creds = CredentialsProvider.lookupCredentials(
                StandardUsernamePasswordCredentials.class, 
                Jenkins.instance
            );
            def c = creds.findResult { it.username == \'zqchindiginex\' ? it : null }
            def pass = c.password;
            def curlOutput = ["curl","--data",\'{"query":"{repository(owner:\\\\"zqchindiginex\\\\",name:\\\\"matching-engine\\\\"){packages(first:1){nodes{versions(first:5){nodes{version}}}}}}"}\',"--header","Authorization: Bearer "+pass,"--header", "Content-Type: application/json","--location","--request","POST", "https://api.github.com/graphql"].execute().text
            def parsedJson = new groovy.json.JsonSlurper().parseText(curlOutput)
            return resultList = parsedJson.data.repository.packages.nodes.versions.nodes.version[0]''', multiSelectDelimiter: ',', name: 'releaseVersion', quoteValue: false, saveJSONParameterToFile: false, type: 'PT_SINGLE_SELECT', visibleItemCount: 5)
        }
        stages {
            stage('checkout') {
                steps {
                    sh "echo 'environment chosen: ${params.environment}'"
                    sh "echo 'release chosen: ${params.releaseVersion}'"
                }
            }
            stage('Deployment') {
                steps{
                    container('maven') {
                        echo 'deploying to ${params.environment}'
                        script {
                            currentBuild.displayName = 'Test setting display name '+env.BUILD_NUMBER
                            currentBuild.description = 'Test setting display description'
                        }
                    }
                }
            }
            stage('tests 1') {
                steps{
                    container('maven') {
                        echo 'performing tests'
                    }
                }
            }
            stage('tests 2') {
                steps{
                    container('maven') {
                        echo 'performing tests'
                    }
                }
            }
            stage('tests 3') {
                steps{
                    container('maven') {
                        echo 'performing tests'
                    }
                }
            }
        }
    }
}