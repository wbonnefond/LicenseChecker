package com.wardbonnefond.licensechecker

import org.gradle.api.GradleException
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito
import org.slf4j.Logger

import static org.junit.Assert.*

class UtilsTest {

    private final static INPUT_PATH = 'src/test/resources/inputs/'
    private final static OUTPUT_PATH = 'src/test/resources/outputs/'


    private Logger logger;

    /**
     *  Can't figure out what is happening, but if tests run during project build
     *  it has /license-checker in the relative path, if you run the tests manually
     *  from Studio it doesn't. This method will add the folder to the path if needed
     * @return
     */
    private File getJsonConfigFileForTests(fileName) {
        def jsonConfigFile = new File(INPUT_PATH, fileName)
        if (!jsonConfigFile.exists()) {
            jsonConfigFile = new File('license-checker/' + INPUT_PATH, fileName)
        }
        return jsonConfigFile
    }

    private String getOutputTextForTest(fileName) {
        def outputFile = new File(OUTPUT_PATH, fileName)
        if (!outputFile.exists()) {
            outputFile = new File('license-checker/' + OUTPUT_PATH, fileName)
        }
        return outputFile.text
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

        assertEquals(2, Utils.checkAttributions(json, dependencies).size())

    }

    @Test
    void testThrowsExceptionWhenPackageDoesntExist_FailOnMissing() {
        Set<String> dependencies = new HashSet<>();
        dependencies.add("com.github.bumptech.glide:glide")
        dependencies.add("com.android.support:appcompat-v7")
        dependencies.add("com.android.support:support-v4")

        def jsonConfigFile = getJsonConfigFileForTests('license-1.json')
        def json = new JsonParser()
        json.parse(jsonConfigFile)

        assertEquals(2,Utils.checkAttributions(json, dependencies).size())
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

    @Test
    void testCheckExcludedPackages_MorePackagesInConfig() {
        Set<String> dependencies = new HashSet<>();
        dependencies.add("com.android.support:appcompat-v7")

        def jsonConfigFile = getJsonConfigFileForTests('license-1.json')
        def json = new JsonParser()
        json.parse(jsonConfigFile)

        assertEquals(0, Utils.checkExcludedPackages(json, dependencies).size())
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

    @Test
    void testHtmlOutput_license1() {
        def jsonConfigFile = getJsonConfigFileForTests('basic.json')
        def json = new JsonParser()
        json.parse(jsonConfigFile)
        Set<String> dependencies = new HashSet<>()
        dependencies.add("com.fake.library:fakelibrary")
        dependencies.add("com.new:some-library")
        dependencies.add("com.android.support:appcompat-v7")

        assertEquals(getOutputTextForTest('basic-output.txt'), Utils.buildHtmlOutput(json, dependencies))
    }

    @Test
    void testConfigurationForVariantCompile() {
        assertTrue(Utils.isConfigurationForCurrentVariant("compile", "debug", "paid"))
    }

    @Test
    void testConfigurationForVariantCompileDebug() {
        assertTrue(Utils.isConfigurationForCurrentVariant("debugCompile", "debug", "paid"))
    }

    @Test
    void testConfigurationForVariantPaidDebugCompile() {
        assertTrue(Utils.isConfigurationForCurrentVariant("paidDebugCompile", "debug", "paid"))
    }

    @Test
    void testConfigurationForVariantReleaseCompile() {
        assertFalse(Utils.isConfigurationForCurrentVariant("debugCompile", "release", "paid"))
    }

    @Test
    void testConfigurationForVariantFreeCompile() {
        assertFalse(Utils.isConfigurationForCurrentVariant("freeCompile", "release", "paid"))
    }

    @Test
    void testConfigurationForVariantFreePaidCompile() {
        assertFalse(Utils.isConfigurationForCurrentVariant("paidFreeCompile", "release", "paid"))
    }

    @Test
    void testConfigurationForVariantDebugCompileReleaseEmpty() {
        assertFalse(Utils.isConfigurationForCurrentVariant("debugCompile", "release", ""))
    }

    @Test
    void testConfigurationForVariantDebugCompileEmpty() {
        assertTrue(Utils.isConfigurationForCurrentVariant("debugCompile", "debug", ""))
    }
}