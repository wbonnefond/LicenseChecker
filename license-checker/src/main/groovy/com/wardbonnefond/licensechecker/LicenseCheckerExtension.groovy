package com.wardbonnefond.licensechecker

/**
 * Configuration values for the victor {} script block.
 */
class LicenseCheckerExtension {

    def outputFolder = 'src/main/assets'

    def outputFileName = "open_source_licenses.html"

    def inputFileName = 'attributions.json'

    def failOnMissingAttributions = false
}
