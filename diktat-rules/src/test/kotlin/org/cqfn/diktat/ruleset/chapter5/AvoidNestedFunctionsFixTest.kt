package org.cqfn.diktat.ruleset.chapter5

import generated.WarningNames
import org.cqfn.diktat.ruleset.rules.AvoidNestedFunctionsRule
import org.cqfn.diktat.util.FixTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class AvoidNestedFunctionsFixTest : FixTestBase("test/paragraph5/nested_functions", ::AvoidNestedFunctionsRule) {
    @Test
    @Tag(WarningNames.AVOID_NESTED_FUNCTIONS)
    fun `fix nested functions`() {
        fixAndCompare("AvoidNestedFunctionsExample.kt", "AvoidNestedFunctionsTest.kt")
    }
    @Test
    @Tag(WarningNames.AVOID_NESTED_FUNCTIONS)
    fun `fix several nested functions`() {
        fixAndCompare("AvoidNestedFunctionsSeveralExample.kt", "AvoidNestedFunctionsSeveralTest.kt")
    }

    @Test
    @Tag(WarningNames.AVOID_NESTED_FUNCTIONS)
    fun `should not change`() {
        fixAndCompare("AvoidNestedFunctionsNoTriggerExample.kt", "AvoidNestedFunctionsNoTriggerTest.kt")
    }
}
