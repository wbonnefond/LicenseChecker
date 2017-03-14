package com.wardbonnefond.licensechecker

import org.gradle.api.GradleException

class Utils {

    /**
     * Determines if the input file contains any duplicate package names. Helps protect against copy-paste errors when first setting up the
     * attributions.json file
     * @param parser the parser for the input file
     * @return false if it contains no duplicates, throws and exception if it does
     */
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

    /**
     * Checks that each dependency for the app is listed in the input json file.
     * @param parser the parser for the input file
     * @param dependencies a Set<String> of the package names for the apps dependencies
     * @param failOnMissingAttributions whether this check should throw an exception if there are missing attributions in the input file
     * @return
     */
    static def checkAttributions(parser, dependencies, failOnMissingAttributions) {
        parser.jsonConfig.libraries.each { k ->

            if (dependencies.contains(k.gradlePackage)) {
                dependencies.remove(k.gradlePackage)
            }
            else {
                if (failOnMissingAttributions) {
                    throw new GradleException("Could not find " + k.gradlePackage + " in attributions.json")
                }
            }
        }
        return dependencies
    }

    /**
     * Checks that each excluded package in the input file is a project dependency
     * @param parser the parse for the input file
     * @param dependencies a Set<String> of the package names for the apps dependencies
     * @param failOnMissingAttributions whether this check should throw an exception if there are attributions in the input file
     * @return
     */
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

    def static buildHtmlOutput(parser) {
        String html = INDIVIDUAL_HTML;
        StringBuilder sb = new StringBuilder();

        parser.jsonConfig.libraries.each { k ->
            sb.append(html.replace("{name}", k.name).replace("{legalText}", k.legalText));
        }

        return BASE_HTML.replace("{attributions}", sb.toString())
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
            "{attributions}" +
            "</body>\n" +
            "</html>";

    def static INDIVIDUAL_HTML = "<b>{name}</b>\n" +
            "<br/>\n" +
            "<pre>\n" +
            "{legalText}\n" +
            "</pre>\n";
}