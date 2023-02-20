#!/usr/bin/env groovy
import defra.pipeline.names.PoolTag

def call(String resourceGroupName, String buildNumber, String branch) {
    // defaults taken from Jenkinsfile_run_jmeter in the test repo
    String NUMBER_OF_THREADS = '1'
    String TEST_ITERATIONS = '1'
    String WAIT = '0'
    String RAMP_UP_PERIOD = '0'
    boolean DECISION = true
    boolean CONTROL = true
    boolean CHEDPP_E2E = false
    boolean DYNAMICS_TESTS_ENABLED = false
    int DYNAMICS_WAIT_TIME = 120
    String DYNAMICS_MAX_RETRIES = '3'
    String DYNAMICS_URL = 'defra-trade-snd-plants.crm4.dynamics.com'
    String PROXY_URL = 'https://imports-proxy.azurewebsites.net'
    String USER_TYPE = 'B2B'
    String TARGET = 'imports-proxy.azurewebsites.net'
    String IDM_DOMAIN = 'idm-dev-public.azure.defra.cloud'
    String POOL = PoolTag.getId(resourceGroupName)

    //Scripts
    String CHED_SCRIPT = 'imports-performance-test.jmx'
    String CHEDPP_SCRIPT = 'imports-performance-test-chedpp.jmx'
    String EU_IMP_SCRIPT = 'imports-performance-test-eu-imp.jmx'
    String BORDER_NOTIFICATIONS_SCRIPT = 'imports-performance-test-border-notifications.jmx'
    String RE_ENFORCED_CHECK_SCRIPT = 'imports-performance-test-re-enforced-check.jmx'

    Map<String, Object> chedJMeterRun = [
            'parameters': [
                    'POOL'             : POOL,
                    'USER_TYPE'        : USER_TYPE,
                    'IDM_DOMAIN'       : IDM_DOMAIN,
                    'NUMBER_OF_THREADS': NUMBER_OF_THREADS,
                    'TEST_ITERATIONS'  : TEST_ITERATIONS,
                    'WAIT'             : WAIT,
                    'RAMP_UP_PERIOD'   : RAMP_UP_PERIOD,
                    'DECISION'         : DECISION,
                    'CONTROL'          : CONTROL,
                    'HIGH_RISK_EU_CHED': false,
                    'TARGET'           : TARGET,
                    'SCRIPT'           : CHED_SCRIPT,
            ],
            'name'      : 'non-EU CHED notification',
            'file'      : 'imports-performance-test-ched.yml',
            'report'    : "report-ched-${buildNumber}.xml"
    ]
    Map<String, Object> chedppDOAJMeterRun = [
            'parameters': [
                    'POOL'                  : POOL,
                    'USER_TYPE'             : 'B2C',
                    'DOA_JOURNEY'           : true,
                    'CHEDPP_E2E'            : CHEDPP_E2E,
                    'IDM_DOMAIN'            : IDM_DOMAIN,
                    'NUMBER_OF_THREADS'     : NUMBER_OF_THREADS,
                    'TEST_ITERATIONS'       : TEST_ITERATIONS,
                    'WAIT'                  : WAIT,
                    'RAMP_UP_PERIOD'        : RAMP_UP_PERIOD,
                    'DYNAMICS_TESTS_ENABLED': DYNAMICS_TESTS_ENABLED,
                    'DYNAMICS_WAIT_TIME'    : DYNAMICS_WAIT_TIME * 1000,
                    'DYNAMICS_MAX_RETRIES'  : DYNAMICS_MAX_RETRIES,
                    'DYNAMICS_URL'          : DYNAMICS_URL,
                    'TARGET'                : TARGET,
                    'SCRIPT'                : CHEDPP_SCRIPT,
            ],
            'name'      : 'CHEDPP with DOA notification',
            'file'      : 'imports-performance-test-chedpp-doa.yml',
            'report'    : "report-chedpp-doa-${buildNumber}.xml"
    ]
    Map<String, Object> chedppBulkUploadJMeterRun = [
            'parameters': [
                    'POOL'                  : POOL,
                    'USER_TYPE'             : USER_TYPE,
                    'CHEDPP_BULK_UPLOAD'    : true,
                    'CHEDPP_E2E'            : CHEDPP_E2E,
                    'IDM_DOMAIN'            : IDM_DOMAIN,
                    'NUMBER_OF_THREADS'     : NUMBER_OF_THREADS,
                    'TEST_ITERATIONS'       : TEST_ITERATIONS,
                    'WAIT'                  : WAIT,
                    'RAMP_UP_PERIOD'        : RAMP_UP_PERIOD,
                    'DYNAMICS_TESTS_ENABLED': DYNAMICS_TESTS_ENABLED,
                    'DYNAMICS_WAIT_TIME'    : DYNAMICS_WAIT_TIME * 1000,
                    'DYNAMICS_MAX_RETRIES'  : DYNAMICS_MAX_RETRIES,
                    'DYNAMICS_URL'          : DYNAMICS_URL,
                    'TARGET'                : TARGET,
                    'SCRIPT'                : CHEDPP_SCRIPT,
            ],
            'name'      : 'CHEDPP bulk upload',
            'file'      : 'imports-performance-test-chedpp-bulk-upload.yml',
            'report'    : "report-chedpp-bulk-upload-${buildNumber}.xml"
    ]
    Map<String, Object> euImpKnownJMeterRun = [
            'parameters': [
                    'POOL'                  : POOL,
                    'USER_TYPE'             : USER_TYPE,
                    'IDM_DOMAIN'            : IDM_DOMAIN,
                    'NUMBER_OF_THREADS'     : NUMBER_OF_THREADS,
                    'TEST_ITERATIONS'       : TEST_ITERATIONS,
                    'WAIT'                  : WAIT,
                    'RAMP_UP_PERIOD'        : RAMP_UP_PERIOD,
                    'KNOWN_COMMODITY'       : true,
                    'DYNAMICS_TESTS_ENABLED': DYNAMICS_TESTS_ENABLED,
                    'DYNAMICS_WAIT_TIME'    : DYNAMICS_WAIT_TIME * 1000,
                    'DYNAMICS_MAX_RETRIES'  : DYNAMICS_MAX_RETRIES,
                    'TARGET'                : TARGET,
                    'SCRIPT'                : EU_IMP_SCRIPT,
            ],
            'name'      : 'Import notification - Known commodity',
            'file'      : 'imports-performance-test-eu-imp-known.yml',
            'report'    : "report-eu-imp-known-commodity-${buildNumber}.xml"
    ]
    Map<String, Object> euImpUnknownJMeterRun = [
            'parameters': [
                    'POOL'                  : POOL,
                    'USER_TYPE'             : USER_TYPE,
                    'IDM_DOMAIN'            : IDM_DOMAIN,
                    'NUMBER_OF_THREADS'     : NUMBER_OF_THREADS,
                    'TEST_ITERATIONS'       : TEST_ITERATIONS,
                    'WAIT'                  : WAIT,
                    'RAMP_UP_PERIOD'        : RAMP_UP_PERIOD,
                    'KNOWN_COMMODITY'       : false,
                    'DYNAMICS_TESTS_ENABLED': DYNAMICS_TESTS_ENABLED,
                    'DYNAMICS_WAIT_TIME'    : DYNAMICS_WAIT_TIME * 1000,
                    'DYNAMICS_MAX_RETRIES'  : DYNAMICS_MAX_RETRIES,
                    'TARGET'                : TARGET,
                    'SCRIPT'                : EU_IMP_SCRIPT,
            ],
            'name'      : 'Import notification - unknown commodity',
            'file'      : 'imports-performance-test-eu-imp-unknown.yml',
            'report'    : "report-eu-imp-unknown-commodity-${buildNumber}.xml"
    ]
    Map<String, Object> borderJMeterRun = [
            'parameters': [
                    'POOL'             : POOL,
                    'USER_TYPE'        : USER_TYPE,
                    'IDM_DOMAIN'       : IDM_DOMAIN,
                    'NUMBER_OF_THREADS': NUMBER_OF_THREADS,
                    'TEST_ITERATIONS'  : TEST_ITERATIONS,
                    'WAIT'             : WAIT,
                    'RAMP_UP_PERIOD'   : RAMP_UP_PERIOD,
                    'TARGET'           : TARGET,
                    'SCRIPT'           : BORDER_NOTIFICATIONS_SCRIPT,
            ],
            'name'      : 'Border notifications',
            'file'      : 'imports-performance-test-border.yml',
            'report'    : "report-border-notifications-${buildNumber}.xml"
    ]
    Map<String, Object> reEnforcedCheckJMeterRun = [
            'parameters': [
                    'POOL'             : POOL,
                    'USER_TYPE'        : 'B2B',
                    'IDM_DOMAIN'       : IDM_DOMAIN,
                    'NUMBER_OF_THREADS': NUMBER_OF_THREADS,
                    'TEST_ITERATIONS'  : TEST_ITERATIONS,
                    'WAIT'             : WAIT,
                    'RAMP_UP_PERIOD'   : RAMP_UP_PERIOD,
                    'PROXY_URL'        : PROXY_URL,
                    'TARGET'           : TARGET,
                    'SCRIPT'           : RE_ENFORCED_CHECK_SCRIPT,
            ],
            'name'      : 'Re-enforced Check',
            'file'      : 'imports-performance-test-check.yml',
            'report'    : "report-re-enforced-check-${buildNumber}.xml"
    ]

    List<Map<String, Object>> runs = [chedJMeterRun, chedppDOAJMeterRun, chedppBulkUploadJMeterRun, euImpKnownJMeterRun, euImpUnknownJMeterRun, borderJMeterRun, reEnforcedCheckJMeterRun]

    withCredentials([string(credentialsId: 'JENKINS_GITLAB_TOKEN', variable: 'TOKEN')]) {
        runs.each {
            Map<String, Object> parameters = it['parameters'] as Map<String, Object>
            String populateTemplate = it['file']
            String reportName = it['report']
            String testName = it['name']
            echo "====== RUNNING JMETER: ${testName} FRONTEND DOMAIN: ${TARGET} POOL: ${POOL} FILE: ${populateTemplate} with script ${parameters['SCRIPT']} ====="
            catchError {
                testJmeter(parameters, 'imports-performance-test.yml', populateTemplate, branch, "${TOKEN}")
            }
            sh "mv report.xml ${reportName}"
            perfReport reportName
            if (currentBuild.currentResult == 'FAILURE') {
                error("JMeter test: ${testName} failed")
            }
        }
    }
}
