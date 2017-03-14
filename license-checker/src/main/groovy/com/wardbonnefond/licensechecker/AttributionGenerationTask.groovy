package com.wardbonnefond.licensechecker

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs
import org.gradle.api.tasks.incremental.InputFileDetails

class AttributionGenerationTask extends DefaultTask {

    @InputFile
    def File inputFile

    @OutputFile
    def File outputFile

    @Input
    def boolean failOnMissingAttributions

    @TaskAction
    def generateLicenseAttributions(IncrementalTaskInputs inputs) {
        if (!inputs.isIncremental() && outputFile.exists()) {
            logger.debug("Licenses generation is not incremental; deleting output file")
            project.delete(outputFile)
        }
        if (!inputFile.exists()) {
            throw new GradleException("attributions.json does not exist")
        }

        logger.info("failOnMissingAttributions: " + failOnMissingAttributions)

        inputs.outOfDate { InputFileDetails change ->
            logger.info("$change.file.name has changed; regenerating attribution file")
        }

        Set<String> dependenciesMap = buildDependenciesMap()

        def configFile = new File(project.projectDir, project.licenseChecker.inputFileName);
        if (!configFile.exists()) {
            throw new GradleException("attributions.json does not exist")
        }
        def configParser = new JsonParser()
        configParser.parse(configFile)

        // First do some validation on the input
        Utils.jsonContainsDuplicates(configParser)

        // Check the attributions
        dependenciesMap = Utils.checkAttributions(configParser, dependenciesMap, failOnMissingAttributions)

        // Check the excluded packages
        dependenciesMap = Utils.checkExcludedPackages(configParser, dependenciesMap, failOnMissingAttributions)

        // Ensure no dependencies are still in the set
        Utils.ensureAllDependenciesAccountedFor(dependenciesMap, logger, failOnMissingAttributions)

        // build the HTML file
        String finalHtml = Utils.BASE_HTML;

        String html = Utils.INDIVIDUAL_HTML;
        StringBuilder sb = new StringBuilder();

        configParser.jsonConfig.libraries.each { k ->
            sb.append(html.replace("{name}", k.name).replace("{legalText}", k.legalText));
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