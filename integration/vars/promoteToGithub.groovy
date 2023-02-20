import defra.pipeline.vault.VaultKey
import defra.pipeline.config.Config

//clone a repo from Github, add files from another cloned repo and force push to master
//used for promoting code from Gitlab to Github for Code in Open

def call(String sourceFolder, String targetRepo, String targetFolder, String vstsUser, String repository, String version) {

  def vstsPat = VaultKey.getSecuredValue('githubDefraPat', this)
  def repositoryUrl = "https://${vstsPat}@${repository}${targetRepo}"
  String blackListFilesList = "${Config.getPropertyValue("blackListFiles", this)}"
  String blackListString = "${Config.getPropertyValue("blackListString", this)}"

  sh(script: """
  set +x
  git clone "${repositoryUrl}" && \
  echo "SOURCE_FOLDER IS: ${sourceFolder}"
  echo "DELETING FILES FROM THE BLACK LIST: ${blackListFilesList}"
  for file in ${blackListFilesList}
    do
    find . -name \$file -type f -delete -print
  done
  echo "REMOVING STRINGS: ${blackListString}"
  echo "FILES TO BE MODIFIED:"
  grep -rlin --include \\*.json --include \\*.xml  --exclude-dir='.?*' ${blackListString} .
  grep -rlin --include \\*.json --include \\*.xml  --exclude-dir='.?*' ${blackListString} . | xargs --no-run-if-empty sed -i 's/${blackListString}/REMOVED/g'
  cp -r ${sourceFolder}/* ${targetRepo}
  cd ${targetRepo}
  git add -A && git commit --allow-empty -m "${version} release code"
  git tag -a ${version} -m "RC-${version}"
  echo "[INFO] Pushing changes to github..."
  git push -f "${repositoryUrl}" master --follow-tags
  """)
}
