#!/usr/bin/env groovy

import defra.pipeline.config.Config

def call(String colour, String message, String status, String channel, String branchName = "master") {

  def webhook = Config.getPropertyValue(channel, this)
  def notificationColour = Config.getPropertyValue(colour, this)

  echo("${notificationColour} ${webhook}")
  if (!env.BRANCH_NAME || "${BRANCH_NAME}" == branchName) {
    office365ConnectorSend(color: "${notificationColour}", message: message, status: status, webhookUrl: "${webhook}")
    echo("Notification sent to ${channel}")
  }
  else {
    echo("Notification not sent for branch ${branchName}")
  }
}
