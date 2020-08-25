package org.cqfn.diktat.ruleset.chapter3.files

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.cqfn.diktat.ruleset.constants.Warnings.TOO_MANY_BLANK_LINES
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.files.BlankLinesRule
import org.cqfn.diktat.util.lintMethod
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class BlankLinesWarnTest {
    private val ruleId = "$DIKTAT_RULE_SET_ID:blank-lines"
    private val consecutiveLinesWarn = "${TOO_MANY_BLANK_LINES.warnText()} do not use more than two consecutive blank lines"
    private fun blankLinesInBlockWarn(isBeginning: Boolean) =
            "${TOO_MANY_BLANK_LINES.warnText()} do not put newlines ${if (isBeginning) "in the beginning" else "at the end"} of code blocks"

    @Test
    @Tag(WarningNames.TOO_MANY_BLANK_LINES)
    fun `blank lines usage - positive example`() {
        lintMethod(BlankLinesRule(),
                """
                    |class Example {
                    |    fun foo() {
                    |    
                    |    }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.TOO_MANY_BLANK_LINES)
    fun `should prohibit usage of two or more consecutive blank lines`() {
        lintMethod(BlankLinesRule(),
                """
                    |class Example {
                    |
                    |
                    |    val foo = 0
                    |    
                    |    
                    |    fun bar() { }
                    |}
                """.trimMargin(),
                LintError(1, 16, ruleId, consecutiveLinesWarn, true),
                LintError(4, 16, ruleId, consecutiveLinesWarn, true)
        )
    }

    @Test
    @Tag(WarningNames.TOO_MANY_BLANK_LINES)
    fun `should prohibit blank lines in the beginning or at the end of block`() {
        lintMethod(BlankLinesRule(),
                """
                    |class Example {
                    |
                    |    fun foo() {
                    |    
                    |        bar()
                    |        
                    |    }
                    |}
                """.trimMargin(),
                LintError(1, 16, ruleId, blankLinesInBlockWarn(true), true),
                LintError(3, 16, ruleId, blankLinesInBlockWarn(true), true),
                LintError(5, 14, ruleId, blankLinesInBlockWarn(false), true)
        )
    }
}
