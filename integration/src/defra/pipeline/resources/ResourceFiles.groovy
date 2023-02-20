package defra.pipeline.resources

class ResourceFiles {

    public static void getFile(String fileNameAndPath, Script script) {

        script.echo("Writing File " + fileNameAndPath)
        if (!script.fileExists(fileNameAndPath)) {
            script.writeFile file:fileNameAndPath, text:script.libraryResource(fileNameAndPath)
            script.echo "File " + fileNameAndPath + " written"
        } else {
            script.echo("File already exists, not extracting again")
        }
    }

    public static void getBase64FileBinary(String encodedFileNameAndPath, String decodedFileNameAndPath, Script script) {

        //WORKAROUND, till either artifactory arrives or we upgrade jenkins to version that supports Base64 in resource
        //Files are pre-encoded in base64 before adding as resource
        //on mac: openssl base64 -in <infile> -out <outfile>
        def binaryFileBase64 = script.libraryResource(resource:encodedFileNameAndPath)
        script.writeFile(file:encodedFileNameAndPath, text:binaryFileBase64)
        def decodeCommand = "base64 --decode ${encodedFileNameAndPath} > ${decodedFileNameAndPath}" 
        script.sh(script: decodeCommand)
        script.echo "File " + decodedFileNameAndPath + " written"
    }
}
