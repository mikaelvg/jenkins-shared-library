#!/usr/bin/env groovy

def call(Map args = [:]) {
def jiraRelease = [ name: args.releaseVersion,
                    archived: false,
                    released: false,
                    description: 'Release '+args.releaseVersion,
                    project: args.projectKey ]
try {
    jiraNewVersion site: 'diginex-jira', version: jiraRelease, failOnError: false 
}catch(Exception e) {
    // We ignore if creating a jira version fails
    // Need find a better way
}
}