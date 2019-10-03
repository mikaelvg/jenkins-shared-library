#!/usr/bin/env groovy

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
        stages {
            stage('build & unit test') {
                steps{
                    container('maven') {
                        sh 'mvn -ntp clean test'
                    }
                }
            }
            stage('push snapshot') {
                when { beforeAgent true; branch 'develop'; }
                steps{
                    container('maven') {
                        withCredentials([file(credentialsId: 'settings.xml', variable: 'SETTINGS')]) {
                            sh 'mvn -ntp versions:set -DnewVersion=1.0.0-SNAPSHOT'
                            sh "mvn -ntp -s ${SETTINGS} clean deploy"
                        }
                    }
                }
            }
        }
        post { 
            failure { 
                echo 'Notify snapshot creation failed'
            }
            success {
                echo 'Notify snapshot created'
            }
        }
    }
}