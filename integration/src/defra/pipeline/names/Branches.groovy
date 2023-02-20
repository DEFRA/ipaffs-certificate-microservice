package defra.pipeline.names

import defra.pipeline.config.Config;

class Branches {

    /**
     * Get the IMTA number of a feature branch of the format feature/IMTA-1234-text
     *
     * @param branchName  The name of the branch
     * @return null if master, the IMTA number if it is there else the branch name with '/' mapped to '-'
     */
    public static String getBranchPrefix(String branchName) {

        def tokens = null
        if (branchName.toLowerCase() != 'master') {
            tokens = branchName.tokenize('/')
            if (tokens.size() > 1) {
                tokens = tokens[1].tokenize('-')
                if (tokens.size() > 1 && tokens[0].matches("[A-Z]*") && tokens[1].matches("[1-9][0-9]*")) {
                    return tokens[0].toLowerCase() + '-' + tokens[1]
                }
            }
            return branchName.replaceAll('/', '-').toLowerCase()
        }
        return branchName
    }
}
