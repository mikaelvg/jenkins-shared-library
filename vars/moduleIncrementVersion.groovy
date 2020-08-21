#!/usr/bin/env groovy

def call(Map args = [:]) {
    isVersionFormatValid(args.relver)
    performVersionIncrement(args.relver,args.increment)
}

def isVersionFormatValid(versionNumber) {
    println('given release version: '+versionNumber)

    def releaseVersionArray = versionNumber.split('\\.')
    if (releaseVersionArray.length > 3) {
        error('The provided version is not in the correct format!')
    }
}

def performVersionIncrement(versionNumber,increment) {
    def releaseArray = versionNumber.split('\\.')
    switch(increment) {
        case 'Major':
            println('incrementing major...')
            releaseArray[0]++
            releaseArray[1] = 0
            releaseArray[2] = 0
            break;
        case 'Minor':
            println('incrementing minor...')
            releaseArray[1]++
            break;
        case 'Patch':
            println('incrementing patch...')
            releaseArray[2]++
            break;
    }
    def finalReleaseString = releaseArray.join('.')
    println('generated release version: '+finalReleaseString)
    return finalReleaseString
}