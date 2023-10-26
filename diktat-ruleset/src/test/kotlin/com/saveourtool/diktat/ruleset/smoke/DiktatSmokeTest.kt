package com.saveourtool.diktat.ruleset.smoke

import com.saveourtool.diktat.api.DiktatError
import com.saveourtool.diktat.ktlint.format
import com.saveourtool.diktat.ktlint.lint
import com.saveourtool.diktat.ruleset.rules.DiktatRuleConfigReaderImpl
import com.saveourtool.diktat.ruleset.rules.DiktatRuleSetFactoryImpl
import com.saveourtool.diktat.test.framework.processing.TestComparatorUnit
import org.jetbrains.kotlin.utils.addToStdlib.safeAs
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import java.nio.file.Path
import kotlin.io.path.inputStream
import kotlin.io.path.readText

/**
 * Test for [DiktatRuleSetFactoryImpl] in autocorrect mode as a whole. All rules are applied to a file.
 * Note: ktlint uses initial text from a file to calculate line and column from offset. Because of that line/col of unfixed errors
 * may change after some changes to text or other rules.
 */
class DiktatSmokeTest : DiktatSmokeTestBase() {
    private val unfixedLintErrors: MutableList<DiktatError> = mutableListOf()

    override fun fixAndCompare(
        config: Path,
        expected: String,
        test: String,
    ) {
        val result = getTestComparatorUnit(config)
            .compareFilesFromResources(expected, test)
        if (!result.isSuccessful) {
            Assertions.assertAll(
                {
                    assertUnfixedLintErrors {
                        org.assertj.core.api.Assertions.assertThat(unfixedLintErrors).isEmpty()
                    }
                },
                {
                    Assertions.assertEquals(result.expectedContentWithoutWarns, result.actualContent)
                }
            )
        }
        Assertions.assertTrue(result.isSuccessful)
    }

    @BeforeEach
    internal fun setUp() {
        unfixedLintErrors.clear()
    }

    override fun assertUnfixedLintErrors(diktatErrorConsumer: (List<DiktatError>) -> Unit) {
        diktatErrorConsumer(unfixedLintErrors)
    }

    private fun getTestComparatorUnit(config: Path) = TestComparatorUnit(
        resourceFilePath = RESOURCE_FILE_PATH,
        function = { testFile ->
            lint(
                ruleSetSupplier = {
                    val diktatRuleConfigReader = DiktatRuleConfigReaderImpl()
                    val diktatRuleSetFactory = DiktatRuleSetFactoryImpl()
                    diktatRuleSetFactory(diktatRuleConfigReader(config.inputStream()))
                },
                file = testFile,
                cb = { lintError, _ -> unfixedLintErrors.add(lintError) },
            )
            format(
                ruleSetSupplier = {
                    val diktatRuleConfigReader = DiktatRuleConfigReaderImpl()
                    val diktatRuleSetFactory = DiktatRuleSetFactoryImpl()
                    diktatRuleSetFactory(diktatRuleConfigReader(config.inputStream()))
                },
                file = testFile,
                cb = { _, _ -> },
            )
        },
    )
}
