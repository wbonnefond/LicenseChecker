package com.wardbonnefond.licensechecker

import org.gradle.api.GradleException

class Utils {

    static def checkAttributions(parser, dependencies) {
        parser.jsonConfig.licenses.each { k ->

            if (dependencies.contains(k.gradlePackage)) {
                dependencies.remove(k.gradlePackage)
            } else {
                throw new GradleException("Could not find " + k.gradlePackage + " in licenses config -- exiting")
            }
        }
        return dependencies
    }

    def static checkExcludedPackages(parser, dependencies) {
        parser.jsonConfig.excludedPackages.each { k ->

            if (dependencies.contains(k.gradlePackage)) {
                dependencies.remove(k.gradlePackage)
            } else {
                throw new GradleException("Excluded package: " + k.gradlePackage + " in licenses config but not a project dependency")
            }
        }
        return dependencies
    }

    def writeOutputFile(parser, directory, directoryParent) {
        // build the License file

        String finalHtml = new File(directoryParent, "/buildSrc/html-top").text
        String htmlBottom = new File(directoryParent, "/buildSrc/html-bottom").text

        String html = new File(directoryParent, "/buildSrc/individual-license-html").text

        parser.jsonConfig.licenses.each { k ->
            finalHtml += html.replace("{name}", k.name).replace("{author}", k.author).replace("{licenseText}", k.licenseText);
        }
        finalHtml += htmlBottom
        def assets = new File(directory, "/src/main/assets")
        if (!assets.exists()) {
            // Create all folders up-to and including B
            assets.mkdirs()
        }
        File file = new File(directory, "/src/main/assets/open_source_licenses.html")
        file.text = finalHtml
    }
}