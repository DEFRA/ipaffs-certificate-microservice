#!/usr/bin/env groovy

def call(String branchName = "master") {
  if (!env.BRANCH_NAME || "${BRANCH_NAME}" == branchName) {
      archiveArtifacts('**/cucumber/**/*.json')
  }
}
