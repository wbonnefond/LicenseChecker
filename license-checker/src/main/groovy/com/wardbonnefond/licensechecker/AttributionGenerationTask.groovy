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

    @Input
    def Set<String> variantDependencies

    @TaskAction
    def generateLicenseAttributions(IncrementalTaskInputs inputs) {
        if (!inputs.isIncremental() && outputFile.exists()) {
            logger.debug("Licenses generation is not incremental; deleting output file")
            project.delete(outputFile)
        }
        if (!inputFile.exists()) {
            throw new GradleException("attributions file does not exist")
        }

        inputs.outOfDate { InputFileDetails change ->
            logger.info("$change.file.name has changed; regenerating attribution HTML file")
        }

        Set<String> dependencies = new HashSet<>(variantDependencies) // this is used after verifying attributions to create the output

        def configParser = new JsonParser()
        configParser.parse(inputFile)

        // First do some validation on the input
        Utils.jsonContainsDuplicates(configParser)

        // Check the attributions
        variantDependencies = Utils.checkAttributions(configParser, variantDependencies)

        // Check the excluded packages
        variantDependencies = Utils.checkExcludedPackages(configParser, variantDependencies)

        // Ensure no dependencies are still in the set
        Utils.ensureAllDependenciesAccountedFor(variantDependencies, logger, failOnMissingAttributions)

        // Create the ouput directory if it doesn't exist
        def assets = new File(project.projectDir, project.licenseChecker.outputFolder)
        if (!assets.exists()) {
            assets.mkdirs()
        }

        outputFile.text = Utils.buildHtmlOutput(configParser, dependencies)
    }
}