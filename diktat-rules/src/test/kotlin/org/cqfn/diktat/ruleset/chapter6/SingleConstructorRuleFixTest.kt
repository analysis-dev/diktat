package org.cqfn.diktat.ruleset.chapter6

import org.cqfn.diktat.ruleset.rules.classes.SingleConstructorRule
import org.cqfn.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class SingleConstructorRuleFixTest : FixTestBase("test/chapter6/classes", ::SingleConstructorRule) {
    @Test
    @Tag(WarningNames.SINGLE_CONSTRUCTOR_SHOULD_BE_PRIMARY)
    fun `should convert simple secondary constructor to primary`() {
        fixAndCompare("SimpleConstructorExpected.kt", "SimpleConstructorTest.kt")
    }

    @Test
    @Tag(WarningNames.SINGLE_CONSTRUCTOR_SHOULD_BE_PRIMARY)
    fun `should convert secondary constructor to a primary and init block`() {
        fixAndCompare("ConstructorWithInitExpected.kt", "ConstructorWithInitTest.kt")
    }

    @Test
    @Tag(WarningNames.SINGLE_CONSTRUCTOR_SHOULD_BE_PRIMARY)
    fun `should convert secondary constructor to a primary saving modifiers`() {
        fixAndCompare("ConstructorWithModifiersExpected.kt", "ConstructorWithModifiersTest.kt")
    }

    @Test
    @Tag(WarningNames.SINGLE_CONSTRUCTOR_SHOULD_BE_PRIMARY)
    fun `should keep custom assignments when converting secondary constructor`() {
        fixAndCompare("ConstructorWithCustomAssignmentsExpected.kt", "ConstructorWithCustomAssignmentsTest.kt")
    }

    @Test
    @Tag(WarningNames.SINGLE_CONSTRUCTOR_SHOULD_BE_PRIMARY)
    fun `should keep assignments and required local variables in an init block`() {
        fixAndCompare("AssignmentWithLocalPropertyExpected.kt", "AssignmentWithLocalPropertyTest.kt")
    }
}
