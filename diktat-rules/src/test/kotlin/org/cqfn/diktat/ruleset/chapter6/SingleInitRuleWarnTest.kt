package org.cqfn.diktat.ruleset.chapter6

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.classes.SingleInitRule
import org.cqfn.diktat.util.LintTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class SingleInitRuleWarnTest: LintTestBase(::SingleInitRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:multiple-init-block"

    @Test
    @Tag(WarningNames.MULTIPLE_INIT_BLOCKS)
    fun `should allow single init block`() {
        lintMethod(
            """
                |class Example {
                |    init { println("Lorem ipsum") }
                |}
            """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.MULTIPLE_INIT_BLOCKS)
    fun `should forbid multiple init blocks`() {
        lintMethod(
            """
                |class Example {
                |    init { println("Lorem ipsum") }
                |    
                |    val foo = 0
                |    
                |    init { println("Dolor sit amet") }
                |}
            """.trimMargin(),
            LintError(1, 15, ruleId, "${Warnings.MULTIPLE_INIT_BLOCKS.warnText()} in class <Example> found 2 `init` blocks", true)
        )
    }

    @Test
    @Tag(WarningNames.MULTIPLE_INIT_BLOCKS)
    fun `should warn if properties are assigned in init block`() {
        lintMethod(
            """
                |class A(baseUrl: String) {
                |    private val customUrl: String
                |    init {
                |        customUrl = "${'$'}baseUrl/myUrl"
                |    }
                |}
            """.trimMargin(),
            LintError(3, 5, ruleId, "${Warnings.MULTIPLE_INIT_BLOCKS.warnText()} `init` block has assignments that can be moved to declarations", true)
        )
    }
}
