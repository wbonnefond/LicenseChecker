package com.wardbonnefond.licensechecker

import groovy.json.JsonSlurper
import org.gradle.api.logging.Logging
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class JsonParser {

    private static Logger logger = LoggerFactory.getLogger(JsonParser.class);
    private static String logPrefix = logger.getName()

    def slurper, jsonConfig

    JsonParser() {

        this.slurper = new JsonSlurper()
    }

    def parse(File jsonFile) {

        logger.info(Logging.LIFECYCLE, "$logPrefix parsing: $jsonFile")
        this.jsonConfig = slurper.parse(jsonFile)
    }
}