package org.cqfn.diktat.ruleset.chapter3

import generated.WarningNames
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.chapter3.StringConcatenationRule
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class StringConcatenationRuleFixTest : FixTestBase("test/paragraph3/string_concatenation", ::StringConcatenationRule,
    listOf(
        RulesConfig(Warnings.STRING_CONCATENATION.name, true, mapOf())
    )
) {
    @Test
    @Tag(WarningNames.STRING_CONCATENATION)
    fun `fixing string concatenation`() {
        fixAndCompare("StringConcatenationExpected.kt", "StringConcatenationTest.kt")
    }
}
