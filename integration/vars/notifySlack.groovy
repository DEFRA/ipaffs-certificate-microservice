#!/usr/bin/env groovy

import defra.pipeline.config.Config

def call(String message) {
  call(message, "#BDFFC3")
}

def call(String message, String colour, String branchName = "master") {

  def channel = Config.getPropertyValue('slackChannel', this)
  if (!env.BRANCH_NAME || "${BRANCH_NAME}" == branchName) {
    slackSend(color: colour, message: message, channel: "#${channel}" )
    echo('Slack notification sent')
  }
  else {
    echo("Notification not sent for branch ${branchName}")
  }

}
