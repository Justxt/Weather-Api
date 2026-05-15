def normalizeBranch(String branchName) {
    def branch = (branchName ?: 'main')
        .replace('refs/heads/', '')
        .replace('origin/', '')
        .replaceFirst('^\\*/', '')
    return branch
}

def isProtectedBranch(String branchName) {
    return ['main', 'develop'].contains(normalizeBranch(branchName))
}

def branchLabel(String branchName) {
    return isProtectedBranch(branchName) ? 'protected' : 'feature'
}

def mvnw(String command) {
    if (isUnix()) {
        retry(2) {
            sh "mvn ${command}"
        }
    } else {
        retry(2) {
            bat "mvnw.cmd ${command}"
        }
    }
}

return this
