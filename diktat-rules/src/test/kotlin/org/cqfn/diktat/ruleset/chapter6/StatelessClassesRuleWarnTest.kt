package org.cqfn.diktat.ruleset.chapter6

import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.classes.StatelessClassesRule
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames.OBJECT_IS_PREFERRED
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class StatelessClassesRuleWarnTest : LintTestBase(::StatelessClassesRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:stateless-class"

    @Test
    @Tag(OBJECT_IS_PREFERRED)
    fun `should not trigger on class not extending any interface`() {
        lintMethod(
            """
            |class Some : I() {
            |   override fun some()
            |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(OBJECT_IS_PREFERRED)
    fun `should trigger on class extending interface`() {
        lintMethod(
            """
            |class Some : I {
            |   override fun some()
            |}
            |
            |interface I {
            |   fun some()
            |}
            """.trimMargin(),
            LintError(1, 1, ruleId, "${Warnings.OBJECT_IS_PREFERRED.warnText()} class Some", true)
        )
    }

    @Test
    @Tag(OBJECT_IS_PREFERRED)
    fun `should not trigger on class with constructor`() {
        lintMethod(
            """
            |class Some(b: Int) : I {
            |   
            |   override fun some()
            |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(OBJECT_IS_PREFERRED)
    fun `should not trigger on class with no interface in this file`() {
        lintMethod(
            """
            |class Some : I {
            |   
            |   override fun some()
            |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(OBJECT_IS_PREFERRED)
    fun `should not trigger on class with state`() {
        lintMethod(
            """
            |class Some : I {
            |   val a = 5
            |   override fun some()
            |}
            """.trimMargin()
        )
    }
}
