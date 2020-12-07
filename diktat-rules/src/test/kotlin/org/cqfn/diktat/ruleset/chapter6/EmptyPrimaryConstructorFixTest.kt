package org.cqfn.diktat.ruleset.chapter6

import org.cqfn.diktat.ruleset.rules.AvoidEmptyPrimaryConstructor
import org.cqfn.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class EmptyPrimaryConstructorFixTest : FixTestBase("test/chapter6/primary_constructor", ::AvoidEmptyPrimaryConstructor) {
    @Test
    @Tag(WarningNames.EMPTY_PRIMARY_CONSTRUCTOR)
    fun `should remove empty primary constructor`() {
        fixAndCompare("EmptyPCExpected.kt", "EmptyPCTest.kt")
    }
}
