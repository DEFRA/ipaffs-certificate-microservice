#!/usr/bin/env groovy

/**
 * Download Application Insights Agent. Version sourced from pipeline-propeties.
 */

import defra.pipeline.config.Config

def call() {

    def applicationInsightsVersion = Config.getPropertyValue("applicationInsightsVersion", this)

  sh(script: "wget --no-clobber https://github.com/microsoft/ApplicationInsights-Java/releases/download/${applicationInsightsVersion}/applicationinsights-agent-${applicationInsightsVersion}.jar --directory-prefix /tmp")
}
