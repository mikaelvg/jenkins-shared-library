#!/usr/bin/env groovy

def call(Map args = [:]) {
    pipeline {
        parameters {
            booleanParam(name: 'MAJOR', defaultValue: false, description: 'Increment major version')
            booleanParam(name: 'MINOR', defaultValue: false, description: 'Increment minor version')
            booleanParam(name: 'PATCH', defaultValue: false, description: 'Increment patch version')
        }
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
            stage('checkout') {
                steps {
                    container('maven'){
                        checkout([$class: 'GitSCM', branches: [[name: '**']], doGenerateSubmoduleConfigurations: false, extensions: [[$class: 'LocalBranch', localBranch: '**']], submoduleCfg: [], userRemoteConfigs: [[credentialsId: 'github-exchange-token', url: 'https://github.com/zqchindiginex/matching-engine.git']]])
                        sh 'mvn -ntp clean'
                        script {
                            env.CURRENT_RELEASE_VERSION=sh(label:'Retrieve version',returnStdout:true,script:'mvn help:evaluate -Dexpression=project.version -q -DforceStdout').trim()
                            env.NEW_RELEASE_VERSION=incrementVersion(major:params.MAJOR,minor:params.MINOR,patch:params.PATCH,relver:env.CURRENT_RELEASE_VERSION)
                        }
                    }
                }
            }
            stage('build & unit test') {
                steps{
                    container('maven') {
                        sh 'mvn -ntp clean test'
                    }
                }
            }
            stage('push snapshot') {
                when { beforeAgent true; branch 'task/*'; }
                steps{
                    container('maven') {
                        withCredentials([file(credentialsId: 'settings.xml', variable: 'SETTINGS')]) {
                            sh 'mvn -ntp versions:set -DnewVersion=1.0.0-SNAPSHOT'
                            sh "mvn -ntp -s ${SETTINGS} clean deploy"
                        }
                    }
                }
            }
            stage('create release') {
                when {beforeAgent true; branch 'develop'; expression { return env.CURRENT_RELEASE_VERSION != env.NEW_RELEASE_VERSION }}
                steps {
                    container('maven') {
                        echo 'version number not same, creating release...'
                        sh 'git config --global user.email "jenkins_test@diginex.com"'
                        sh 'git config --global user.name "jenkins_test"'
                        sh 'mvn -ntp clean'
                        sh "mvn -ntp versions:set -DnewVersion=${env.NEW_RELEASE_VERSION}"
                        withCredentials([usernamePassword(credentialsId: 'GITHUB_CRED', passwordVariable: 'GH_PASSWORD', usernameVariable: 'GH_USERNAME')]) {
                            sh "mvn -ntp -Dmessage='Release ${env.NEW_RELEASE_VERSION}' scm:checkin"
                            sh "mvn -ntp -Dbranch='release/${env.NEW_RELEASE_VERSION}' scm:branch"
                        }
                        withCredentials([file(credentialsId: 'settings.xml', variable: 'SETTINGS')]) {
                            sh "mvn -s ${SETTINGS} -ntp deploy scm:tag"
                        }
                        script {
                            createJiraRelease(projectKey:'TJI',releaseVersion:env.NEW_RELEASE_VERSION)
                        }
                    }
                }
            }
        }
    }
}