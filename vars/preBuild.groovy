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

    stage('Prepare') {
        abortAllPreviousBuildInProgress(currentBuild)
        echo "BRANCH_NAME=${env.BRANCH_NAME}\nCHANGE_ID=${env.CHANGE_ID}\nCHANGE_TARGET=${env.CHANGE_TARGET}\nBUILD_URL=${env.BUILD_URL}"
    }

    stage('Checkout') {
        checkout scm

    }

}
