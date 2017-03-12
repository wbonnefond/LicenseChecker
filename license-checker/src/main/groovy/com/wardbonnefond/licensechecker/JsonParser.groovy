package com.wardbonnefond.licensechecker

import groovy.json.JsonSlurper

class JsonParser {

    def slurper, jsonConfig

    JsonParser() {
        this.slurper = new JsonSlurper()
    }

    def parse(File jsonFile) {
        this.jsonConfig = slurper.parse(jsonFile)
    }
}