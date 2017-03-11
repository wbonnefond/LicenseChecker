package com.wardbonnefond.licensechecker

import org.gradle.api.GradleException
import org.junit.Test
import org.slf4j.LoggerFactory

import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertTrue


class UtilsTest {

    private final static RESOURCE_PATH = 'src/test/input/'

    /**
     *  Can't figure out what is happening, but if tests run during project build
     *  it has /licenseChecker in the relative path, if you run the tests manually
     *  from AS it doesn't. This will add the folder to the path if needed
     * @return
     */
    private File getJsonConfigFileForTests(fileName) {
        def jsonConfigFile = new File(RESOURCE_PATH, fileName)
        if (!jsonConfigFile.exists()) {
            jsonConfigFile = new File('licenseChecker/' + RESOURCE_PATH, fileName)
        }
        return jsonConfigFile
    }

    @Test
    void testRemovesAttributedPackagesFromSet() {
        Set<String> dependencies = new HashSet<>();
        dependencies.add("io.reactivex.rxjava2:rxjava")
        dependencies.add("com.github.bumptech.glide:glide")
        dependencies.add("com.android.support:appcompat-v7")
        dependencies.add("com.android.support:support-v4")

        def jsonConfigFile = getJsonConfigFileForTests('license-1.json')
        def json = new JsonParser()
        json.parse(jsonConfigFile)

        assertEquals(2, Utils.checkAttributions(json, dependencies).size())

    }


    @Test(expected = GradleException.class)
    void testThrowsExceptionWhenPackageDoesntExist() {
        Set<String> dependencies = new HashSet<>();
        dependencies.add("com.github.bumptech.glide:glide")
        dependencies.add("com.android.support:appcompat-v7")
        dependencies.add("com.android.support:support-v4")

        def jsonConfigFile = getJsonConfigFileForTests('license-1.json')
        def json = new JsonParser()
        json.parse(jsonConfigFile)

        Utils.checkAttributions(json, dependencies)
    }

    @Test
    void testCheckExcludedPackages_AllPackagesInConfig() {
        Set<String> dependencies = new HashSet<>();
        dependencies.add("com.android.support:appcompat-v7")
        dependencies.add("com.android.support:support-v4")

        def jsonConfigFile = getJsonConfigFileForTests('license-1.json')
        def json = new JsonParser()
        json.parse(jsonConfigFile)

        assertEquals(0, Utils.checkExcludedPackages(json, dependencies).size())
    }

    @Test(expected = GradleException.class)
    void testCheckExcludedPackages_MorePackagesInConfig() {
        Set<String> dependencies = new HashSet<>();
        dependencies.add("com.android.support:appcompat-v7")

        def jsonConfigFile = getJsonConfigFileForTests('license-1.json')
        def json = new JsonParser()
        json.parse(jsonConfigFile)
        
        assertEquals(1, Utils.checkExcludedPackages(json, dependencies).size())
    }

    @Test
    void testAllDependenciesAccountedFor() {
        Set<String> dependencies = new HashSet<>();
        assertTrue(Utils.ensureAllDependenciesAccountedFor(dependencies, LoggerFactory.getLogger(UtilsTest.class)))
    }

    @Test(expected = GradleException.class)
    void testAllDependenciesAccountedFor_NonEmptySet() {
        Set<String> dependencies = new HashSet<>();
        dependencies.add("com.fake.fakelibrary")
        Utils.ensureAllDependenciesAccountedFor(dependencies, LoggerFactory.getLogger(UtilsTest.class))
    }
}