package defra.pipeline.vault

import defra.pipeline.config.Config
import defra.pipeline.azure.AzureQueries

class VaultKey {

    public static String getSecuredValue(String name, Script script) {

        def vaultName = Config.getPropertyValue("azureKeyVault", script)

        return AzureQueries.runCachedQueryForAzCli("az keyvault secret show --vault-name ${vaultName} --name ${name} --query value -o tsv", false, script).trim() as String

    }

}
