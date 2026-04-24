def isProtectedBranch(String branchName) {
    return ['main', 'develop'].contains(branchName)
}

def branchLabel(String branchName) {
    return isProtectedBranch(branchName) ? 'protected' : 'feature'
}

def mvnw(String command) {
    if (isUnix()) {
        sh "./mvnw ${command}"
    } else {
        bat "mvnw.cmd ${command}"
    }
}

return this
