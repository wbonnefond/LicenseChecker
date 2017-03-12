package com.wardbonnefond.licensechecker

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class LicenseChecker implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.create('licenseChecker', LicenseCheckerExtension)

        def variants = null
        if (project.android.hasProperty('applicationVariants')) {
            variants = project.android.applicationVariants
        }
        else if (project.android.hasProperty('libraryVariants')) {
            variants = project.android.libraryVariants
        }
        else {
            throw new IllegalStateException('Android project must have applicationVariants or libraryVariants!')
        }

        project.afterEvaluate {
            def assembleTask = getStartTask(project)
            // Only run the task if the current gradle task is 'assemble'
            if (assembleTask != null) {

                AttributionGenerationTask checkerTask = project.task("generateLicenseAttributions", type: AttributionGenerationTask)
                checkerTask.outputFile = new File(project.projectDir, project.licenseChecker.outputFolder + "/" + project.licenseChecker.outputFileName)
                def inputFile = new File(project.projectDir, project.licenseChecker.inputFileName)
                if (!inputFile.exists()) {
                    throw new GradleException("license.json config file does not exist at location: " + inputFile.absolutePath)
                }
                checkerTask.inputFile = inputFile

                // TODO: figure out a good way to handle something assemble{BuildType} with multiple flavors
                def currentVariant = variants.find { variant -> getVariantAssembleTask(variant).equals(assembleTask) }

                // Pull the extension from build.gradle if it was specified
                if (currentVariant != null && currentVariant.getBuildType().hasProperty("failOnMissingAttributions")) {
                    checkerTask.failOnMissingAttributions = currentVariant.getBuildType().failOnMissingAttributions
                }

                project.tasks.preBuild.dependsOn(checkerTask)
            }
        }
    }

    /**
     * Determines the gradle 'assemble' task that started this build.
     * @param project
     * @return the task name that started the build if it contains 'assemble' otherwise an empty string
     */
    def getStartTask(project) {
        def tasks = project.getGradle().getStartParameter().getTaskRequests().args.toString().replace("[", "").replace("]", "").split(",")
        return tasks.find { task -> task.contains("assemble") }
    }

    /**
     * Returns the 'assemble' task to build this variant as a string.
     * @param variant
     * @return the assemble task for the supplied variant
     */
    def getVariantAssembleTask(variant) {
        return variant.getAssemble().toString().replace("task", "").replace(" ", "").replace("\'", "")
    }

}
