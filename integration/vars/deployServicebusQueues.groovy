/**
 * Deploy the service bus queues for local dev or sandpit
 *
 * @param queues         The queue that should be deployed
 *
 */

import defra.pipeline.config.Config
import defra.pipeline.azure.AzureActions
import defra.pipeline.script.ScriptActions
import hudson.AbortException

def call(String queues, Script script) {

    if (queues == "sandpit") {
      script.getFile 'configuration/imports/servicebus/templates/service-bus.template.json'
      script.getFile 'configuration/imports/servicebus/parameters/sandpit/service-bus.parameters.json'

      try {
        script.getFile 'configuration/imports/servicebus/templates/local-dev-queues.json'
        def scriptToRun = """ \
          az deployment group create -g SNDIMPINFRGP001 --template-file configuration/imports/servicebus/templates/service-bus.template.json --parameters  configuration/imports/servicebus/parameters/sandpit/service-bus.parameters.json
          """.trim()
        ScriptActions.runCommandLogOnlyOnError(scriptToRun, script)
        script.echo("DEPLOYED Servicebus Queues TO ${queues}")
      } catch (Exception ex) {
        script.echo(ex.getMessage())
        throw new AbortException("Failed to deploy Service bus: ${queues}")
      }
    } else {
      try {
        script.getFile 'configuration/imports/servicebus/templates/local-dev-queues.json'
        def scriptToRun = """ \
          az deployment group create -g SNDIMPINFRGP001 --template-file configuration/imports/servicebus/templates/local-dev-queues.json --parameters serviceBusNamespaceName=SNDIMPINFSBS002-dev
          """.trim()
        ScriptActions.runCommandLogOnlyOnError(scriptToRun, script)
        script.echo("DEPLOYED Servicebus Queues TO ${queues}")
      } catch (Exception ex) {
        script.echo(ex.getMessage())
        throw new AbortException("Failed to deploy Service bus: ${queues}")
      }
    }
}
