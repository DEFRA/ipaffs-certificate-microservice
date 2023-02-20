package defra.pipeline.names

import defra.pipeline.BaseTest

import org.junit.Test

class BranchesTest extends BaseTest {

    @Test
    public void testGetBranchPrefixForNormalFeatureBranch() {
        def result = Branches.getBranchPrefix("feature/IMTA-1234-test-branch")
        assert result == "imta-1234"
    }

    @Test
    public void testGetBranchPrefixForBadlyNamedBranch() {
        def result = Branches.getBranchPrefix("my-badly-named-branch")
        assert result == "my-badly-named-branch"
    }

    @Test
    public void testGetBranchPrefixForBadlyNamedBranch2() {
        def result = Branches.getBranchPrefix("my/badLy-n4med-branch")
        assert result == "my-badly-n4med-branch"
    }

    @Test
    public void testGetNameAndTagForMasterAndSandpit() {
        def result = Branches.getBranchPrefix("master")
        assert result == "master"
    }

}
