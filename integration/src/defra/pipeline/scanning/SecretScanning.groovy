package defra.pipeline.scanning

class SecretScanning {

  static List<Map<String, String>> getComponents(String deploymentListFile, Script script) {
    String components = script.libraryResource deploymentListFile
    List<Map<String, String>> detailedComponents = new ArrayList<>()

    components.split("\n").each { String component ->
      List<String> parts = component.tokenize(":")
      detailedComponents.add(Map.of(
              'name', parts[0],
              'version', parts[1]
      ))
    }

    return detailedComponents
  }
}
