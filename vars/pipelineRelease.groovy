#!/usr/bin/env groovy

def call(Map args = [:]) {
    pipeline {
        parameters { choice(name: 'INCREMENT', choices: ['Major', 'Minor', 'Patch'], description: 'Increment version') }
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
        stages {
            stage('Confirm parameter') {
                steps {
                    script {
                    env.CONFIRM_INCREMENT = input(
                        message: "Increment ${params.INCREMENT} ?",
                        ok: 'Yes',
                        parameters: [
                        choice(name: "Increment ${params.INCREMENT} ?", choices: ['Yes', 'No'].join('\n'), description: '')
                        ]
                    )
                    }
                }
            }
            stage('Calculating version') {
                when {beforeAgent true; branch 'release'; expression { return env.CONFIRM_INCREMENT == 'Yes'}}
                steps {
                    container('maven'){
                        script {
                            env.CURRENT_RELEASE_VERSION=sh(label:'Retrieve version',returnStdout:true,script:'mvn help:evaluate -Dexpression=project.version -q -DforceStdout').trim()
                            env.NEW_RELEASE_VERSION=moduleIncrementVersion(increment:params.INCREMENT,relver:env.CURRENT_RELEASE_VERSION)
                            currentBuild.displayName = "${env.NEW_RELEASE_VERSION}-${env.BUILD_NUMBER}"
                        }
                    }
                }
            }
            stage('build & unit test') {
                when {beforeAgent true; branch 'release'; expression { return env.CONFIRM_INCREMENT == 'Yes'}}
                steps{
                    container('maven') {
                        //sh 'mvn -ntp clean test'
                    }
                }
            }
            stage('create release') {
                when {beforeAgent true; branch 'release'; expression { return env.CURRENT_RELEASE_VERSION != env.NEW_RELEASE_VERSION && env.CONFIRM_INCREMENT == 'Yes'}}
                steps {
                    container('maven') {
                        script {
                            moduleJiraRelease(projectKey:'TJI',releaseVersion:"${env.NEW_RELEASE_VERSION}")
                        }
                        sh 'git config --global user.email "jenkins_test@diginex.com"'
                        sh 'git config --global user.name "jenkins_test"'
                        sh 'mvn -ntp clean'
                        sh "mvn -ntp versions:set -DnewVersion=${env.NEW_RELEASE_VERSION}"
                        withCredentials([usernamePassword(credentialsId: 'github-exchange-token', passwordVariable: 'GH_PASSWORD', usernameVariable: 'GH_USERNAME')]) {
                            sh "mvn -ntp -Dmessage='Release ${env.NEW_RELEASE_VERSION}' scm:checkin"
                        }
                        withCredentials([file(credentialsId: 'settings.xml', variable: 'SETTINGS')]) {
                            sh "mvn -s ${SETTINGS} -ntp deploy scm:tag"
                        }
                    }
                }
            }
        }
        post { 
            failure { 
                echo 'Notify release creation failed'
            }
            success {
                echo 'Notify release created'
            }
        }
    }
}