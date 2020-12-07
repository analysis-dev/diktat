package org.cqfn.diktat.ruleset.chapter6

import org.cqfn.diktat.ruleset.rules.classes.CompactInitialization
import org.cqfn.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class CompactInitializationFixTest : FixTestBase("test/chapter6/compact_initialization", ::CompactInitialization) {
    @Test
    @Tag(WarningNames.COMPACT_OBJECT_INITIALIZATION)
    fun `should wrap properties into apply`() {
        fixAndCompare("SimpleExampleExpected.kt", "SimpleExampleTest.kt")
    }

    @Test
    @Tag(WarningNames.COMPACT_OBJECT_INITIALIZATION)
    fun `should wrap properties into apply also moving comments`() {
        fixAndCompare("ExampleWithCommentsExpected.kt", "ExampleWithCommentsTest.kt")
    }

    @Test
    @Tag(WarningNames.COMPACT_OBJECT_INITIALIZATION)
    fun `should wrap properties into apply - existing apply with value argument`() {
        fixAndCompare("ApplyWithValueArgumentExpected.kt", "ApplyWithValueArgumentTest.kt")
    }
}
