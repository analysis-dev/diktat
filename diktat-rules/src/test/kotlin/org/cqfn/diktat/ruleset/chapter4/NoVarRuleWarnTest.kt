package org.cqfn.diktat.ruleset.chapter4

import com.pinterest.ktlint.core.LintError
import generated.WarningNames.SAY_NO_TO_VAR
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.ImmutableValNoVarRule
import org.cqfn.diktat.util.LintTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class NoVarRuleWarnTest : LintTestBase(::ImmutableValNoVarRule) {

    private val ruleId = "$DIKTAT_RULE_SET_ID:no-var-rule"

    @Test
    @Tag(SAY_NO_TO_VAR)
    fun `valid case where x is used in while loop as some counter`() {
        lintMethod(
                """
                    | fun foo() { 
                    |     var x = 0
                    |     while (x < 10) {
                    |        x++
                    |     }
                    | }
                """.trimMargin()
        )
    }

    @Test
    @Tag(SAY_NO_TO_VAR)
    fun `valid case where y is used in for each loop as some counter, but a is not`() {
        lintMethod(
                """
                    | fun foo() { 
                    |     var a = emptyList()
                    |     var y = 0
                    |     a.forEach { x ->
                    |        y = x + 1
                    |     }
                    | }
                """.trimMargin(),
                LintError(2, 6, ruleId, "${Warnings.SAY_NO_TO_VAR.warnText()} var a = emptyList()", false)
        )
    }

    @Test
    @Tag(SAY_NO_TO_VAR)
    fun `For loop with internal counter`() {
        lintMethod(
                """
                    | fun foo() { 
                    |     for (x in 0..10) println(x)
                    | }
                """.trimMargin()
        )
    }

    @Test
    @Tag(SAY_NO_TO_VAR)
    fun `var in class`() {
        lintMethod(
                """
                    | class A { 
                    |     var a = 0
                    | }
                """.trimMargin()
        )
    }

    @Test
    @Tag(SAY_NO_TO_VAR)
    fun `var used simply in function`() {
        lintMethod(
                """
                    | fun foo(): Int {
                    |     var a = 0
                    |     a = a + 15
                    |     a = a + 56
                    |     return a
                    | }
                """.trimMargin(),
                LintError(2, 6, ruleId, "${Warnings.SAY_NO_TO_VAR.warnText()} var a = 0", false)
        )
    }
}
