package org.cqfn.diktat.test.framework.processing

import org.cqfn.diktat.test.framework.common.ExecutionResult
import org.cqfn.diktat.test.framework.common.TestBase
import org.cqfn.diktat.test.framework.config.TestConfig
import org.cqfn.diktat.test.framework.config.TestFrameworkProperties

import org.apache.commons.io.FileUtils
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.io.File

/**
 * A class that runs tests and compares output with expected result
 */
@Suppress("ForbiddenComment")
open class TestCompare : TestBase {
    @Suppress("MISSING_KDOC_CLASS_ELEMENTS") protected open val log: Logger = LoggerFactory.getLogger(TestCompare::class.java)
    private lateinit var expectedResult: File

    // testResultFile will be used if and only if --in-place option will be used
    private lateinit var testFile: File
    private lateinit var testConfig: TestConfig

    /** Result of the test run */
    protected lateinit var testResult: ExecutionResult

    /**
     * @return true if test has passed successfully, false otherwise
     */
    @Suppress("FUNCTION_BOOLEAN_PREFIX")
    override fun runTest(): Boolean {
        // FixMe: this is an execution for Windows, should support other OS
        val testPassed = if (testConfig.inPlace) processInPlace() else processToStdOut()

        if (testPassed) {
            log.info("Test <${testConfig.testName}> passed")
        } else {
            log.error("Test <${testConfig.testName}> failed")
        }

        return testPassed
    }

    /**
     * injects test configuration that was read from .json config file
     *
     * @param testConfig json configuration
     * @param properties config from properties
     * @return test instance itself
     */
    override fun initTestProcessor(testConfig: TestConfig, properties: TestFrameworkProperties): TestCompare {
        this.testConfig = testConfig
        this.expectedResult = buildFullPathToResource(
                testConfig.expectedResultFile,
                properties.testFilesRelativePath
        )
        this.testFile = buildFullPathToResource(testConfig.testFile, properties.testFilesRelativePath)

        return this
    }

    // STRING_TEMPLATE_CURLY_BRACES is disabled temporarily, until https://github.com/cqfn/diKTat/issues/401 is fixed
    @Suppress("STRING_TEMPLATE_CURLY_BRACES", "FUNCTION_BOOLEAN_PREFIX")
    private fun processInPlace(): Boolean {
        val copyTestFile = File("${testFile}_copy")
        FileUtils.copyFile(testFile, copyTestFile)
        executeCommand("cmd /c ${testConfig.executionCommand} $copyTestFile")

        val testPassed = FileComparator(expectedResult, copyTestFile).compareFilesEqual()
        FileUtils.forceDelete(copyTestFile)

        return testPassed
    }

    @Suppress("FUNCTION_BOOLEAN_PREFIX")
    private fun processToStdOut(): Boolean {
        this.testResult = executeCommand("cmd /c ${testConfig.executionCommand} $testFile")

        return FileComparator(expectedResult, getExecutionResult()).compareFilesEqual()
    }

    private fun buildFullPathToResource(resourceFile: String, resourceAbsolutePath: String): File {
        val fileUrl = javaClass.classLoader.getResource("$resourceAbsolutePath/$resourceFile")
        require(fileUrl != null) { "Cannot read resource file $$resourceAbsolutePath/$resourceFile - it cannot be found in resources" }
        return File(fileUrl.file)
    }

    /**
     * Get result of the test execution
     */
    protected open fun getExecutionResult() = testResult.stdOut
}
