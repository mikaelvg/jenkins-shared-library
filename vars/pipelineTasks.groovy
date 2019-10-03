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
        }
        post { 
            failure { 
                echo 'Notify failure'
            }
        }
    }
}