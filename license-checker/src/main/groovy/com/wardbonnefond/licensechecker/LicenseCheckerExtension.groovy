package com.wardbonnefond.licensechecker

/**
 * Configuration values for the victor {} script block.
 */
class LicenseCheckerExtension {

    private final static DEFAULT_OUTPUT_FOLDER = 'src/main/assets'
    private final static DEFAULT_INPUT_FILE = 'licenses.json'

    def outputFolder = 'src/main/assets'

    def outputFileName = "open_source_licenses.html"

    def inputFileName = 'licenses.json'

    def failOnMissingAttributions = false
}
