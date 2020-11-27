package org.cqfn.diktat.ruleset.chapter6

import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_NAME_OF_VARIABLE_INSIDE_ACCESSOR
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.PropertyAccessorFields
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class PropertyAccessorFieldsWarnTest : LintTestBase(::PropertyAccessorFields) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:getter-setter-fields"

    @Test
    @Tag(WarningNames.WRONG_NAME_OF_VARIABLE_INSIDE_ACCESSOR)
    fun `check simple correct examples`() {
        lintMethod(
            """
                    |class A {
                    |
                    |   var isEmpty: Boolean = false
                    |   set(value) {
                    |       println("Side effect")
                    |       field = value
                    |   }
                    |   get() = field
                    |   
                    |   var isNotEmpty: Boolean = true
                    |   set(value) {
                    |       val q = isEmpty.and(true)
                    |       field = value
                    |   }
                    |   get() {
                    |       println(12345)
                    |       return field
                    |   }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NAME_OF_VARIABLE_INSIDE_ACCESSOR)
    fun `check wrong setter and getter examples`() {
        lintMethod(
            """
                    |class A {
                    |
                    |   var isEmpty: Boolean = false
                    |   set(values) {
                    |       println("Side effect")
                    |       isEmpty = values
                    |   }
                    |   
                    |   var isNotEmpty: Boolean = true
                    |   set(value) {
                    |       val q = isNotEmpty.and(true)
                    |       field = value
                    |   }
                    |   get() {
                    |       println(12345)
                    |       return isNotEmpty
                    |   }
                    |   
                    |   var isNotOk: Boolean = false
                    |   set(values) {
                    |       this.isNotOk = values
                    |   }
                    |}
                """.trimMargin(),
            LintError(4, 4, ruleId, "${WRONG_NAME_OF_VARIABLE_INSIDE_ACCESSOR.warnText()} set(values) {..."),
            LintError(14, 4, ruleId, "${WRONG_NAME_OF_VARIABLE_INSIDE_ACCESSOR.warnText()} get() {..."),
            LintError(20, 4, ruleId, "${WRONG_NAME_OF_VARIABLE_INSIDE_ACCESSOR.warnText()} set(values) {...")
        )
    }

    @Test
    @Tag(WarningNames.WRONG_NAME_OF_VARIABLE_INSIDE_ACCESSOR)
    @Suppress("TOO_LONG_FUNCTION")
    fun `check examples with local var`() {
        lintMethod(
            """
                    |class A {
                    |
                    |   var isEmpty: Boolean = false
                    |   set(values) {
                    |       fun foo() {
                    |           val isEmpty = false
                    |       }
                    |       isEmpty = values
                    |   }
                    |   
                    |   var isNotOk: Boolean = false
                    |   set(valuess) {
                    |       var isNotOk = true
                    |       isNotOk = valuess
                    |   }
                    |   
                    |   var isOk: Boolean = false
                    |   set(valuess) {
                    |       isOk = valuess
                    |       var isOk = true
                    |   }
                    |   
                    |   var isNotEmpty: Boolean = true
                    |   set(value) {
                    |       val q = isNotEmpty
                    |       field = value
                    |   }
                    |   get() = field
                    |}
                """.trimMargin(),
            LintError(4, 4, ruleId, "${WRONG_NAME_OF_VARIABLE_INSIDE_ACCESSOR.warnText()} set(values) {..."),
            LintError(18, 4, ruleId, "${WRONG_NAME_OF_VARIABLE_INSIDE_ACCESSOR.warnText()} set(valuess) {..."),
            LintError(24, 4, ruleId, "${WRONG_NAME_OF_VARIABLE_INSIDE_ACCESSOR.warnText()} set(value) {...")
        )
    }
}
