package org.cqfn.diktat.ruleset.chapter6

import org.cqfn.diktat.ruleset.constants.Warnings.AVOID_USING_UTILITY_CLASS
import org.cqfn.diktat.ruleset.rules.AvoidUtilityClass
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class AvoidUtilityClassWarnTest : LintTestBase(::AvoidUtilityClass) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:avoid-utility-class"

    @Test
    @Tag(WarningNames.AVOID_USING_UTILITY_CLASS)
    fun `simple test`() {
        lintMethod(
            """
                    |object StringUtil {
                    |   fun stringInfo(myString: String): Int {
                    |       return myString.count{ "something".contains(it) }
                    |   }
                    |}   
                    |
                    |class A() {
                    |   fun foo() { }
                    |}
                    |
                    |class StringUtils {
                    |   fun goo(tex: String): Int {
                    |       return myString.count{ "something".contains(it) }
                    |   }
                    |}
                    |
                    |class StringUtil {
                    |   val z = "hello"
                    |   fun goo(tex: String): Int {
                    |       return myString.count{ "something".contains(it) }
                    |   }
                    |}
                """.trimMargin(),
            LintError(1, 1, ruleId, "${AVOID_USING_UTILITY_CLASS.warnText()} StringUtil"),
            LintError(11, 1, ruleId, "${AVOID_USING_UTILITY_CLASS.warnText()} StringUtils")
        )
    }

    @Test
    @Tag(WarningNames.AVOID_USING_UTILITY_CLASS)
    fun `test with comment anf companion`() {
        lintMethod(
            """
                    |
                    |class StringUtils {
                    |   companion object  {
                    |       private val name = "Hello"
                    |   }
                    |   /**
                    |    * @param tex
                    |    */
                    |   fun goo(tex: String): Int {
                    |       //hehe
                    |       return myString.count{ "something".contains(it) }
                    |   }
                    |}
                    |
                    |class StringUtil {
                    |   /*
                    |
                    |    */
                    |   companion object  {
                    |   }
                    |   val z = "hello"
                    |   fun goo(tex: String): Int {
                    |       return myString.count{ "something".contains(it) }
                    |   }
                    |}
                """.trimMargin(),
            LintError(2, 1, ruleId, "${AVOID_USING_UTILITY_CLASS.warnText()} StringUtils")
        )
    }

    @Test
    @Tag(WarningNames.AVOID_USING_UTILITY_CLASS)
    fun `test with class without identifier`() {
        lintMethod(
            """
                    fun foo() {
                        window.addMouseListener(object : MouseAdapter() {
                            override fun mouseClicked(e: MouseEvent) { /*...*/ }
                        
                            override fun mouseEntered(e: MouseEvent) { /*...*/ }
                        })
                    }
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.AVOID_USING_UTILITY_CLASS)
    fun `check test-class`() {
        lintMethod(
            """
                    |class StringUtils {
                    |   fun goo(tex: String): Int {
                    |       return myString.count{ "something".contains(it) }
                    |   }
                    |}
                """.trimMargin(),
            LintError(1, 1, ruleId, "${AVOID_USING_UTILITY_CLASS.warnText()} StringUtils"),
            fileName = "src/main/kotlin/org/cqfn/diktat/Example.kt",
        )
        lintMethod(
            """
                    |@Test
                    |class StringUtils1 {
                    |   fun goo(tex: String): Int {
                    |       return myString.count{ "something".contains(it) }
                    |   }
                    |}
                """.trimMargin(),
            fileName = "src/test/kotlin/org/cqfn/diktat/Example.kt"
        )
        lintMethod(
            """
                    |class StringUtils2 {
                    |   fun goo(tex: String): Int {
                    |       return myString.count{ "something".contains(it) }
                    |   }
                    |}
                """.trimMargin(),
            fileName = "src/test/kotlin/org/cqfn/diktat/UtilTest.kt"
        )
    }
}
