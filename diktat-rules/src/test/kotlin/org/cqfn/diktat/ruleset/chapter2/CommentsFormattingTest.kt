package org.cqfn.diktat.ruleset.chapter2

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.constants.Warnings.IF_ELSE_COMMENTS
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.kdoc.CommentsFormatting
import org.cqfn.diktat.util.LintTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class CommentsFormattingTest : LintTestBase(::CommentsFormatting){

    private val ruleId = "$DIKTAT_RULE_SET_ID:kdoc-comments-codeblocks-formatting"

    @Test
    @Tag(WarningNames.COMMENT_WHITE_SPACE)
    fun `check white space before comment good` () {
        val code =
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |
                    |class Example {
                    |    // First Comment
                    |    private val log = LoggerFactory.getLogger(Example.javaClass)
                    |}
                """.trimMargin()

        lintMethod(code)
    }


    @Test
    @Tag(WarningNames.COMMENT_WHITE_SPACE)
    fun `check white space before comment bad 2` () {
        val code =
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |
                    |class Example {
                    |    val s = RulesConfig(WRONG_INDENTATION.name, true,
                    |            mapOf(
                    |                    "newlineAtEnd" to "true",     // comment
                    |                    "extendedIndentOfParameters" to "true",
                    |                    "alignedParameters" to "true",
                    |                    "extendedIndentAfterOperators" to "true"
                    |            )
                    |    )
                    |}
                """.trimMargin()

        lintMethod(code,
                LintError(6,51,ruleId,"${Warnings.COMMENT_WHITE_SPACE.warnText()} There should be 2 space(s) before comment text, but there are too many in // comment", true))
    }

    @Test
    @Tag(WarningNames.COMMENT_WHITE_SPACE)
    fun `check white space before comment bad 3` () {
        val code =
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |
                    |@Suppress("RULE")    // asdasd
                    |class Example {
                    |
                    |}
                """.trimMargin()

        lintMethod(code,
                LintError(3,22,ruleId,"${Warnings.COMMENT_WHITE_SPACE.warnText()} There should be 2 space(s) before comment text, but there are too many in // asdasd", true))
    }

    @Test
    @Tag(WarningNames.COMMENT_WHITE_SPACE)
    fun `check white space before comment good2` () {
        val code =
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |
                    |/* This is a comment */
                    |class Example {
                    |    /**
                    |    *
                    |    * Some Comment
                    |    */
                    |    private val log = LoggerFactory.getLogger(Example.javaClass)
                    |    
                    |    fun a() {
                    |       // When comment
                    |       when(1) {
                    |           1 -> print(1)
                    |       }
                    |    }
                    |    /*
                    |       Some Comment
                    |    */
                    |}
                """.trimMargin()

        lintMethod(code)
    }

    @Test
    @Tag(WarningNames.COMMENT_WHITE_SPACE)
    fun `check comment before package good` () {
        val code =
                """
                    |// This is a comment before package
                    |package org.cqfn.diktat.ruleset.chapter3
                    |
                    |// This is a comment
                    |class Example {
                    |
                    |}
                """.trimMargin()

        lintMethod(code)
    }


    @Test
    @Tag(WarningNames.COMMENT_WHITE_SPACE)
    fun `check white space before comment bad` () {
        val code =
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |
                    |class Example {
                    |    //First Comment
                    |    private val log = LoggerFactory.getLogger(Example.javaClass)
                    |    
                    |    /**
                    |    *      Some comment
                    |    */
                    |    
                    |    /*     Comment */
                    |}
                """.trimMargin()

        lintMethod(code,
                LintError(4,5, ruleId, "${Warnings.COMMENT_WHITE_SPACE.warnText()} There should be 1 space(s) before comment token in //First Comment", true),
                LintError(11,5, ruleId, "${Warnings.COMMENT_WHITE_SPACE.warnText()} There should be 1 space(s) before comment token in /*     Comment */", true))
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES_AROUND_KDOC)
    fun `check new line above comment good` () {
        val code =
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |
                    |class Example {
                    |    private val log = LoggerFactory.getLogger(Example.javaClass)
                    |    
                    |    // Another Comment
                    |    private val some = 5
                    |    
                    |    fun someFunc() {
                    |       /* First comment */
                    |       val first = 5  // Some comment
                    |       
                    |       /**
                    |       * kDoc comment
                    |       * some text
                    |       */
                    |       val second = 6
                    |       
                    |       /**
                    |       * asdasd
                    |       */
                    |       fun testFunc() {
                    |           val a = 5  // Some Comment
                    |           
                    |           // Fun in fun Block
                    |           val b = 6
                    |       }
                    |    }
                    |}
                """.trimMargin()

        lintMethod(code)
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES_AROUND_KDOC)
    fun `check file new line above comment good` () {
        val code =
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |
                    |// Some comment
                    |class Example {
                    |
                    |}
                    |
                    |// Some comment 2
                    |class AnotherExample {
                    |
                    |}
                """.trimMargin()

        lintMethod(code)
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES_AROUND_KDOC)
    fun `check file new line above comment bad` () {
        val code =
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |// Some comment
                    |class Example {
                    |
                    |}
                    |
                    |// Some comment 2
                    |class AnotherExample {
                    |
                    |}
                """.trimMargin()

        lintMethod(code,
                LintError(2,1,ruleId, "${Warnings.WRONG_NEWLINES_AROUND_KDOC.warnText()} // Some comment", true))
    }

    @Test
    @Tag(WarningNames.WRONG_NEWLINES_AROUND_KDOC)
    fun `check file new line above comment bad - block and kDOC comments` () {
        val code =
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |/* Some comment */
                    |class Example {
                    |
                    |}
                    |/**
                    |* Some comment 2
                    |*/
                    |
                    |class AnotherExample {
                    |
                    |}
                """.trimMargin()

        lintMethod(code,
                LintError(2,1,ruleId, "${Warnings.WRONG_NEWLINES_AROUND_KDOC.warnText()} /* Some comment */", true),
                LintError(6,1,ruleId, "${Warnings.WRONG_NEWLINES_AROUND_KDOC.warnText()} /**...", true),
                LintError(8,3,ruleId, "${Warnings.WRONG_NEWLINES_AROUND_KDOC.warnText()} redundant blank line after /**...", true))
    }

    @Test
    @Tag(WarningNames.COMMENT_WHITE_SPACE)
    fun `check right side comments - good` () {
        val code =
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |
                    |/* Some comment */
                    |class Example {
                    |   val a = 5  // This is a comment
                    |}
                """.trimMargin()

        lintMethod(code)
    }

    @Test
    @Tag(WarningNames.COMMENT_WHITE_SPACE)
    fun `check right side comments - bad` () {
        val code =
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |
                    |/* Some comment */
                    |class Example {
                    |   val a = 5// This is a comment
                    |}
                """.trimMargin()

        lintMethod(code,
                LintError(5,13, ruleId, "${Warnings.COMMENT_WHITE_SPACE.warnText()} There should be 2 space(s) before comment text, but are none in // This is a comment", true))
    }

    @Test
    @Tag(WarningNames.IF_ELSE_COMMENTS)
    fun `if - else comments good` () {
        val code =
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |
                    |class Example {
                    |   fun someFunc() {
                    |       // general if comment
                    |       if(a = 5) {
                    |       
                    |       }
                    |       else {
                    |           // Good Comment
                    |           print(5)
                    |       }
                    |   }
                    |}
                """.trimMargin()

        lintMethod(code)
    }

    @Test
    @Tag(WarningNames.IF_ELSE_COMMENTS)
    fun `if - else comments good 2` () {
        val code =
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |
                    |class Example {
                    |   fun someFunc() {
                    |       // general if comment
                    |       if(a = 5) {
                    |       
                    |       } else
                    |           // Good Comment
                    |           print(5)
                    |   }
                    |}
                """.trimMargin()

        lintMethod(code)
    }

    @Test
    @Tag(WarningNames.IF_ELSE_COMMENTS)
    fun `if - else comments bad` () {
        val code =
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |
                    |class Example {
                    |   fun someFunc() {
                    |       // general if comment
                    |       if(a = 5) {
                    |       
                    |       }
                    |       // Bad Comment
                    |       else {
                    |           print(5)
                    |       }
                    |   }
                    |}
                """.trimMargin()

        lintMethod(code,
                LintError(6,8,ruleId, "${IF_ELSE_COMMENTS.warnText()} // Bad Comment", true))
    }

    @Test
    @Tag(WarningNames.IF_ELSE_COMMENTS)
    fun `if - else comments bad 3` () {
        val code =
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |
                    |class Example {
                    |   fun someFunc() {
                    |       // general if comment
                    |       if(a = 5) {
                    |       
                    |       }  /* Some comment */ else {
                    |           print(5)
                    |       }
                    |   }
                    |}
                """.trimMargin()

        lintMethod(code,
                LintError(6,8,ruleId, "${IF_ELSE_COMMENTS.warnText()} /* Some comment */", true))
    }

    @Test
    @Tag(WarningNames.IF_ELSE_COMMENTS)
    fun `if - else comments bad 4` () {
        val code =
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |
                    |class Example {
                    |   fun someFunc() {
                    |       // general if comment
                    |       if(a = 5) {
                    |       
                    |       }  /* Some comment */ else
                    |           print(5)
                    |   }
                    |}
                """.trimMargin()

        lintMethod(code,
                LintError(6,8,ruleId, "${IF_ELSE_COMMENTS.warnText()} /* Some comment */", true))
    }

    @Test
    @Tag(WarningNames.IF_ELSE_COMMENTS)
    fun `should not trigger on comment` () {
        val code =
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |
                    |class Example {
                    |   fun someFunc() {
                    |       // general if comment
                    |       if(a = 5) {
                    |           /* Some comment */
                    |       } else {
                    |           print(5)
                    |       }
                    |   }
                    |}
                """.trimMargin()

        lintMethod(code)
    }

    @Test
    @Tag(WarningNames.FIRST_COMMENT_NO_SPACES)
    fun `first comment no space in if - else bad` () {
        val code =
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |
                    |class Example {
                    |   fun someFunc() {
                    |       // general if comment
                    |       if(a = 5) {
                    |       
                    |       } else {  // Bad Comment 
                    |           print(5)
                    |       }
                    |   }
                    |}
                """.trimMargin()

        lintMethod(code,
                LintError(8,18,ruleId, "${Warnings.FIRST_COMMENT_NO_SPACES.warnText()} // Bad Comment ", true))
    }

    @Test
    @Tag(WarningNames.COMMENT_WHITE_SPACE)
    fun `check comment in class bad` () {
        val code =
                """
                    |package org.cqfn.diktat.ruleset.chapter3
                    |
                    |class Example { 
                    |    // First Comment
                    |    private val log = LoggerFactory.getLogger(Example.javaClass)  // secondComment
                    |}
                """.trimMargin()

        lintMethod(code)
    }
}
