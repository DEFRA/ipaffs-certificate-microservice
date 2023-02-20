import defra.pipeline.vault.VaultKey

//clone a repo from VSTS, add files from another cloned repo and force push to master
//used for promoting code from Gitlab to VSTS for use in Octopus

def call(String sourceRepo, String sourceFolder, String targetRepo, String targetFolder, String vstsUser, String repository, String version) {

  def vstsPat = VaultKey.getSecuredValue('vstsPat', this)
  def repositoryUrl = "https://${vstsUser}:${vstsPat}@${repository}/${targetRepo}"

  sh(script: """
  set +x
  git clone "${repositoryUrl}" && \
  cd ${targetRepo}/${targetFolder} && \
  echo "SOURCE_FOLDER IS: ${sourceFolder}"
  cp -r ${sourceFolder} ./
  git add -A && git commit --allow-empty -m "ARM Templates"
  git tag -a ${version} -m "Release of RC ${version}"
  echo "[INFO] Pushing changes to VSTS..."
  git push -f "${repositoryUrl}" master --follow-tags
  """)
}
