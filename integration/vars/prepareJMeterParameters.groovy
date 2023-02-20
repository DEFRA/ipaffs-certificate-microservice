import defra.pipeline.vault.VaultKey

String call(Map parameters, String template) {
    if (template == null) {
        throw new IllegalArgumentException("Template content is not set")
    }

    String populatedTemplate = template

    getSecuredParameters().each {
        populatedTemplate = replace(populatedTemplate, it.key, it.value)
    }

    parameters.each {
        populatedTemplate = replace(populatedTemplate, String.valueOf(it.key), String.valueOf(it.value))
    }

    // remove all unpopulated fields
    populatedTemplate = populatedTemplate.replaceAll('\\n.+: \\$\\{.+}', '')

    return populatedTemplate
}

String replace(String target, String key, String value) {
    return target.replaceAll('\\$\\{' + key + '}', value)
}

def getSecuredParameters() {
    return [
            'DYNAMICS_CLIENT_ID'          : VaultKey.getSecuredValue('dynamicsClientId', this),
            'DYNAMICS_CLIENT_SECRET'      : VaultKey.getSecuredValue('dynamicsClientSecret', this),
            'DYNAMICS_TENANT_ID'          : VaultKey.getSecuredValue('dynamicsTenantId', this),
            'NOTIFIER_PASSWORD'           : VaultKey.getSecuredValue('performanceTestsNotifierPassword', this),
            'MULTI_USER_NOTIFIER_PASSWORD': VaultKey.getSecuredValue('performanceTestsMultiUserNotifierPassword', this),
            'AGENT_PASSWORD'              : VaultKey.getSecuredValue('performanceTestsAgentPassword', this),
            'INSPECTOR_PASSWORD'          : VaultKey.getSecuredValue('performanceTestsInspectorPassword', this),
            'BASIC_AUTH_PASSWORD'         : VaultKey.getSecuredValue('performanceTestsBasicAuthPassword', this)
    ]
}
