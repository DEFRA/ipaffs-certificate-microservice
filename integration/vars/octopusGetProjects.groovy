def call(String octopusApiKey, String octopusProjects) {
    sh(script: """
        mkdir octopus_projects_and_variables
        for project in ${octopusProjects}
        do
          docker run --rm -v \$(pwd):\$(pwd) -w \$(pwd) 'octopusdeploy/octo:7.4.3424' export --apiKey ${octopusApiKey} --type project --name \$project --server 'https://octopus-ops.azure.defra.cloud' --filePath ./octopus_projects_and_variables/\${project}.json
          echo "Downloading json definition of Octopus Project: \$project"
        done
        export sets=\$(cat octopus_projects_and_variables/*.json | grep '"Id": "LibraryVariableSets' | cut -f4 -d \\" | sort | uniq)
        echo "Library Variable Sets used across all projects:"
        echo \$sets
        for set in \$sets
        do
          echo "Downloading json definition of Octopus Variables Set: \$set"
          curl -H "X-Octopus-ApiKey: ${octopusApiKey}" --request GET https://octopus-ops.azure.defra.cloud/api/variables/variableset-\$set > ./octopus_projects_and_variables/\$set.json
          echo "Downloaded definition of Octopus Variables Set: \$set to a file: \$set.json"
        done
        rm -rf resources/configuration/imports/octopus_projects_and_variables
        mv octopus_projects_and_variables resources/configuration/imports/
        """)
}
