package org.cqfn.diktat.ruleset.chapter1

import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.ruleset.constants.Warnings.FUNCTION_BOOLEAN_PREFIX
import org.cqfn.diktat.ruleset.constants.Warnings.FUNCTION_NAME_INCORRECT_CASE
import org.junit.jupiter.api.Test
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.IdentifierNaming
import org.cqfn.diktat.util.lintMethod

class MethodNamingWarnTest {

    private val ruleId: String = "$DIKTAT_RULE_SET_ID:identifier-naming"

    @Test
    fun `method name incorrect, part 1`() {
        val code =
            """
                  class SomeClass {
                    fun /* */ methODTREE(): String {

                    }
                  }
                """.trimIndent()
        lintMethod(IdentifierNaming(), code, LintError(2, 13, ruleId, "${FUNCTION_NAME_INCORRECT_CASE.warnText()} methODTREE", true))
    }

    @Test
    fun `method name incorrect, part 2`() {
        val code =
            """
                  class TestPackageName {
                    fun method_two(): String {
                        return ""
                    }
                  }
                """.trimIndent()
        lintMethod(IdentifierNaming(), code, LintError(2, 7, ruleId, "${FUNCTION_NAME_INCORRECT_CASE.warnText()} method_two", true))
    }

    @Test
    fun `method name incorrect, part 3`() {
        val code =
            """
                    fun String.methODTREE(): String {
                        fun TEST(): Unit {
                            return ""
                        }
                    }
                """.trimIndent()
        lintMethod(IdentifierNaming(), code,
                LintError(1, 12, ruleId, "${FUNCTION_NAME_INCORRECT_CASE.warnText()} methODTREE", true),
                LintError(2, 9, ruleId, "${FUNCTION_NAME_INCORRECT_CASE.warnText()} TEST", true)
        )
    }

    @Test
    fun `method name incorrect, part 4`() {
        val code =
            """
                  class TestPackageName {
                    fun methODTREE(): String {
                    }
                  }
                """.trimIndent()
        lintMethod(IdentifierNaming(), code, LintError(2, 7, ruleId, "${FUNCTION_NAME_INCORRECT_CASE.warnText()} methODTREE", true))
    }

    @Test
    fun `method name incorrect, part 5`() {
        val code =
            """
                  class TestPackageName {
                    fun methODTREE() {
                    }
                  }
                """.trimIndent()
        lintMethod(IdentifierNaming(), code, LintError(2, 7, ruleId, "${FUNCTION_NAME_INCORRECT_CASE.warnText()} methODTREE", true))
    }

    @Test
    fun `boolean method name incorrect`() {
        val code =
            """
                 fun someBooleanCheck(): Boolean {
                     return false
                 }
                """.trimIndent()
        lintMethod(IdentifierNaming(), code, LintError(1, 5, ruleId, "${FUNCTION_BOOLEAN_PREFIX.warnText()} someBooleanCheck", true))
    }
}
