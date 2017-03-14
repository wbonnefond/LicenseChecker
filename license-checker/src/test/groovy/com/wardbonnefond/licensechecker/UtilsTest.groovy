package com.wardbonnefond.licensechecker

import org.gradle.api.GradleException
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.slf4j.Logger

import static org.junit.Assert.*

class UtilsTest {

    private final static RESOURCE_PATH = 'src/test/resources/'

    private Logger logger;

    /**
     *  Can't figure out what is happening, but if tests run during project build
     *  it has /license-checker in the relative path, if you run the tests manually
     *  from Studio it doesn't. This method will add the folder to the path if needed
     * @return
     */
    private File getJsonConfigFileForTests(fileName) {
        def jsonConfigFile = new File(RESOURCE_PATH, fileName)
        if (!jsonConfigFile.exists()) {
            jsonConfigFile = new File('license-checker/' + RESOURCE_PATH, fileName)
        }
        return jsonConfigFile
    }

    @Before
    public void setup() {
        logger = Mockito.mock(Logger.class)

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

        assertEquals(2, Utils.checkAttributions(json, dependencies, true).size())

    }


    @Test(expected = GradleException.class)
    void testThrowsExceptionWhenPackageDoesntExist_FailOnMissing() {
        Set<String> dependencies = new HashSet<>();
        dependencies.add("com.github.bumptech.glide:glide")
        dependencies.add("com.android.support:appcompat-v7")
        dependencies.add("com.android.support:support-v4")

        def jsonConfigFile = getJsonConfigFileForTests('license-1.json')
        def json = new JsonParser()
        json.parse(jsonConfigFile)

        Utils.checkAttributions(json, dependencies, true)
    }

    @Test
    void testCheckExcludedPackages_AllPackagesInConfig() {
        Set<String> dependencies = new HashSet<>();
        dependencies.add("com.android.support:appcompat-v7")
        dependencies.add("com.android.support:support-v4")

        def jsonConfigFile = getJsonConfigFileForTests('license-1.json')
        def json = new JsonParser()
        json.parse(jsonConfigFile)

        assertEquals(0, Utils.checkExcludedPackages(json, dependencies, true).size())
    }

    @Test(expected = GradleException.class)
    void testCheckExcludedPackages_MorePackagesInConfig() {
        Set<String> dependencies = new HashSet<>();
        dependencies.add("com.android.support:appcompat-v7")

        def jsonConfigFile = getJsonConfigFileForTests('license-1.json')
        def json = new JsonParser()
        json.parse(jsonConfigFile)

        assertEquals(1, Utils.checkExcludedPackages(json, dependencies, true).size())
    }

    @Test
    void testAllDependenciesAccountedFor() {
        Set<String> dependencies = new HashSet<>();
        assertTrue(Utils.ensureAllDependenciesAccountedFor(dependencies, logger, true))
        Mockito.verify(logger, Mockito.times(1)).info("All dependencies accounted for in attributions.json")
    }

    @Test(expected = GradleException.class)
    void testAllDependenciesAccountedFor_NonEmptySet() {
        Set<String> dependencies = new HashSet<>();
        dependencies.add("com.fake.fakelibrary")
        Utils.ensureAllDependenciesAccountedFor(dependencies, logger, true)
    }

    @Test
    void testIsTaskBuildingMultipleVariants_assemble() {
        def task = "assemble"
        def buildTypes = ["release", "debug", "nightly"]
        def productFlavors = ["paid", "free"]
        assertEquals("assemble", Utils.isTaskBuildingMultipleVariants(task, buildTypes, productFlavors))
    }

    @Test
    void testIsTaskBuildingMultipleVariants_assembleDebug() {
        def task = "assembleDebug"
        def buildTypes = ["release", "debug", "nightly"]
        def productFlavors = ["paid", "free"]
        assertEquals("debug", Utils.isTaskBuildingMultipleVariants(task, buildTypes, productFlavors))
    }

    @Test
    void testIsTaskBuildingMultipleVariants_assembleFree() {
        def task = "assembleFree"
        def buildTypes = ["release", "debug", "nightly"]
        def productFlavors = ["paid", "free"]
        assertEquals("free", Utils.isTaskBuildingMultipleVariants(task, buildTypes, productFlavors))
    }

    @Test
    void testIsTaskBuildingMultipleVariants_assembleFreeDebug() {
        def task = "assembleFreeDebug"
        def buildTypes = ["release", "debug", "nightly"]
        def productFlavors = ["paid", "free"]
        assertEquals("", Utils.isTaskBuildingMultipleVariants(task, buildTypes, productFlavors))
    }

    @Test(expected = GradleException.class)
    void testJsonContainsDuplicates_DuplicatePackages() {
        def jsonConfigFile = getJsonConfigFileForTests('duplicate-libs.json')
        def json = new JsonParser()
        json.parse(jsonConfigFile)
        Utils.jsonContainsDuplicates(json)
    }

    @Test
    void testJsonContainsDuplicates_NoDuplicates() {
        def jsonConfigFile = getJsonConfigFileForTests('license-1.json')
        def json = new JsonParser()
        json.parse(jsonConfigFile)
        assertFalse(Utils.jsonContainsDuplicates(json))
    }

    @Test(expected = GradleException.class)
    void testJsonContainsDuplicates_DuplicateExcludedPackages() {
        def jsonConfigFile = getJsonConfigFileForTests('duplicate-excluded-libs.json')
        def json = new JsonParser()
        json.parse(jsonConfigFile)
        Utils.jsonContainsDuplicates(json)
    }

    @Test(expected = GradleException.class)
    void testJsonContainsDuplicates_PackageIncludedAndExcluded() {
        def jsonConfigFile = getJsonConfigFileForTests('lib-in-both.json')
        def json = new JsonParser()
        json.parse(jsonConfigFile)
        Utils.jsonContainsDuplicates(json)
    }
}