package com.wardbonnefond.licensechecker

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs

class AttributionGenerationTask extends DefaultTask {

    @OutputFile
    def File outputFile

    @TaskAction
    def generateLicenseAttributions(IncrementalTaskInputs inputs) {
        if (!inputs.isIncremental() && outputFile.exists()) {
            project.delete(outputFile)
        }

        Set<String> dependenciesMap = buildDependenciesMap()

        def configFile = new File(project.projectDir, project.licenseChecker.inputFile);
        println(configFile.absolutePath)
        def configParser = new JsonParser()
        configParser.parse(configFile)

        // Check the attributions
        dependenciesMap = Utils.checkAttributions(configParser, dependenciesMap)

        // Check the excluded packages
        dependenciesMap = Utils.checkExcludedPackages(configParser, dependenciesMap)

        // Ensure no dependencies are still in the set
        Utils.ensureAllDependenciesAccountedFor(dependenciesMap, logger)

        // build the HTML file
        String finalHtml = new File(project.parent.projectDir, "/licenseChecker/base-html").text

        String html = new File(project.parent.projectDir, "/licenseChecker/individual-license-html").text
        StringBuilder sb = new StringBuilder();

        configParser.jsonConfig.licenses.each { k ->
            sb.append(html.replace("{name}", k.name).replace("{author}", k.author).replace("{licenseText}", k.licenseText));
        }

        def assets = new File(project.projectDir, project.licenseChecker.outputFolder)
        if (!assets.exists()) {
            assets.mkdirs()
        }

        outputFile.text = finalHtml.replace("{attributions}", sb.toString())
    }

    Set<String> buildDependenciesMap() {
        Set<String> dependenciesMap = new HashSet();

        project.configurations.each { conf ->
            if (conf.name.equals("compile")) {
                conf.allDependencies.each { dep ->
                    String packageName = dep.group + ":" + dep.name;
                    if (!packageName.equals("null:unspecified")) {
                        dependenciesMap.add(packageName)
                    }
                }
            }
        }

        return dependenciesMap
    }


}