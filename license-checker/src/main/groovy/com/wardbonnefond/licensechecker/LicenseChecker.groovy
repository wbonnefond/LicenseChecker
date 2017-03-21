package com.wardbonnefond.licensechecker

import org.gradle.api.GradleException
import org.gradle.api.Plugin
import org.gradle.api.Project

class LicenseChecker implements Plugin<Project> {
    void apply(Project project) {
        project.extensions.create('licenseChecker', LicenseCheckerExtension)

        project.afterEvaluate {

            def variants = null
            if (project.android.hasProperty('applicationVariants')) {
                variants = project.android.applicationVariants
            }
            else if (project.android.hasProperty('libraryVariants')) {
                variants = project.android.libraryVariants
            }
            else {
                throw new GradleException('Android project must have applicationVariants or libraryVariants!')
            }

            variants.all { variant ->

                File variantOutputFile = new File("$project.buildDir/intermediates/assets/$flavorName/$buildType.name/" + project.licenseChecker.outputFileName)
                File variantInputFile = new File(project.projectDir, project.licenseChecker.inputFileName)
                def variantInputFileName = project.licenseChecker.inputFileName
                def variantFlavor = variant.flavorName
                def variantBuildType = variant.buildType.name

                if (!variantInputFile.exists()) {
                    throw new GradleException(variantInputFileName + " config file does not exist at location: " + variantInputFile.absolutePath)
                }

                def shouldFailOnMissingAttributions = false
                if (variant.getBuildType().hasProperty("failOnMissingAttributions")) {
                    shouldFailOnMissingAttributions = variant.getBuildType().failOnMissingAttributions
                }

                AttributionGenerationTask checkerTask = project.task("generate${variant.name.capitalize()}LicenseAttributions", type: AttributionGenerationTask) {
                    outputFile = variantOutputFile
                    inputFile = variantInputFile
                    failOnMissingAttributions = shouldFailOnMissingAttributions
                    variantDependencies = getVariantDependencies(project, variantBuildType, variantFlavor)
                }

                project.tasks.getByName("generate${variant.name.capitalize()}Assets").dependsOn(checkerTask)
            }
        }
    }

    /**
     * Get's the list of dependencies defined in the app's build.gradle file.
     * @return a Set<String> containing the package for every app dependency
     */
    Set<String> getVariantDependencies(project, buildType, flavor) {
        Set<String> dependenciesMap = new HashSet();

        project.configurations.each { conf ->

            if (Utils.isConfigurationForCurrentVariant(conf.name, buildType, flavor)) {
                conf.allDependencies.each { dep ->
                    String packageName = dep.group + ":" + dep.name;
                    if (!packageName.equals("null:unspecified")) {
                        if (packageName.startsWith("null:")) {
                            // If the lib was included from /libs folder
                            packageName = packageName.replace("null:", "")
                        }
                        dependenciesMap.add(packageName)
                    }
                }
            }
        }

        return dependenciesMap
    }
}
