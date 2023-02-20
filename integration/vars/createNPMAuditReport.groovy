def call() {
    echo('Creating NPM Audit Report')
    try {
        sh script: 'cd service && npm audit --json > npm-audit-report.json || :'
        archiveArtifacts('**/service/npm-audit-report.json')
        echo "Successfully generated NPM audit report."
    } catch (Exception e) {
        echo "Can't generate NPM audit report for this microservice."
    }
}