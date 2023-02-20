
def call(String shouldSkipStage) {
   return shouldSkipStage == "null" ? false : shouldSkipStage.toBoolean()
}
