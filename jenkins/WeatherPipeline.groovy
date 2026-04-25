def isProtectedBranch(String branchName) {
    return ['main', 'develop'].contains(branchName)
}

def branchLabel(String branchName) {
    return isProtectedBranch(branchName) ? 'protected' : 'feature'
}

def mvnw(String command) {
    if (isUnix()) {
        retry(2) {
            sh "chmod +x mvnw && ./mvnw ${command}"
        }
    } else {
        retry(2) {
            bat "mvnw.cmd ${command}"
        }
    }
}

return this
