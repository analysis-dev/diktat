package org.cqfn.diktat.ruleset.chapter5

import org.cqfn.diktat.ruleset.rules.CheckInverseMethodRule
import org.cqfn.diktat.util.FixTestBase

import generated.WarningNames.INVERSE_FUNCTION_PREFERRED
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class CheckInverseMethodRuleFixTest : FixTestBase("test/paragraph5/method_call_names", ::CheckInverseMethodRule) {
    @Test
    @Tag(INVERSE_FUNCTION_PREFERRED)
    fun `should fix method calls`() {
        fixAndCompare("ReplaceMethodCallNamesExpected.kt", "ReplaceMethodCallNamesTest.kt")
    }
}
