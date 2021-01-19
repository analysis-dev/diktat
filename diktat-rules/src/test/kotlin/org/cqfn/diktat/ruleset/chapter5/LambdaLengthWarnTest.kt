package org.cqfn.diktat.ruleset.chapter5

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.LambdaLengthRule
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class LambdaLengthWarnTest : LintTestBase(::LambdaLengthRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:lambda-length"
    private val rulesConfigList: List<RulesConfig> = listOf(
        RulesConfig(
            Warnings.TOO_MANY_LINES_IN_LAMBDA.name, true,
            mapOf("maxLambdaLength" to "3"))
    )

    @Test
    @Tag(WarningNames.TOO_MANY_LINES_IN_LAMBDA)
    fun `less than max`() {
        lintMethod(
            """
                    |fun foo() {
                    |   val x = 10
                    |   val list = listOf(1, 2, 3, 4, 5)
                    |       .map {element -> element + x}
                    |}
                """.trimMargin(),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.TOO_MANY_LINES_IN_LAMBDA)
    fun `nested lambda with implicit parameter`() {
        lintMethod(
            """
                    |fun foo() {
                    |   private val allTestsFromResources: List<String> by lazy {
                    |     val fileUrl: URL? = javaClass.getResource("123")
                    |     val resource = fileUrl
                    |         ?.let { File(it.file) }
                    |   }
                    |}
                """.trimMargin(),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.TOO_MANY_LINES_IN_LAMBDA)
    fun `lambda doesn't expect parameters`() {
        lintMethod(
            """
                    |fun foo() {
                    |   private val allTestsFromResources: List<String> by lazy {
                    |     val fileUrl: URL? = javaClass.getResource("123")
                    |     list = listOf(1, 2, 3, 4, 5)
                    |         .removeAt(1)
                    |   }
                    |}
                """.trimMargin(),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.TOO_MANY_LINES_IN_LAMBDA)
    fun `less than max without argument`() {
        lintMethod(
            """
                    |fun foo() {
                    |   val x = 10
                    |   val list = listOf(1, 2, 3, 4, 5)
                    |       .map {it + x}
                    |}
                """.trimMargin(),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.TOO_MANY_LINES_IN_LAMBDA)
    fun `more than max with argument`() {
        lintMethod(
            """
                    |fun foo() {
                    |   val calculateX = { x : Int ->
                    |       when(x) {
                    |           in 0..40 -> "Fail"
                    |           in 41..70 -> "Pass"
                    |           in 71..100 -> "Distinction"
                    |           else -> false
                    |       }
                    |   }
                    |}
                """.trimMargin(),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.TOO_MANY_LINES_IN_LAMBDA)
    fun `more than maximum without argument`() {
        lintMethod(
            """
                    |fun foo() {
                    |   val list = listOf(1, 2, 3, 4, 5)
                    |       .map {
                    |           val x = 0
                    |           val y = x + 1
                    |           val z = y + 1
                    |           it + z
                    |   
                    |   
                    |   
                    |   
                    |   }
                    |}
                """.trimMargin(),
            LintError(3, 13, ruleId, "${Warnings.TOO_MANY_LINES_IN_LAMBDA.warnText()} max length lambda without arguments is 3, but you have 6", false),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.TOO_MANY_LINES_IN_LAMBDA)
    fun `two lambda more than maximum without argument`() {
        lintMethod(
            """
                    |fun foo() {
                    |   val list = listOf(1, 2, 3, 4, 5)
                    |       .filter { n -> n % 2 == 1 }
                    |       .map {
                    |           val x = 0
                    |           val y = x + 1
                    |           val z = y + 1
                    |           it + z
                    |   
                    |   
                    |   
                    |   
                    |   }
                    |}
                """.trimMargin(),
            LintError(4, 13, ruleId, "${Warnings.TOO_MANY_LINES_IN_LAMBDA.warnText()} max length lambda without arguments is 3, but you have 6", false),
            rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.TOO_MANY_LINES_IN_LAMBDA)
    fun `lambda in lambda`() {
        lintMethod(
            """
                    |fun foo() {
                    |   val list = listOf(listOf(1,2,3), listOf(4,5,6))
                    |       .map {l -> l.map {
                    |           val x = 0
                    |           val y = x + 1
                    |           val z = y + 1
                    |           println(it)
                    |           }
                    |       }
                    |   }
                """.trimMargin(),
            LintError(3, 25, ruleId, "${Warnings.TOO_MANY_LINES_IN_LAMBDA.warnText()} max length lambda without arguments is 3, but you have 6", false),
            rulesConfigList = rulesConfigList
        )
    }
}
