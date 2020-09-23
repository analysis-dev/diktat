package org.cqfn.diktat.ruleset.chapter3

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import generated.WarningNames.STRING_TEMPLATE_CURLY_BRACES
import generated.WarningNames.STRING_TEMPLATE_QUOTES
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.StringTemplateFormatRule
import org.cqfn.diktat.util.LintTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class StringTemplateRuleWarnTest : LintTestBase(::StringTemplateFormatRule) {

    private val ruleId = "$DIKTAT_RULE_SET_ID:string-template-format"

    @Test
    @Tag(STRING_TEMPLATE_CURLY_BRACES)
    fun `long string template good example`() {
        lintMethod(
                """
                    |class Some { 
                    |   val template = "${'$'}{::String} ${'$'}{asd.moo()}"
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(STRING_TEMPLATE_CURLY_BRACES)
    fun `long string template bad example`() {
        lintMethod(
                """
                    |class Some { 
                    |   val template = "${'$'}{a} ${'$'}{asd.moo()}"
                    |   val some = "${'$'}{1.0}"
                    |}
                """.trimMargin(),
                LintError(2, 20, ruleId, "${Warnings.STRING_TEMPLATE_CURLY_BRACES.warnText()} ${'$'}{a}", true),
                LintError(3, 16, ruleId, "${Warnings.STRING_TEMPLATE_CURLY_BRACES.warnText()} ${'$'}{1.0}", true)
        )
    }

    @Test
    @Tag(STRING_TEMPLATE_QUOTES)
    fun `short string template bad example`() {
        lintMethod(
                """
                    |class Some { 
                    |   val template = "${'$'}a"
                    |   val z = a
                    |}
                """.trimMargin(),
                LintError(2, 20, ruleId, "${Warnings.STRING_TEMPLATE_QUOTES.warnText()} ${'$'}a", true)
        )
    }

    @Test
    @Tag(STRING_TEMPLATE_CURLY_BRACES)
    fun `long string template bad example 2`() {
        lintMethod(
                """
                    |class Some {
                    |   fun some() {
                    |       val s = "abs"
                    |       println("${'$'}{s}.length is ${'$'}{s.length}")
                    |
                    |   }
                    |}
                """.trimMargin(),
                LintError(4, 17, ruleId, "${Warnings.STRING_TEMPLATE_CURLY_BRACES.warnText()} ${'$'}{s}", true)
        )
    }

    @Test
    @Tag(STRING_TEMPLATE_QUOTES)
    fun `should not trigger`() {
        lintMethod(
                """
                    |class Some {
                    |   fun some() {
                    |       val price = ""${'"'}
                    |       ${'$'}9.99
                    |       ""${'"'}
                    |   }
                    |}
                """.trimMargin()
        )
    }
}
