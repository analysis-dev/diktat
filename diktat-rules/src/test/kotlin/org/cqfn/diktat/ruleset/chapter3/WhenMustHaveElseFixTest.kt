package org.cqfn.diktat.ruleset.chapter3

import org.cqfn.diktat.ruleset.rules.WhenMustHaveElseRule
import org.cqfn.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class WhenMustHaveElseFixTest : FixTestBase("test/paragraph3/else_expected", ::WhenMustHaveElseRule) {
    @Test
    @Tag(WarningNames.WHEN_WITHOUT_ELSE)
    fun `should make else branch`() {
        fixAndCompare("ElseInWhenExpected.kt", "ElseInWhenTest.kt")
    }
}
