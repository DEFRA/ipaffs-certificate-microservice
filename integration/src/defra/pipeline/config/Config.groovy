package defra.pipeline.config

import groovy.json.JsonSlurperClassic

class Config {

    /**
     * Get a config item value from the pipeline settings file
     *
     * @param key     The property name
     * @param script  The global script parameter
     * @return The value of the property
     */
    public static String getPropertyValue(String key, Script script) {
        def config = script.libraryResource('settings/pipeline-properties.config')
        Properties properties = new Properties()
        properties.load(new StringReader(config))
        String value = properties.getProperty(key)
        return value
    }

    /**
     * Get an ARM template returns as JSON
     *
     * @param serviceName   The name of the service
     * @param subscription  The subscription, e.g. Sandpit
     * @return JSON representation of the ARM template
     */
    public static Object getParameters(String serviceName, String subscription, Script script) {
        def parametersJson = script.libraryResource "configuration/imports/web_app_services/parameters/${subscription.toLowerCase()}/${serviceName}.parameters.json"
        def jsonSlurper = new JsonSlurperClassic()
        return jsonSlurper.parseText(parametersJson)
    }
}

