package org.cqfn.diktat.ruleset.chapter2

import org.cqfn.diktat.common.config.rules.DIKTAT_COMMON
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_TRIVIAL_KDOC_ON_FUNCTION
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_WITHOUT_PARAM_TAG
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_WITHOUT_RETURN_TAG
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_WITHOUT_THROWS_TAG
import org.cqfn.diktat.ruleset.constants.Warnings.MISSING_KDOC_ON_FUNCTION
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.kdoc.KdocMethods
import org.cqfn.diktat.util.LintTestBase

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test

class KdocMethodsTest : LintTestBase(::KdocMethods) {
    private val ruleId: String = "$DIKTAT_RULE_SET_ID:kdoc-methods"
    private val funCode = """
        fun doubleInt(a: Int): Int {
            if (Config.condition) throw IllegalStateException()
            return 2 * a
        }
    """.trimIndent()

    @Test
    @Tags(Tag(WarningNames.KDOC_WITHOUT_PARAM_TAG), Tag(WarningNames.KDOC_WITHOUT_RETURN_TAG), Tag(WarningNames.KDOC_WITHOUT_THROWS_TAG))
    fun `Accessible methods with parameters, return type and throws should have proper KDoc (positive example)`() {
        val validCode = """
            /**
             * Test method
             * @param a - dummy integer
             * @return doubled value
             * @throws IllegalStateException
             */
            $funCode
        """.trimIndent()

        lintMethod(validCode)
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_TOP_LEVEL)
    fun `Warning should not be triggered for private functions`() {
        val validCode = "private $funCode"

        lintMethod(validCode)
    }

    @Test
    @Tags(Tag(WarningNames.KDOC_WITHOUT_PARAM_TAG), Tag(WarningNames.KDOC_WITHOUT_RETURN_TAG), Tag(WarningNames.KDOC_WITHOUT_THROWS_TAG),
        Tag(WarningNames.MISSING_KDOC_ON_FUNCTION))
    fun `Warning should not be triggered for functions in tests`() {
        val validCode = "@Test $funCode"
        val complexAnnotationCode = "@Anno(test = [\"args\"]) $funCode"

        // do not force KDoc on annotated function
        lintMethod(validCode, fileName = "src/main/kotlin/org/cqfn/diktat/Example.kt")
        // no false positive triggers on annotations
        lintMethod(complexAnnotationCode,
            LintError(1, 1, ruleId, "${KDOC_WITHOUT_PARAM_TAG.warnText()} doubleInt (a)", true),
            LintError(1, 1, ruleId, "${KDOC_WITHOUT_RETURN_TAG.warnText()} doubleInt", true),
            LintError(1, 1, ruleId, "${KDOC_WITHOUT_THROWS_TAG.warnText()} doubleInt (IllegalStateException)", true),
            LintError(1, 1, ruleId, "${MISSING_KDOC_ON_FUNCTION.warnText()} doubleInt", true),
            fileName = "src/main/kotlin/org/cqfn/diktat/Example.kt"
        )
        // should check all .kt files unless both conditions on location and name are true
        lintMethod(funCode,
            LintError(1, 1, ruleId, "${KDOC_WITHOUT_PARAM_TAG.warnText()} doubleInt (a)", true),
            LintError(1, 1, ruleId, "${KDOC_WITHOUT_RETURN_TAG.warnText()} doubleInt", true),
            LintError(1, 1, ruleId, "${KDOC_WITHOUT_THROWS_TAG.warnText()} doubleInt (IllegalStateException)", true),
            LintError(1, 1, ruleId, "${MISSING_KDOC_ON_FUNCTION.warnText()} doubleInt", true),
            fileName = "src/test/kotlin/org/cqfn/diktat/Example.kt"
        )
        // should allow to set custom test dirs
        lintMethod(funCode, fileName = "src/jvmTest/kotlin/org/cqfn/diktat/ExampleTest.kt",
            rulesConfigList = listOf(RulesConfig(DIKTAT_COMMON, true, mapOf("testDirs" to "test,jvmTest")))
        )
    }

    @Test
    @Tag(WarningNames.KDOC_WITHOUT_PARAM_TAG)
    fun `Empty parameter list should not trigger warning about @param`() {
        val validCode = """
            /**
             * Test method
             * @return zero
             * @throws IllegalStateException
             */
            fun foo(): Int {
                return 0
            }
        """.trimIndent()

        lintMethod(validCode)
    }

    @Test
    @Tag(WarningNames.KDOC_WITHOUT_PARAM_TAG)
    fun `All methods with parameters should have @param KDoc`() {
        val invalidKdoc = """
            /**
             * Test method
             * @return doubled value
             * @throws IllegalStateException
             */
        """.trimIndent()
        val invalidCode = """
            $invalidKdoc
            $funCode
        """.trimIndent()

        lintMethod(invalidCode,
            LintError(1, 13, ruleId, "${KDOC_WITHOUT_PARAM_TAG.warnText()} doubleInt (a)", true)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_WITHOUT_PARAM_TAG)
    fun `All methods with parameters should have @param KDoc for each parameter`() {
        val invalidKdoc = """
            /**
             * Test method
             * @param a - dummy integer
             * @return doubled value
             * @throws IllegalStateException
             */
        """.trimIndent()
        val invalidCode = """
            $invalidKdoc
            fun addInts(a: Int, b: Int): Int = a + b
        """.trimIndent()

        lintMethod(invalidCode,
            LintError(1, 12, ruleId, "${KDOC_WITHOUT_PARAM_TAG.warnText()} addInts (b)", true)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_WITHOUT_RETURN_TAG)
    fun `All methods with explicit return type excluding Unit should have @return KDoc`() {
        val invalidKdoc = """
            /**
             * Test method
             * @param a - dummy integer
             * @throws IllegalStateException
             */
        """.trimIndent()
        val invalidCode = """
            $invalidKdoc
            $funCode
        """.trimIndent()

        lintMethod(invalidCode,
            LintError(1, 13, ruleId, "${KDOC_WITHOUT_RETURN_TAG.warnText()} doubleInt", true)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_WITHOUT_RETURN_TAG)
    fun `All methods with expression body should have @return tag or explicitly set return type to Unit`() {
        val kdocWithoutReturn = """
            /**
             * Test method
             * @param a - dummy integer
             * @throws IllegalStateException
             */
        """.trimIndent()

        val invalidCode = """
            $kdocWithoutReturn
            fun foo(a: Int) = bar(2 * a)

            $kdocWithoutReturn
            fun bar(a: Int): Unit = this.list.add(a)

            private val list = mutableListOf<Int>()
        """.trimIndent()

        lintMethod(invalidCode,
            LintError(1, 12, ruleId, "${KDOC_WITHOUT_RETURN_TAG.warnText()} foo", true)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_WITHOUT_THROWS_TAG)
    fun `All methods with throw in method body should have @throws KDoc`() {
        val invalidKdoc = """
            /**
             * Test method
             * @param a - dummy integer
             * @return doubled value
             */
        """.trimIndent()
        val invalidCode = """
            $invalidKdoc
            $funCode
        """.trimIndent()

        lintMethod(invalidCode,
            LintError(1, 13, ruleId, "${KDOC_WITHOUT_THROWS_TAG.warnText()} doubleInt (IllegalStateException)", true)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_WITHOUT_THROWS_TAG)
    fun `Linter shouldn't detect throws inside comments`() {
        val invalidKdoc = """
            /**
             * Test method
             * @param a - dummy integer
             * @return doubled value
             */
        """.trimIndent()
        val invalidCode = """
            $invalidKdoc
            fun foo(a: Int) {
                // throw Exception()
                return bar
            }
        """.trimIndent()

        lintMethod(invalidCode)
    }

    @Test
    @Tag(WarningNames.KDOC_WITHOUT_THROWS_TAG)
    fun `All thrown exceptions should be in KDoc`() {
        val invalidCode = """
            /**
             * Test method
             * @param a - dummy integer
             * @return doubled value
             * @throws IllegalStateException
             */
            fun doubleInt(a: Int): Int {
                if (Config.condition) throw IllegalStateException()
                if (Config.condition2) throw IllegalAccessException()
                return 2 * a
            }
        """.trimIndent()

        lintMethod(invalidCode,
            LintError(1, 1, ruleId, "${KDOC_WITHOUT_THROWS_TAG.warnText()} doubleInt (IllegalAccessException)", true)
        )
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_TOP_LEVEL)
    fun `do not force documentation on standard methods`() {
        lintMethod(
            """
                    |class Example {
                    |    override fun toString() = "example"
                    |    
                    |    override fun equals(other: Any?) = false
                    |    
                    |    override fun hashCode() = 42
                    |}
                    |
                    |fun main() { }
                """.trimMargin()
        )

        lintMethod(
            """
                    |class Example {
                    |    override fun toString(): String { return "example" }
                    |    
                    |    override fun equals(other: Any?): Boolean { return false }
                    |    
                    |    override fun hashCode(): Int { return 42 }
                    |}
                    |
                    |fun main(vararg args: String) { }
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_ON_FUNCTION)
    fun `should not force documentation on single line getters and setters`() {
        lintMethod(
            """
                    |class Example {
                    |    fun setX(x: Type) {
                    |        this.x = x
                    |    }
                    |    
                    |    fun getX() {
                    |        return x
                    |    }
                    |    
                    |    fun getY() = this.y
                    |    
                    |    fun setY(y: Type) {
                    |        this.validate(y)
                    |        this.y = y
                    |    }
                    |    
                    |    fun getZ(): TypeZ {
                    |        baz(z)
                    |        return z
                    |    }
                    |}
                """.trimMargin(),
            LintError(10, 5, ruleId, "${MISSING_KDOC_ON_FUNCTION.warnText()} getY", false),
            LintError(12, 5, ruleId, "${KDOC_WITHOUT_PARAM_TAG.warnText()} setY (y)", true),
            LintError(12, 5, ruleId, "${MISSING_KDOC_ON_FUNCTION.warnText()} setY", true),
            LintError(17, 5, ruleId, "${KDOC_WITHOUT_RETURN_TAG.warnText()} getZ", true),
            LintError(17, 5, ruleId, "${MISSING_KDOC_ON_FUNCTION.warnText()} getZ", true)
        )
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_ON_FUNCTION)
    fun `regression - warn about missing KDoc even if it cannot be autocorrected`() {
        lintMethod(
            """
                    |fun foo() { }
                """.trimMargin(),
            LintError(1, 1, ruleId, "${MISSING_KDOC_ON_FUNCTION.warnText()} foo", false)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_TRIVIAL_KDOC_ON_FUNCTION)
    fun `should check if KDoc is not trivial`() {
        lintMethod(
            """
                    |/**
                    | * Returns X
                    | */
                    |fun getX(): TypeX { return x }
                """.trimMargin(),
            LintError(2, 3, ruleId, "${KDOC_TRIVIAL_KDOC_ON_FUNCTION.warnText()} Returns X", false)
        )
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_ON_FUNCTION)
    fun `should not trigger on override funcs`() {
        lintMethod(
            """
                    |class Some : A {
                    |   override fun foo() {}
                    |   
                    |   override fun bar(t: T): U { return U() }
                    |}
                """.trimMargin()
        )
    }
}
