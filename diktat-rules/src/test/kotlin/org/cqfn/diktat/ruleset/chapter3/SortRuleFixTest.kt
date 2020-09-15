package org.cqfn.diktat.ruleset.chapter3

import generated.WarningNames
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.SortRule
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class SortRuleFixTest : FixTestBase("test/paragraph3/sort_error", ::SortRule) {

    private val rulesConfigSortEnum: List<RulesConfig> = listOf(
            RulesConfig(Warnings.WRONG_DECLARATIONS_ORDER.name, true,
                    mapOf("sortEnum" to "true"))
    )

    private val rulesConfigSortProperty: List<RulesConfig> = listOf(
            RulesConfig(Warnings.WRONG_DECLARATIONS_ORDER.name, true,
                    mapOf("sortProperty" to "true"))
    )

    @Test
    @Tag(WarningNames.WRONG_DECLARATIONS_ORDER)
    fun `should fix enum order`() {
        fixAndCompare("EnumSortExpected.kt", "EnumSortTest.kt", rulesConfigSortEnum)
    }

    @Test
    @Tag(WarningNames.WRONG_DECLARATIONS_ORDER)
    fun `should fix constants order`() {
        fixAndCompare("ConstantsExpected.kt", "ConstantsTest.kt", rulesConfigSortProperty)
    }
}
