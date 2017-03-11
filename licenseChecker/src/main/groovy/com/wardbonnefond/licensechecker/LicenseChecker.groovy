package com.wardbonnefond.licensechecker

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
            AttributionGenerationTask checkerTask = project.task("generateLicenseAttributions", type: AttributionGenerationTask)
            checkerTask.outputFile = new File(project.projectDir, project.licenseChecker.outputFolder + "/" + project.licenseChecker.outputFileName)
            println(checkerTask.outputFile.absolutePath)
            println(project.licenseChecker.inputFile)
            println(project.licenseChecker.outputFileName)

            project.tasks.preBuild.dependsOn(checkerTask)
        }

    }
}
