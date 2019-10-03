def call(body) {
    def context= [:]
    body.resolveStrategy = Closure.DELEGATE_FIRST
    body.delegate = context
    body()

    properties([
            buildDiscarder(logRotator(artifactDaysToKeepStr: '', artifactNumToKeepStr: '', daysToKeepStr: '', numToKeepStr: '10')),
            durabilityHint('MAX_SURVIVABILITY'),
            parameters([string(defaultValue: '', description: '', name: 'run_stages')])
    ])

    stage('Checkout') {
        node('build') {
            checkout scm
        }
    }

}
