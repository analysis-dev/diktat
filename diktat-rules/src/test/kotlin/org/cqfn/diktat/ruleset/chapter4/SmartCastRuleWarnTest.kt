package org.cqfn.diktat.ruleset.chapter4

import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.SmartCastRule
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames.SMART_CAST_NEEDED
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class SmartCastRuleWarnTest : LintTestBase(::SmartCastRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:smart-cast-rule"

    @Test
    @Tag(SMART_CAST_NEEDED)
    fun `if with is smart cast good`() {
        lintMethod(
            """
                    |class Test {
                    |   val x = ""
                    |   fun someFun() {
                    |       if (x is String) {
                    |           val a = x.length
                    |       }
                    |   }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(SMART_CAST_NEEDED)
    fun `if with is smart cast bad`() {
        lintMethod(
            """
                    |class Test {
                    |   val x = ""
                    |   fun someFun() {
                    |       if (x is String) {
                    |           val a = (x as String).length
                    |       }
                    |   }
                    |}
                """.trimMargin(),
            LintError(5, 21, ruleId, "${Warnings.SMART_CAST_NEEDED.warnText()} x as String", true)
        )
    }

    @Test
    @Tag(SMART_CAST_NEEDED)
    fun `if with another if with is smart cast good`() {
        lintMethod(
            """
                    |class Test {
                    |   val x = ""
                    |   fun someFun() {
                    |       if (x is String) {
                    |           val a = x.length
                    |           if (x is Int) {
                    |               val a = x.value
                    |           }
                    |       }
                    |   }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(SMART_CAST_NEEDED)
    fun `if with another if with is smart cast bad`() {
        lintMethod(
            """
                    |class Test {
                    |   val x = ""
                    |   fun someFun() {
                    |       if (x is String) {
                    |           val a = x.length
                    |           if (x is Int) {
                    |               val a = (x as Int).value
                    |           }
                    |       }
                    |   }
                    |}
                """.trimMargin(),
            LintError(7, 25, ruleId, "${Warnings.SMART_CAST_NEEDED.warnText()} x as Int", true)
        )
    }

    @Test
    @Tag(SMART_CAST_NEEDED)
    fun `smart cast in else good`() {
        lintMethod(
            """
                    |class Test {
                    |   val x = ""
                    |   fun someFun() {
                    |       if (x !is String) {
                    |           val a = (x as String).length
                    |       } else {
                    |           val b = (x as String).length
                    |       }
                    |   }
                    |}
                """.trimMargin(),
            LintError(7, 21, ruleId, "${Warnings.SMART_CAST_NEEDED.warnText()} x as String", true)
        )
    }

    @Test
    @Tag(SMART_CAST_NEEDED)
    fun `smart cast in if without braces bad`() {
        lintMethod(
            """
                    |class Test {
                    |   val x = ""
                    |   fun someFun() {
                    |       if (x is String)
                    |           print((x as String).length)
                    |   }
                    |}
                """.trimMargin(),
            LintError(5, 19, ruleId, "${Warnings.SMART_CAST_NEEDED.warnText()} x as String", true)
        )
    }

    @Test
    @Tag(SMART_CAST_NEEDED)
    fun `smart cast in if without braces good`() {
        lintMethod(
            """
                    |class Test {
                    |   val x = ""
                    |   fun someFun() {
                    |       if (x is String)
                    |           print(x.length)
                    |   }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(SMART_CAST_NEEDED)
    fun `smart cast in else without braces good`() {
        lintMethod(
            """
                    |class Test {
                    |   val x = ""
                    |   fun someFun() {
                    |       if (x !is String) {
                    |           print("asd")
                    |       }
                    |       else
                    |           print((x as String).length)
                    |   }
                    |}
                """.trimMargin(),
            LintError(8, 19, ruleId, "${Warnings.SMART_CAST_NEEDED.warnText()} x as String", true)
        )
    }

    @Test
    @Tag(SMART_CAST_NEEDED)
    fun `smart cast in else nested bad`() {
        lintMethod(
            """
                    |class Test {
                    |   val x = ""
                    |   fun someFun() {
                    |       if (x !is String) {
                    |           print("asd")
                    |       }
                    |       else {
                    |           print((x as String).length)
                    |           val a = ""
                    |           if (a !is String) {
                    |           
                    |           } else {
                    |               print((a as String).length)
                    |           }
                    |       }
                    |   }
                    |}
                """.trimMargin(),
            LintError(8, 19, ruleId, "${Warnings.SMART_CAST_NEEDED.warnText()} x as String", true),
            LintError(13, 23, ruleId, "${Warnings.SMART_CAST_NEEDED.warnText()} a as String", true)
        )
    }

    @Test
    @Tag(SMART_CAST_NEEDED)
    fun `smart cast in when bad`() {
        lintMethod(
            """
                    |class Test {
                    |   val x = ""
                    |   fun someFun() {
                    |       when (x) {
                    |           is Int -> print((x as Int).length)
                    |           is String -> print("String")
                    |           is Long -> x as Int
                    |       }
                    |   }
                    |}
                """.trimMargin(),
            LintError(5, 29, ruleId, "${Warnings.SMART_CAST_NEEDED.warnText()} x as Int", true)
        )
    }

    @Test
    @Tag(SMART_CAST_NEEDED)
    fun `smart cast in when good`() {
        lintMethod(
            """
                    |class Test {
                    |   val x = ""
                    |   fun someFun() {
                    |       when (x) {
                    |           is Int -> print(x.length)
                    |           is String -> print("String")
                    |       }
                    |   }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(SMART_CAST_NEEDED)
    fun `if with multiple is good`() {
        lintMethod(
            """
                    |class Test {
                    |   val x = ""
                    |   val y = 3
                    |   fun someFun() {
                    |       if (x is String || y is Int) {
                    |           val a = x.length
                    |           val b = y.value
                    |       }
                    |   }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(SMART_CAST_NEEDED)
    fun `if with multiple is bad`() {
        lintMethod(
            """
                    |class Test {
                    |   val x = ""
                    |   val y = 3
                    |   fun someFun() {
                    |       if (x is String || y is Int) {
                    |           val a = (x as String).length
                    |           val b = (y as Int).value
                    |       }
                    |   }
                    |}
                """.trimMargin(),
            LintError(6, 21, ruleId, "${Warnings.SMART_CAST_NEEDED.warnText()} x as String", true),
            LintError(7, 21, ruleId, "${Warnings.SMART_CAST_NEEDED.warnText()} y as Int", true)
        )
    }

    @Test
    @Tag(SMART_CAST_NEEDED)
    fun `if with function condition`() {
        lintMethod(
            """
                    |class Test {
                    |   val x = ""
                    |   val list = listOf(1,2,3)
                    |   fun someFun() {
                    |       if (list.filter { it is Foo }.all { it.bar() }) {
                    |           val a = x.length
                    |           val b = y.value
                    |       }
                    |   }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(SMART_CAST_NEEDED)
    fun `if with shadowed var good`() {
        lintMethod(
            """
                    |class Test {
                    |   val x = ""
                    |   fun someFun() {
                    |       if (x is String) {
                    |           val x = 5
                    |           val a = (x as String).length
                    |       }
                    |   }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(SMART_CAST_NEEDED)
    fun `if with shadowed var bad`() {
        lintMethod(
            """
                    |class Test {
                    |   val x = ""
                    |   fun someFun() {
                    |       if (x is String) {
                    |           val x = 5
                    |           val a = (x as String).length
                    |           if (x is Int) {
                    |               val b = (x as Int).value
                    |           }
                    |       }
                    |   }
                    |}
                """.trimMargin(),
            LintError(8, 25, ruleId, "${Warnings.SMART_CAST_NEEDED.warnText()} x as Int", true)
        )
    }

    @Test
    @Tag(SMART_CAST_NEEDED)
    fun `if with shadowed var bad 2`() {
        lintMethod(
            """
                    |class Test {
                    |   val x = ""
                    |   fun someFun() {
                    |       if (x is String) {
                    |           val x = 5
                    |           val a = (x as Int).length
                    |       }
                    |   }
                    |}
                """.trimMargin(),
            LintError(6, 21, ruleId, "${Warnings.SMART_CAST_NEEDED.warnText()} x as Int", true)
        )
    }
}
