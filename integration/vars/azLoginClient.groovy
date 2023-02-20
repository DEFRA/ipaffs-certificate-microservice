#!/usr/bin/env groovy

/**
 * Perform the Azure login step
 */

import defra.pipeline.config.Config

def call() {

  def azureSubscriptionId = Config.getPropertyValue("azureSubscriptionId", this)
  def azureClientId = Config.getPropertyValue("azureClientId", this)
  def azureTenantId = Config.getPropertyValue("azureTenantId", this)
  def jenkinsCredentialsRepository = Config.getPropertyValue("jenkinsCredentialsRepository", this)

  withCredentials([azureServicePrincipal(jenkinsCredentialsRepository)]) {
      sh(script: "az login --service-principal -u ${azureClientId} -p ${AZURE_CLIENT_SECRET} -t ${azureTenantId}")
  }

  sh(script: "az account set -s ${azureSubscriptionId}")
}