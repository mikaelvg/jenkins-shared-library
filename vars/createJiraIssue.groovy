#!/usr/bin/env groovy

def call(Map args = [:]) {
def testIssue = [fields: [ // id or key must present for project.
                               project: [key: 'TJI'],
                               summary: 'New JIRA Created from Jenkins.',
                               description: 'Built new release',
                               environment: 'dev',
                               // id or name must present for issueType.
                               issuetype: [name: 'Task']]]
jiraNewIssue site: 'diginex-jira', issue: testIssue
println('created jira issue')
}