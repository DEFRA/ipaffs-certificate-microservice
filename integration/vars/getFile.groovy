#!/usr/bin/env groovy

def call(String fileNameAndPath) {
  echo "Writing File " + fileNameAndPath
  writeFile file:fileNameAndPath, text:libraryResource(fileNameAndPath)
  echo "File " + fileNameAndPath + " written"
}
