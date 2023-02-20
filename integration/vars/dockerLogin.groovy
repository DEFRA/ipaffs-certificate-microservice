#!/usr/bin/env groovy

import defra.pipeline.config.Config
import defra.pipeline.vault.VaultKey

def call(String subscription) {
    def vaultName = Config.getPropertyValue("azureKeyVault", this)
    def acrName = ""

    if ( subscription == "Sandpit" || subscription == "SandpitASEv2" ) {
        acrName = Config.getPropertyValue("azureSndContainerRegistry", this)
        acrUser = Config.getPropertyValue("azureSndContainerRepository", this)
        acrPassword = 'azureSndContainerRegistryPassword'
    } else {
        acrName = Config.getPropertyValue("azureOpsContainerRegistry", this)
        acrUser = Config.getPropertyValue("azureOpsContainerRepository", this)
        acrPassword = 'azureOpsContainerRegistryPassword'
    }

    sh(script: "(az keyvault secret show --vault-name ${vaultName} --name ${acrPassword} --query value -o tsv) | docker login ${acrName} -u ${acrUser} --password-stdin")
}
