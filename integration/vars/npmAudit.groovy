def call() {
    echo('Running NPM Audit')
    try {
        sh(returnStdout: true, script: 'npm --prefix ./service audit --json --audit-level=moderate')
    } catch (Exception ex) {
        error('Vulnerability found with level Moderate or greater.')
    }
    echo('No vulnerabilities found with level Moderate or greater.')
}