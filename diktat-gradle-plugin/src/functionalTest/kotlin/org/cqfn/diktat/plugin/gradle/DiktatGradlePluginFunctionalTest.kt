package org.cqfn.diktat.plugin.gradle

import org.cqfn.diktat.plugin.gradle.DiktatGradlePlugin.Companion.DIKTAT_CHECK_TASK
import org.gradle.buildinit.plugins.internal.modifiers.BuildInitDsl
import org.gradle.internal.impldep.org.junit.rules.TemporaryFolder
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File

class DiktatGradlePluginFunctionalTest {
    private val testProjectDir = TemporaryFolder()
    private lateinit var buildFile: File

    @BeforeEach
    fun setUp() {
        testProjectDir.create()
        val buildInitDsl = BuildInitDsl.KOTLIN
        createExampleProject(testProjectDir, File("../examples/gradle-kotlin-dsl"), buildInitDsl)
        buildFile = testProjectDir.root.resolve(buildInitDsl.fileNameFor("build"))
    }

    @AfterEach
    fun tearDown() {
        testProjectDir.delete()
    }

    @Test
    fun `should execute diktatCheck on default values`() {
        val result = runDiktat(testProjectDir, shouldSucceed = false)

        val diktatCheckBuildResult = result.task(":$DIKTAT_CHECK_TASK")
        requireNotNull(diktatCheckBuildResult)
        Assertions.assertEquals(TaskOutcome.FAILED, diktatCheckBuildResult.outcome)
        Assertions.assertTrue(
            result.output.contains("[HEADER_MISSING_OR_WRONG_COPYRIGHT]")
        )
    }

    @Test
    fun `should execute diktatCheck with explicit inputs`() {
        buildFile.appendText(
            """${System.lineSeparator()}
                diktat {
                    inputs = files("src/**/*.kt")
                }
            """.trimIndent()
        )
        val result = runDiktat(testProjectDir, shouldSucceed = false)

        val diktatCheckBuildResult = result.task(":$DIKTAT_CHECK_TASK")
        requireNotNull(diktatCheckBuildResult)
        Assertions.assertEquals(TaskOutcome.FAILED, diktatCheckBuildResult.outcome)
        Assertions.assertTrue(
            result.output.contains("[HEADER_MISSING_OR_WRONG_COPYRIGHT]")
        )
    }

    @Test
    fun `should execute diktatCheck with excludes`() {
        buildFile.appendText(
            """${System.lineSeparator()}
                diktat {
                    inputs = files("src/**/*.kt")
                    excludes = files("src/**/Test.kt")
                }
            """.trimIndent()
        )
        val result = runDiktat(testProjectDir)

        val diktatCheckBuildResult = result.task(":$DIKTAT_CHECK_TASK")
        requireNotNull(diktatCheckBuildResult)
        Assertions.assertEquals(TaskOutcome.SUCCESS, diktatCheckBuildResult.outcome)
    }

    @Test
    fun `should not run diktat with ktlint's default includes when no files match include patterns`() {
        buildFile.appendText(
            """${System.lineSeparator()}
                diktat {
                    inputs = files("nonexistent-directory/src/**/*.kt")
                }
            """.trimIndent()
        )
        val result = runDiktat(testProjectDir, arguments = listOf("--info"))

        // if patterns in gradle are not checked for matching, they are passed to ktlint, which does nothing
        val diktatCheckBuildResult = result.task(":$DIKTAT_CHECK_TASK")
        requireNotNull(diktatCheckBuildResult)
        Assertions.assertEquals(TaskOutcome.SUCCESS, diktatCheckBuildResult.outcome)
        Assertions.assertFalse(
            result.output.contains("Skipping diktat execution")
        )
        Assertions.assertFalse(
            result.output.contains("Inputs for $DIKTAT_CHECK_TASK do not exist, will not run diktat")
        )
    }

    @Test
    fun `should not run diktat with ktlint's default includes when no files match include patterns - 2`() {
        buildFile.appendText(
            """${System.lineSeparator()}
                diktat {
                    inputs = fileTree("nonexistent-directory/src").apply { include("**/*.kt") }
                }
            """.trimIndent()
        )
        val result = runDiktat(testProjectDir, arguments = listOf("--info"))

        val diktatCheckBuildResult = result.task(":$DIKTAT_CHECK_TASK")
        requireNotNull(diktatCheckBuildResult)
        Assertions.assertEquals(TaskOutcome.SUCCESS, diktatCheckBuildResult.outcome)
        Assertions.assertTrue(
            result.output.contains("Skipping diktat execution")
        )
        Assertions.assertTrue(
            result.output.contains("Inputs for $DIKTAT_CHECK_TASK do not exist, will not run diktat")
        )
    }

    @Test
    fun `should execute diktatCheck with absolute paths`() {
        val path = testProjectDir.root
            .resolve("src/**/*.kt")
            .absolutePath
            .replace("\\", "\\\\")
        buildFile.appendText(
            """${System.lineSeparator()}
                diktat {
                    inputs = files("$path")
                }
            """.trimIndent()
        )
        val result = runDiktat(testProjectDir, shouldSucceed = false)

        val diktatCheckBuildResult = result.task(":$DIKTAT_CHECK_TASK")
        requireNotNull(diktatCheckBuildResult)
        Assertions.assertEquals(TaskOutcome.FAILED, diktatCheckBuildResult.outcome)
        Assertions.assertTrue(
            result.output.contains("[HEADER_MISSING_OR_WRONG_COPYRIGHT]")
        )
    }

    @Test
    fun `should execute diktatCheck with gradle older than 6_4`() {
        val result = runDiktat(testProjectDir, shouldSucceed = false, arguments = listOf("--info")) {
            withGradleVersion("5.0")
        }

        val diktatCheckBuildResult = result.task(":$DIKTAT_CHECK_TASK")
        requireNotNull(diktatCheckBuildResult)
        Assertions.assertEquals(TaskOutcome.FAILED, diktatCheckBuildResult.outcome)
        Assertions.assertTrue(
            result.output.contains("[HEADER_MISSING_OR_WRONG_COPYRIGHT]")
        )
    }

    @Test
    fun `should respect ignoreFailures setting`() {
        buildFile.appendText(
            """${System.lineSeparator()}
                diktat {
                    ignoreFailures = true
                }
            """.trimIndent()
        )
        val result = runDiktat(testProjectDir, shouldSucceed = true, arguments = listOf("--info"))

        val diktatCheckBuildResult = result.task(":$DIKTAT_CHECK_TASK")
        requireNotNull(diktatCheckBuildResult)
        Assertions.assertEquals(TaskOutcome.SUCCESS, diktatCheckBuildResult.outcome)
        Assertions.assertTrue(
            result.output.contains("[HEADER_MISSING_OR_WRONG_COPYRIGHT]")
        )
    }
}
