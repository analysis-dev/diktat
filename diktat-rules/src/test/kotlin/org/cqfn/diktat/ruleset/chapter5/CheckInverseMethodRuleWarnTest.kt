package org.cqfn.diktat.ruleset.chapter5

import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.CheckInverseMethodRule
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames.INVERSE_FUNCTION_PREFERRED
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class CheckInverseMethodRuleWarnTest : LintTestBase(::CheckInverseMethodRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:inverse-method"

    @Test
    @Tag(INVERSE_FUNCTION_PREFERRED)
    fun `should not raise warning`() {
        lintMethod(
            """
                    |fun some() {
                    |   if (list.isEmpty()) {
                    |       // some cool logic 
                    |   }
                    |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(INVERSE_FUNCTION_PREFERRED)
    fun `should raise warning`() {
        lintMethod(
            """
                    |fun some() {
                    |   if (!list.isEmpty()) {
                    |       // some cool logic 
                    |   }
                    |}
            """.trimMargin(),
            LintError(2, 14, ruleId, "${Warnings.INVERSE_FUNCTION_PREFERRED.warnText()} isNotEmpty() instead of !isEmpty()", true)
        )
    }

    @Test
    @Tag(INVERSE_FUNCTION_PREFERRED)
    fun `should consider white spaces between ! and call expression`() {
        lintMethod(
            """
                    |fun some() {
                    |   if (!  list.isEmpty()) {
                    |       // some cool logic 
                    |   }
                    |}
            """.trimMargin(),
            LintError(2, 16, ruleId, "${Warnings.INVERSE_FUNCTION_PREFERRED.warnText()} isNotEmpty() instead of !isEmpty()", true)
        )
    }

    @Test
    @Tag(INVERSE_FUNCTION_PREFERRED)
    fun `should consider comments between ! and call expression`() {
        lintMethod(
            """
                    |fun some() {
                    |   if (! /*cool comment*/ list.isEmpty()) {
                    |       // some cool logic 
                    |   }
                    |}
            """.trimMargin(),
            LintError(2, 32, ruleId, "${Warnings.INVERSE_FUNCTION_PREFERRED.warnText()} isNotEmpty() instead of !isEmpty()", true)
        )
    }
}
