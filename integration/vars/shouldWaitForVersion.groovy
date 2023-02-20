
def call(String shouldWaitForVersion) {
   return shouldWaitForVersion == "null" ? true : shouldWaitForVersion.toBoolean()
}
