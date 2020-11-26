package org.cqfn.diktat.ruleset.chapter2

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.junit.jupiter.api.Test
import org.cqfn.diktat.ruleset.constants.Warnings.*
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.kdoc.KdocComments
import org.cqfn.diktat.util.LintTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags

class KdocCommentsWarnTest : LintTestBase(::KdocComments) {
    private val ruleId: String = "$DIKTAT_RULE_SET_ID:kdoc-comments"

    @Test
    @Tag(WarningNames.MISSING_KDOC_TOP_LEVEL)
    fun `all public classes should be documented with KDoc`() {
        val code =
                """
                    class SomeGoodName {
                        private class InternalClass {
                        }
                    }

                    public open class SomeOtherGoodName {
                    }

                    open class SomeNewGoodName {
                    }

                    public class SomeOtherNewGoodName {
                    }

                """.trimIndent()
        lintMethod(code,
                LintError(1, 1, ruleId, "${MISSING_KDOC_TOP_LEVEL.warnText()} SomeGoodName"),
                LintError(6, 1, ruleId, "${MISSING_KDOC_TOP_LEVEL.warnText()} SomeOtherGoodName"),
                LintError(9, 1, ruleId, "${MISSING_KDOC_TOP_LEVEL.warnText()} SomeNewGoodName"),
                LintError(12, 1, ruleId, "${MISSING_KDOC_TOP_LEVEL.warnText()} SomeOtherNewGoodName")
        )
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_TOP_LEVEL)
    fun `all internal classes should be documented with KDoc`() {
        val code =
                """
                    internal class SomeGoodName {
                    }
                """.trimIndent()
        lintMethod(code, LintError(
                1, 1, ruleId, "${MISSING_KDOC_TOP_LEVEL.warnText()} SomeGoodName")
        )
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_TOP_LEVEL)
    fun `all internal and public functions on top-level should be documented with Kdoc`() {
        val code =
                """
                    fun someGoodName() {
                    }

                    internal fun someGoodNameNew(): String {
                        return " ";
                    }
                    
                    fun main() {}
                """.trimIndent()
        lintMethod(code,
                LintError(1, 1, ruleId, "${MISSING_KDOC_TOP_LEVEL.warnText()} someGoodName"),
                LintError(4, 1, ruleId, "${MISSING_KDOC_TOP_LEVEL.warnText()} someGoodNameNew")
        )
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_TOP_LEVEL)
    fun `all internal and public functions on top-level should be documented with Kdoc (positive case)`() {
        val code =
                """
                    private fun someGoodName() {
                    }
                """.trimIndent()
        lintMethod(code)
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_TOP_LEVEL)
    fun `positive Kdoc case with private class`() {
        val code =
                """
                    private class SomeGoodName {
                    }
                """.trimIndent()
        lintMethod(code)
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_CLASS_ELEMENTS)
    fun `Kdoc should present for each class element`() {
        val code =
                """
                    /**
                    * class that contains fields, functions and public subclasses
                    **/
                    class SomeGoodName {
                        val variable: String = ""
                        private val privateVariable: String = ""
                        fun perfectFunction() {
                        }

                        private fun privateFunction() {
                        }

                        class InternalClass {
                        }

                        private class InternalClass {
                        }
                        
                        public fun main() {}
                    }
                """.trimIndent()
        lintMethod(code,
                LintError(5, 5, ruleId, "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} variable"),
                LintError(7, 5, ruleId, "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} perfectFunction"),
                LintError(13, 5, ruleId, "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} InternalClass")
        )
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_CLASS_ELEMENTS)
    fun `Kdoc shouldn't not be mandatory for overridden functions and props`() {
        val code =
                """
                    /**
                    * class that contains fields, functions and public subclasses
                    **/
                    class SomeGoodName : Another {
                        val variable: String = ""
                        private val privateVariable: String = ""
                        override val someVal: String = ""
                        fun perfectFunction() {
                        }

                        override fun overrideFunction() {
                        }

                        class InternalClass {
                        }

                        private class InternalClass {
                        }
                        
                        public fun main() {}
                    }
                """.trimIndent()
        lintMethod(code,
                LintError(5, 5, ruleId, "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} variable"),
                LintError(8, 5, ruleId, "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} perfectFunction"),
                LintError(14, 5, ruleId, "${MISSING_KDOC_CLASS_ELEMENTS.warnText()} InternalClass")
        )
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_CLASS_ELEMENTS)
    fun `Kdoc shouldn't present for each class element because Test annotation`() {
        lintMethod(
                """
                    /**
                    * class that contains fields, functions and public subclasses
                    **/
                    @Test
                    class SomeGoodName {
                        val variable: String = ""
                        private val privateVariable: String = ""
                        fun perfectFunction() {
                        }

                        private fun privateFunction() {
                        }

                        class InternalClass {
                        }

                        private class InternalClass {
                        }
                    }
                """.trimIndent())
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_CLASS_ELEMENTS)
    fun `Kdoc should present for each class element (positive)`() {
        val code =
                """
                    /**
                    * class that contains fields, functions and public subclasses
                    **/
                    class SomeGoodName {
                        /**
                        * class that contains fields, functions and public subclasses
                        **/
                        val variable: String = ""

                        private val privateVariable: String = ""

                        /**
                        * class that contains fields, functions and public subclasses
                        **/
                        fun perfectFunction() {
                        }

                        private fun privateFunction() {
                        }

                        /**
                        * class that contains fields, functions and public subclasses
                        **/
                        class InternalClass {
                        }

                        private class InternalClass {
                        }
                    }
                """.trimIndent()
        lintMethod(code)
    }

    @Test
    @Tag(WarningNames.MISSING_KDOC_CLASS_ELEMENTS)
    fun `regression - should not force documentation on standard methods`() {
        lintMethod(
                """
                    |/**
                    | * This is an example class
                    | */
                    |class Example {
                    |    override fun toString() = ""
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT)
    fun `check simple primary constructor with comment`() {
        lintMethod(
                """
                    |/**
                    | * @property name d
                    | * @param adsf
                    | * @return something
                    | */
                    |class Example constructor (
                    |   // short
                    |   val name: String
                    |) {
                    |}
                """.trimMargin(),
                LintError(7, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT.warnText()} name", true)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY)
    fun `shouldn't trigger because not primary constructor`() {
        lintMethod(
                """
                    |/**
                    | * @property name d
                    | * @property anotherName text
                    | */
                    |class Example {
                    |   constructor(
                    |   // name
                    |   name: String,
                    |   anotherName: String,
                    |   OneMoreName: String
                    |   )
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT)
    fun `check constructor with comment`() {
        lintMethod(
                """
                    |/**
                    | * @return some
                    | */
                    |class Example (
                    |   //some descriptions
                    |   val name: String,
                    |   anotherName: String,
                    |   OneMoreName: String
                    |   ) {
                    |}
                """.trimMargin(),
                LintError(5, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT.warnText()} name", true)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY)
    fun `check constructor with block comment`() {
        lintMethod(
                """
                    |/**
                    | * @return some
                    | */
                    |class Example (
                    |   /*some descriptions*/val name: String,
                    |   anotherName: String,
                    |   OneMoreName: String
                    |   ) {
                    |}
                """.trimMargin(),
                LintError(5, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY.warnText()} /*some descriptions*/", true)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY)
    fun `check not property`() {
        lintMethod(
                """
                    |/**
                    | * @return some
                    | */
                    |class Example (
                    |   //some descriptions
                    |   name: String,
                    |   anotherName: String,
                    |   OneMoreName: String
                    |   ) {
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY)
    fun `check constructor with kdoc`() {
        lintMethod(
                """
                    |/**
                    | * @return some
                    | */
                    |class Example (
                    |   /**
                    |    * some descriptions
                    |    * @return fdv
                    |    */
                    |    
                    |   val name: String,
                    |   anotherName: String,
                    |   OneMoreName: String
                    |   ) {
                    |}
                """.trimMargin(),
                LintError(5, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY.warnText()} /**...", true)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY)
    fun `shouldn't fix`() {
        lintMethod(
                """
                    |/**
                    | * @property name text
                    | */
                    |class Example (
                    |   /**
                    |    * sdcjkh
                    |    * @property name text2
                    |    */
                    |   val name: String, 
                    |   ) {
                    |}
                """.trimMargin(),
                LintError(5, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY.warnText()} /**...", false)
        )
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY)
    fun `shouldn't trigger`() {
        lintMethod(
                """
                    |/**
                    | * text
                    | */
                    |class Example (
                    |   private val name: String, 
                    |   ) {
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY)
    fun `no property kdoc`() {
        lintMethod(
                """
                    |/**
                    | * @property Name text
                    | */
                    |class Example (
                    |   val name: String, 
                    |   ) {
                    |}
                """.trimMargin(),
                LintError(5, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY.warnText()} add <name> to KDoc", true)
        )
    }

    @Test
    @Tags(Tag(WarningNames.KDOC_NO_CONSTRUCTOR_PROPERTY), Tag(WarningNames.MISSING_KDOC_TOP_LEVEL))
    fun `no property kdoc and class`() {
        lintMethod(
                """
                    |class Example (
                    |   val name: String, 
                    |   private val surname: String
                    |   ) {
                    |}
                """.trimMargin(),
                LintError(1, 1, ruleId, "${MISSING_KDOC_TOP_LEVEL.warnText()} Example"),
                LintError(2, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY.warnText()} add <name> to KDoc", true),
                LintError(3, 4, ruleId, "${KDOC_NO_CONSTRUCTOR_PROPERTY.warnText()} add <surname> to KDoc", true)
        )
    }
}
