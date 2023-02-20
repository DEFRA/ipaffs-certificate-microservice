package defra.pipeline

class BaseTest extends Script {

    // List of files that have been requested, for asserting
    def filesGot = []

    // Dictionary of files that have been written, for asserting
    def filesWritten = [:]

    // Dictionary of shell commands ran, for asserting
    def shellCommandsRan = []

    // Dictionary of shell commands ran, for asserting
    def shellCommandsReturn = [:]

    // Dictionary of shell commands ran, for asserting
    def shellCommandsThrow = [:]

    // The files that we should return to say they exist
    def filesExist = []

    def run() {
        println 'Dummy!'
    }

    def libraryResource(String filePath) {
        if (filePath in filesWritten) {
            return filesWritten[filePath]
        }
        return new File('resources/' + filePath).text
    }

    def getTestDataFile(String filePath) {
        return new File('test/data/' + filePath).text
    }

    def getFile(String filePath) {
        filesGot.add(filePath)
    }

    def fileExists(String filePath) {
        return filePath in filesExist
    }

    def writeFile(String filePath, String contents) {
        //new File(filePath).write(contents)
        filesWritten[filePath] = contents
        filesExist.add(filePath)
    }

    def writeFile(Map l) {
        //new File(filePath).write(contents)
        filesWritten[l.file] = l.text
        filesExist.add(l.file)
    }

    def readFile(Map l) {
        return filesWritten[l.file]
    }

    def sleep(seconds) {
    }

    def sh(vars) {
        shellCommandsRan.add(vars.script)

        def ret = ""

        shellCommandsThrow.keySet().each {
            if (vars['script'].matches(it)) {
                throw new Exception(shellCommandsReturn[it])
            }
        }

        if (vars.returnStdout) {
            shellCommandsReturn.keySet().each {
                if (vars['script'].matches(it)) {
                    ret = shellCommandsReturn[it]
                }
            }
        }

        return ret
    }

    def echo(String s) {
        System.out.println(s)
    }

    def testCommandRan(String cmd) {
        def cmdsRan = []
        shellCommandsRan.each {
            if (it.matches(cmd)) {
                cmdsRan.add(it)
            }
        }
        return cmdsRan
    }

}
