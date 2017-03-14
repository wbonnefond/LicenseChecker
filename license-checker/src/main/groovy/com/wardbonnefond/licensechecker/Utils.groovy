package com.wardbonnefond.licensechecker

import org.gradle.api.GradleException

class Utils {

    static def jsonContainsDuplicates(parser) {
        Set<String> packagesInJsonFile = new HashSet<>();
        parser.jsonConfig.libraries.each { k ->

            if (packagesInJsonFile.contains(k.gradlePackage)) {
                throw new GradleException(k.gradlePackage + " declared more than once in attributions.json")
            }
            else {
                packagesInJsonFile.add(k.gradlePackage)
            }
        }
        Set<String> excludedPackagesInJsonFile = new HashSet<>();
        parser.jsonConfig.excludedLibraries.each { k ->

            if (excludedPackagesInJsonFile.contains(k.gradlePackage)) {
                throw new GradleException(k.gradlePackage + " declared more than once in attributions.json")
            }
            else {
                excludedPackagesInJsonFile.add(k.gradlePackage)
            }
        }
        packagesInJsonFile.each { gradlePackage ->
            if (excludedPackagesInJsonFile.contains(gradlePackage)) {
                throw new GradleException(gradlePackage + " declared more than once in attributions.json")
            }
        }
        return false
    }

    static def checkAttributions(parser, dependencies, failOnMissingAttributions) {
        parser.jsonConfig.libraries.each { k ->

            if (dependencies.contains(k.gradlePackage)) {
                dependencies.remove(k.gradlePackage)
            }
            else {
                if (failOnMissingAttributions) {
                    throw new GradleException("Could not find " + k.gradlePackage + " in licenses.json")
                }
            }
        }
        return dependencies
    }

    def static checkExcludedPackages(parser, dependencies, failOnMissingAttributions) {
        parser.jsonConfig.excludedLibraries.each { k ->

            if (dependencies.contains(k.gradlePackage)) {
                dependencies.remove(k.gradlePackage)
            }
            else {
                if (failOnMissingAttributions) {
                    throw new GradleException("Excluded package: " + k.gradlePackage + " in attributions.json but not a project dependency")
                }
            }
        }
        return dependencies
    }

    def static ensureAllDependenciesAccountedFor(dependencies, logger, failOnMissingAttributions) {
        if (dependencies.isEmpty()) {
            logger.info("All dependencies accounted for in attributions.json")
            return true;

        }
        else {
            def count = 0
            dependencies.each { dep ->
                def text = dep + " was missing from the attributions.json"
                if (failOnMissingAttributions) {
                    logger.error(text)
                }
                else {
                    logger.debug(text)
                }
                count++;
            }
            if (failOnMissingAttributions) {
                throw new GradleException(count + " packages missing from attributions.json")
            }
        }

    }

    def static isTaskBuildingMultipleVariants(task, buildTypes, productFlavors) {
        def taskRemoved = task.toLowerCase().replace("assemble", "")
        if (taskRemoved.isEmpty()) {
            return task;
        }
        def foundBuildType = buildTypes.find { buildType ->
            if (buildType.equals(taskRemoved)) {
                return buildType
            }
        }
        if (foundBuildType != null) {
            return foundBuildType;
        }
        def foundProductFlavor = productFlavors.find { productFlavor ->
            if (productFlavor.equals(taskRemoved)) {
                return productFlavor
            }
        }
        if (foundProductFlavor != null) {
            return foundProductFlavor;
        }
        return "";

    }

    def static BASE_HTML = "<html>\n" +
            "<head>\n" +
            "    <style>\n" +
            "        body {\n" +
            "        font-family: sans-serif;\n" +
            "        }\n" +
            "        pre {\n" +
            "        background-color: #eeeeee;\n" +
            "        padding: 1em;\n" +
            "        white-space: pre-wrap;\n" +
            "        }\n" +
            "    </style>\n" +
            "</head>\n" +
            "<body>\n" +
            "{attributions}\n" +
            "</body>\n" +
            "</html>";

    def static INDIVIDUAL_HTML = "<b>{name}</b>\n" +
            "<br/>\n" +
            "<pre>\n" +
            "{legalText}\n" +
            "</pre>";
}