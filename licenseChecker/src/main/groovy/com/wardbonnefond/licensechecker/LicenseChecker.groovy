package com.wardbonnefond.licensechecker

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.FileCollection
import org.gradle.api.invocation.Gradle

import java.util.regex.Matcher
import java.util.regex.Pattern

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
            checkerTask.inputFile = new File(project.projectDir, project.licenseChecker.inputFileName)
            def shouldFail = false

            def assembleTask = getStartTask(project)
            println(assembleTask)

            variants.all { variant ->
                if (getVariantAssembleTask(variant).equals(assembleTask)) {
                    println("${variant.name.capitalize()}")
                    if (variant.getBuildType().hasProperty("myFoo")) {
                        println(variant.getBuildType().myFoo)
                        shouldFail = variant.getBuildType().myFoo
                    }

                }
            }

            checkerTask.failOnMissingAttributions = shouldFail

            println("failOnMissingAttributions: " + shouldFail)

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

            checkerTask.dependenciesMapNew = dependenciesMap

            println("Output File Name: " + project.licenseChecker.outputFileName)
            println("Input File Name: " + project.licenseChecker.inputFileName)

            println("Output File: " + checkerTask.outputFile.absolutePath)
            println("Input File: " + checkerTask.inputFile.absolutePath)

            project.tasks.preBuild.dependsOn(checkerTask)
        }

    }


    def getStartTask(project) {
        def tasks = project.getGradle().getStartParameter().getTaskRequests().args.toString().replace("[","").replace("]", "").split(",")
        def finalTask
        tasks.each { task ->
            println("task: " + task)
            if (task.contains("assemble")) {
                println("contains")
                finalTask = task
            }
        }
        return finalTask
    }

    def getVariantAssembleTask(variant) {
        return variant.getAssemble().toString().replace("task", "").replace(" ", "").replace("\'", "")
    }

}
