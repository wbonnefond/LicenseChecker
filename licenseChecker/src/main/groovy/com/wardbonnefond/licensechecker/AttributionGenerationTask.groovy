package com.wardbonnefond.licensechecker

import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.incremental.IncrementalTaskInputs

class AttributionGenerationTask extends DefaultTask {

    @OutputFile
    def File outputFile

    @TaskAction
    def generateLicenseAttributions(IncrementalTaskInputs inputs) {
        println inputs.incremental ? "CHANGED inputs considered out of date"
                : "ALL inputs considered out of date"
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
        configParser.jsonConfig.excludedPackages.each { k ->

            if (dependenciesMap.contains(k.gradlePackage)) {
                dependenciesMap.remove(k.gradlePackage)
            } else {
                throw new GradleException("Excluded package: " + k.gradlePackage + " in licenses config but not a project dependency")
            }
        }

        if (dependenciesMap.isEmpty()) {
            println("All dependencies accounted for in licenses config")

        } else {
            dependenciesMap.each { dep ->
                println(dep + " was missing from the licenses config")
            }
        }

        // build the License file
        String finalHtml = new File(project.parent.projectDir, "/licenseChecker/html-top").text
        String htmlBottom = new File(project.parent.projectDir, "/licenseChecker/html-bottom").text

        String html = new File(project.parent.projectDir, "/licenseChecker/individual-license-html").text


        configParser.jsonConfig.licenses.each { k ->
            finalHtml += html.replace("{name}", k.name).replace("{author}", k.author).replace("{licenseText}", k.licenseText);
        }
        finalHtml += htmlBottom
        def assets = new File(project.projectDir, project.licenseChecker.outputFolder)
        if (!assets.exists()) {
            // Create all folders up-to and including B
            assets.mkdirs()
        }
        //File file = outputFile
        outputFile.text = finalHtml
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