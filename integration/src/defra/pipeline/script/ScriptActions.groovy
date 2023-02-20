package defra.pipeline.script

class ScriptActions {

    /**
     * Run a command and log the output only on error
     *
     * @param cmd     The command to run
     * @param script  The global script parameter
     */
    private static void runCommandLogOnlyOnError(String cmd, Script script) {

        def out
        try {
            out = script.sh(script: cmd, returnStdout: true)
        } catch (Exception e) {
            script.echo("""Error: ${out}""")
            throw e
        }

    }

}
