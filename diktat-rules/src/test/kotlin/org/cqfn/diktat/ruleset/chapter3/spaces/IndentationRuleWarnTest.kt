package org.cqfn.diktat.ruleset.chapter3.spaces

import org.cqfn.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_INDENTATION
import org.cqfn.diktat.ruleset.rules.chapter3.files.IndentationRule
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.assertj.core.api.AbstractSoftAssertions
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.SoftAssertions.assertSoftly
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.MethodOrderer.MethodName
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.opentest4j.MultipleFailuresError

import java.util.function.Consumer

@Suppress("LargeClass")
@TestMethodOrder(MethodName::class)
class IndentationRuleWarnTest : LintTestBase(::IndentationRule), IndentationRuleTestMixin {
    private val ruleId = "$DIKTAT_RULE_SET_ID:${IndentationRule.NAME_ID}"
    private val rulesConfigList = listOf(
        RulesConfig(WRONG_INDENTATION.name, true,
            mapOf(
                "extendedIndentOfParameters" to "true",
                "alignedParameters" to "true",
                "extendedIndentAfterOperators" to "true",
                "extendedIndentBeforeDot" to "false",
                "indentationSize" to "4"
            )
        )
    )
    private val disabledOptionsRulesConfigList = listOf(
        RulesConfig(WRONG_INDENTATION.name, true,
            mapOf(
                "extendedIndentOfParameters" to "false",
                "alignedParameters" to "false",
                "extendedIndentAfterOperators" to "false",
                "extendedIndentBeforeDot" to "false",
                "indentationSize" to "4"
            )
        )
    )

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `should warn if tabs are used in indentation`() {
        lintMethod(
            """
                    |class Example {
                    |${"\t"}val zero = 0
                    |}
                    |
            """.trimMargin(),
            LintError(2, 1, ruleId, "${WRONG_INDENTATION.warnText()} tabs are not allowed for indentation", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `should warn if indent size is not 4 spaces`() {
        lintMethod(
            """
                    |class Example {
                    |   val zero = 0
                    |}
                    |
            """.trimMargin(),
            LintError(2, 1, ruleId, warnText(4, 3), true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `should warn if no new line at the end of file`() {
        lintMethod(
            """
                    |class Example {
                    |    val zero = 0
                    |}
            """.trimMargin(),
            LintError(3, 1, ruleId, "${WRONG_INDENTATION.warnText()} no newline at the end of file TestFileName.kt", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `should warn if no new line at the end of file, last child whitespace`() {
        lintMethod(
            """
                |class Example {
                |    val zero = 0
                |}
            """.trimMargin(),
            LintError(3, 1, ruleId, "${WRONG_INDENTATION.warnText()} no newline at the end of file TestFileName.kt", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `should warn if too many blank lines at the end of file`() {
        lintMethod(
            """
                    |class Example {
                    |    val zero = 0
                    |}
                    |
                    |
            """.trimMargin(),
            LintError(5, 1, ruleId, "${WRONG_INDENTATION.warnText()} too many blank lines at the end of file TestFileName.kt", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `valid indentation - example 1`() {
        lintMethod(
            """
                    |class Example {
                    |    private val foo = 0
                    |    private val fuu =
                    |        0
                    |
                    |    fun bar() {
                    |        if (foo > 0) {
                    |            baz()
                    |        } else {
                    |            bazz()
                    |        }
                    |        return foo
                    |    }
                    |}
                    |
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `parameters can be indented by 8 spaces - positive example`() {
        lintMethod(
            """
                    |class Example(
                    |        val field1: Type1,
                    |        val field2: Type2,
                    |        val field3: Type3
                    |) {
                    |    val e1 = Example(
                    |            t1,
                    |            t2,
                    |            t3
                    |    )
                    |
                    |    val e2 = Example(t1, t2,
                    |            t3
                    |    )
                    |}
                    |
            """.trimMargin(),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `parameters can be aligned - positive example`() {
        lintMethod(
            """
                    |class Example(val field1: Type1,
                    |              val field2: Type2,
                    |              val field3: Type3) {
                    |}
                    |
            """.trimMargin(),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `parameters can be aligned`() {
        lintMethod(
            """
                    |class Example(
                    |              val field1: Type1,
                    |              val field2: Type2,
                    |              val field3: Type3) {
                    |}
                    |
            """.trimMargin(),
            LintError(2, 1, ruleId, warnText(8, 14), true),
            LintError(3, 1, ruleId, warnText(8, 14), true),
            LintError(4, 1, ruleId, warnText(8, 14), true),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `lines split by operator can be indented by 8 spaces`() {
        lintMethod(
            """
                    |fun foo(a: Int, b: Int) {
                    |    return 2 * a +
                    |            b
                    |}
                    |
            """.trimMargin(),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `should check indentation in KDocs - positive example`() {
        lintMethod(
            """
                    |/**
                    | * Lorem ipsum
                    | */
                    |class Example {
                    |}
                    |
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `assignment increases indentation if followed by newline`() {
        lintMethod(
            """
                    |fun <T> foo(list: List<T>) {
                    |    val a = list.filter {
                    |        predicate(it)
                    |    }
                    |
                    |    val b =
                    |        list.filter {
                    |            predicate(it)
                    |        }
                    |}
                    |
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `when lambda is assigned, indentation is increased by one step`() {
        lintMethod(
            """
                    |fun foo() {
                    |    val a = { x: Int ->
                    |        x * 2
                    |    }
                    |
                    |    val b =
                    |        { x: Int ->
                    |            x * 2
                    |        }
                    |}
                    |
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `should check indentation in KDocs`() {
        lintMethod(
            """
                    |/**
                    |* Lorem ipsum
                    |*/
                    |class Example {
                    |}
                    |
            """.trimMargin(),
            LintError(2, 1, ruleId, warnText(1, 0), true),
            LintError(3, 1, ruleId, warnText(1, 0), true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `dot call increases indentation`() {
        lintMethod(
            """
                    |fun foo() {
                    |    Integer
                    |        .valueOf(2).also {
                    |            println(it)
                    |        }
                    |        ?.also {
                    |            println("Also with safe access")
                    |        }
                    |        ?: Integer.valueOf(0)
                    |
                    |    bar
                    |        .baz()
                    |        as Baz
                    |        as? Baz
                    |}
                    |
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `loops and conditionals without braces should be indented - positive example`() {
        lintMethod(
            """
                    |fun foo() {
                    |    for (i in 1..100)
                    |        println(i)
                    |
                    |    do
                    |        println()
                    |    while (condition)
                    |
                    |    if (condition)
                    |        bar()
                    |    else
                    |        baz()
                    |}
                    |
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `loops and conditionals without braces should be indented`() {
        lintMethod(
            """
                    |fun foo() {
                    |    for (i in 1..100)
                    |    println(i)
                    |
                    |    do
                    |    println()
                    |    while (condition)
                    |
                    |    if (condition)
                    |    bar()
                    |    else
                    |    baz()
                    |}
                    |
            """.trimMargin(),
            LintError(3, 1, ruleId, warnText(8, 4), true),
            LintError(6, 1, ruleId, warnText(8, 4), true),
            LintError(10, 1, ruleId, warnText(8, 4), true),
            LintError(12, 1, ruleId, warnText(8, 4), true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `loops and conditionals without braces should be indented - if-else with mixed braces`() {
        lintMethod(
            """
                    |fun foo() {
                    |    if (condition) {
                    |        bar()
                    |    } else
                    |        baz()
                    |
                    |    if (condition)
                    |        bar()
                    |    else {
                    |        baz()
                    |    }
                    |
                    |    if (condition)
                    |        bar()
                    |    else if (condition2) {
                    |        baz()
                    |    } else
                    |        qux()
                    |
                    |    if (condition)
                    |        bar()
                    |    else if (condition2)
                    |        baz()
                    |    else {
                    |        quux()
                    |    }
                    |}
                    |
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `opening braces should not increase indent when placed on the same line`() {
        lintMethod(
            """
                    |fun foo() {
                    |    consume(Example(
                    |        t1, t2, t3
                    |    ))
                    |
                    |    bar(baz(
                    |        1,
                    |        2
                    |    )
                    |    )
                    |
                    |    bar(baz(
                    |        1,
                    |        2),
                    |        3
                    |    )
                    |}
                    |
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `opening braces should not increase indent when placed on the same line - with disabled options`() {
        lintMethod(
            """
                    |fun foo() {
                    |    bar(baz(
                    |        1,
                    |        2),
                    |        3
                    |    )
                    |}
                    |
            """.trimMargin(),
            rulesConfigList = disabledOptionsRulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `custom getters and setters should increase indentation - positive example`() {
        lintMethod(
            """
                    |class Example {
                    |    private val foo
                    |        get() = 0
                    |
                    |    private var backing = 0
                    |
                    |    var bar
                    |        get() = backing
                    |        set(value) { backing = value }
                    |}
                    |
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `custom getters and setters should increase indentation`() {
        lintMethod(
            """
                    |class Example {
                    |    private val foo
                    |            get() = 0
                    |
                    |    private var backing = 0
                    |
                    |    var bar
                    |    get() = backing
                    |    set(value) { backing = value }
                    |}
                    |
            """.trimMargin(),
            LintError(3, 1, ruleId, warnText(8, 12), true),
            LintError(8, 1, ruleId, warnText(8, 4), true),
            LintError(9, 1, ruleId, warnText(8, 4), true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `regression - indentation should be increased inside parameter list for multiline parameters`() {
        lintMethod(
            """
                    |fun foo() {
                    |    bar(
                    |        param1 = baz(
                    |            1,
                    |            2
                    |        ),
                    |        param2 = { elem ->
                    |            elem.qux()
                    |        },
                    |        param3 = x
                    |            .y()
                    |    )
                    |}
                    |
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `regression - nested blocks inside loops and conditionals without braces should be properly indented`() {
        lintMethod(
            """
                    |fun foo() {
                    |    if (condition)
                    |        list.filter {
                    |            bar()
                    |        }
                    |            .call(
                    |                param1,
                    |                param2
                    |            )
                    |    else
                    |        list
                    |            .filter {
                    |                baz()
                    |            }
                    |}
                    |
            """.trimMargin(),
            rulesConfigList = listOf(
                RulesConfig(WRONG_INDENTATION.name, true,
                    mapOf(
                        "extendedIndentOfParameters" to "false",
                        "extendedIndentBeforeDot" to "false"
                    )
                )
            )
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `arrows in when expression should increase indentation - positive example`() {
        lintMethod(
            """
                    |fun foo() {
                    |    when (x) {
                    |        X_1 ->
                    |            foo(x)
                    |        X_2 -> bar(x)
                    |        X_3 -> {
                    |            baz(x)
                    |        }
                    |        else ->
                    |            qux(x)
                    |    }
                    |}
                    |
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `arrows in when expression should increase indentation`() {
        lintMethod(
            """
                    |fun foo() {
                    |    when (x) {
                    |        X_1 ->
                    |        foo(x)
                    |        X_2 -> bar(x)
                    |        X_3 -> {
                    |        baz(x)
                    |        }
                    |        else ->
                    |        qux(x)
                    |    }
                    |}
                    |
            """.trimMargin(),
            LintError(4, 1, ruleId, warnText(12, 8), true),
            LintError(7, 1, ruleId, warnText(12, 8), true),
            LintError(10, 1, ruleId, warnText(12, 8), true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `comments should not turn off exceptional indentation`() {
        lintMethod(
            """
                    |fun foo() {
                    |    list
                    |        .map(::foo)
                    |        // comment about the next call
                    |        .filter { it.bar() }
                    |        // another comment about the next call
                    |        ?.filter { it.bar() }
                    |        ?.count()
                    |
                    |    list.any { predicate(it) } &&
                    |        list.any {
                    |            predicate(it)
                    |        }
                    |
                    |    list.any { predicate(it) } &&
                    |        // comment
                    |        list.any {
                    |            predicate(it)
                    |        }
                    |
                    |    list.filter {
                    |        predicate(it) &&
                    |            // comment
                    |            predicate(it)
                    |    }
                    |}
                    |
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `regression - npe with comments`() {
        lintMethod(
            """
                |fun foo() {
                |    bar.let {
                |        baz(it)
                |        // lorem ipsum
                |    }
                |}
                |
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `closing parenthesis bug`() {
        lintMethod(
            """
                    |fun foo() {
                    |    return x +
                    |        (y +
                    |            foo(x)
                    |        )
                    |}
                    |
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `should trigger on string templates starting with new line`() {
        lintMethod(
            """
                |fun foo(some: String) {
                |    fun bar() {
                |        val a = "${'$'}{
                |        expression
                |            .foo()
                |            .bar()
                |        }"
                |    }
                |
                |    val b = "${'$'}{ foo().bar() }"
                |}
                |
            """.trimMargin(),
            LintError(4, 1, ruleId, warnText(12, 8), true),
            LintError(5, 1, ruleId, warnText(16, 12), true),
            LintError(6, 1, ruleId, warnText(16, 12), true),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `check script`() {
        lintMethod(
            """
                |val q = 1
                |
            """.trimMargin(),
            fileName = "src/main/kotlin/org/cqfn/diktat/Example.kts"
        )
    }

    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `check gradle script`() {
        lintMethod(
            """
                |projectName = "diKTat"
                |
            """.trimMargin(),
            fileName = "src/main/kotlin/org/cqfn/diktat/build.gradle.kts"
        )
    }

    /**
     * This test has a counterpart under [IndentationRuleFixTest].
     *
     * See [#1330](https://github.com/saveourtool/diktat/issues/1330).
     */
    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `expression body functions should be properly indented (extendedIndentAfterOperators = true)`() {
        val defaultConfig = IndentationConfig("newlineAtEnd" to false)
        val customConfig = defaultConfig.withCustomParameters("extendedIndentAfterOperators" to true)

        lintMultipleMethods(
            expressionBodyFunctionsContinuationIndent,
            lintErrors = emptyArray(),
            customConfig.asRulesConfigList()
        )
    }

    /**
     * This test has a counterpart under [IndentationRuleFixTest].
     *
     * See [#1330](https://github.com/saveourtool/diktat/issues/1330).
     */
    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `expression body functions should be properly indented (extendedIndentAfterOperators = false)`() {
        val defaultConfig = IndentationConfig("newlineAtEnd" to false)
        val customConfig = defaultConfig.withCustomParameters("extendedIndentAfterOperators" to false)

        lintMultipleMethods(
            expressionBodyFunctionsSingleIndent,
            lintErrors = emptyArray(),
            customConfig.asRulesConfigList()
        )
    }

    /**
     * This test has a counterpart under [IndentationRuleFixTest].
     *
     * See [#1330](https://github.com/saveourtool/diktat/issues/1330).
     */
    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `expression body functions should be reported if mis-indented (extendedIndentAfterOperators = true)`() {
        val defaultConfig = IndentationConfig("newlineAtEnd" to false)
        val customConfig = defaultConfig.withCustomParameters("extendedIndentAfterOperators" to true)

        assertSoftly { softly ->
            expressionBodyFunctionsSingleIndent.forEach { code ->
                softly.assertThat(lintResult(code, customConfig.asRulesConfigList()))
                    .describedAs("lint result for ${code.describe()}")
                    .isNotEmpty
                    .hasSizeBetween(1, 3).allSatisfy(Consumer { lintError ->
                        assertThat(lintError.ruleId).describedAs("ruleId").isEqualTo(ruleId)
                        assertThat(lintError.canBeAutoCorrected).describedAs("canBeAutoCorrected").isTrue
                        assertThat(lintError.detail).matches(warnTextRegex)
                    })
            }
        }
    }

    /**
     * This test has a counterpart under [IndentationRuleFixTest].
     *
     * See [#1330](https://github.com/saveourtool/diktat/issues/1330).
     */
    @Test
    @Tag(WarningNames.WRONG_INDENTATION)
    fun `expression body functions should be reported if mis-indented (extendedIndentAfterOperators = false)`() {
        val defaultConfig = IndentationConfig("newlineAtEnd" to false)
        val customConfig = defaultConfig.withCustomParameters("extendedIndentAfterOperators" to false)

        assertSoftly { softly ->
            expressionBodyFunctionsContinuationIndent.forEach { code ->
                softly.assertThat(lintResult(code, customConfig.asRulesConfigList()))
                    .describedAs("lint result for ${code.describe()}")
                    .isNotEmpty
                    .hasSizeBetween(1, 3).allSatisfy(Consumer { lintError ->
                        assertThat(lintError.ruleId).describedAs("ruleId").isEqualTo(ruleId)
                        assertThat(lintError.canBeAutoCorrected).describedAs("canBeAutoCorrected").isTrue
                        assertThat(lintError.detail).matches(warnTextRegex)
                    })
            }
        }
    }

    /**
     * @see warnTextRegex
     */
    private fun warnText(expected: Int, actual: Int) = "${WRONG_INDENTATION.warnText()} expected $expected but was $actual"

    /**
     * When within a scope of an `AbstractSoftAssertions`, collects failures
     * thrown by [block], correctly accumulating multiple failures from nested
     * soft assertions (if any).
     *
     * @see org.assertj.core.api.AssertionErrorCollector.collectAssertionError
     */
    private fun AbstractSoftAssertions.collectAssertionErrors(block: () -> Unit) =
        try {
            block()
        } catch (mfe: MultipleFailuresError) {
            mfe.failures.forEach { failure ->
                when (failure) {
                    is AssertionError -> collectAssertionError(failure)
                    else -> fail(failure.toString(), failure)
                }
            }
        } catch (ae: AssertionError) {
            collectAssertionError(ae)
        } catch (th: Throwable) {
            fail(th.toString(), th)
        }

    /**
     * Similar to [lintMethod], but can be invoked from a scope of
     * `AbstractSoftAssertions` in order to accumulate test results from linting
     * _multiple_ code fragments.
     *
     * @param rulesConfigList the list of rules which can optionally override
     *   the [default value][LintTestBase.rulesConfigList].
     * @see lintMethod
     */
    private fun AbstractSoftAssertions.lintMethodSoftly(
        @Language("kotlin") code: String,
        vararg lintErrors: LintError,
        rulesConfigList: List<RulesConfig>? = null,
        fileName: String? = null
    ) {
        require(code.isNotBlank()) {
            "code is blank"
        }

        collectAssertionErrors {
            lintMethod(code, lintErrors = lintErrors, rulesConfigList, fileName)
        }
    }

    /**
     * Tests multiple code [fragments] using the same
     * [rule configuration][rulesConfigList].
     *
     * All code fragments get concatenated together and the resulting, bigger
     * fragment gets tested, too.
     *
     * @param rulesConfigList the list of rules which can optionally override
     *   the [default value][LintTestBase.rulesConfigList].
     * @see lintMethod
     */
    private fun lintMultipleMethods(
        @Language("kotlin") fragments: Array<String>,
        vararg lintErrors: LintError,
        rulesConfigList: List<RulesConfig>? = null,
        fileName: String? = null
    ) {
        require(fragments.isNotEmpty()) {
            "code fragments is an empty array"
        }

        assertSoftly { softly ->
            fragments.asSequenceWithConcatenation().forEach { fragment ->
                softly.lintMethodSoftly(
                    fragment,
                    lintErrors = lintErrors,
                    rulesConfigList,
                    fileName
                )
            }
        }
    }

    companion object {
        /**
         * @see warnText
         */
        @Language("RegExp")
        private val warnTextRegex = "^\\Q${WRONG_INDENTATION.warnText()}\\E expected \\d+ but was \\d+$"
    }
}
