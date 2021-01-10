package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.ConsecutiveSpacesRule
import org.cqfn.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class ConsecutiveSpacesRuleFixTest : FixTestBase("test/paragraph3/spaces",
    ::ConsecutiveSpacesRule,
    listOf(
        RulesConfig(Warnings.TOO_MANY_CONSECUTIVE_SPACES.name, true,
            mapOf(
                "maxSpaces" to "1",
                "saveInitialFormattingForEnums" to "true"
            )
        )
    )
) {
    @Test
    @Tag(WarningNames.TOO_MANY_CONSECUTIVE_SPACES)
    fun `many spaces rule enum case`() {
        fixAndCompare("TooManySpacesEnumExpected.kt", "TooManySpacesEnumTest.kt")
    }

    @Test
    @Tag(WarningNames.TOO_MANY_CONSECUTIVE_SPACES)
    fun `many spaces rule`() {
        fixAndCompare("TooManySpacesExpected.kt", "TooManySpacesTest.kt")
    }
}
