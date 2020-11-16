package org.cqfn.diktat.ruleset.chapter6

import generated.WarningNames
import org.cqfn.diktat.util.FixTestBase
import org.cqfn.diktat.ruleset.rules.UselessSupertype
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class UselessSupertypeFixTest : FixTestBase("test/paragraph6/useless-override", ::UselessSupertype) {

    @Test
    @Tag(WarningNames.USELESS_SUPERTYPE)
    fun `fix example with one super`() {
        fixAndCompare("UselessOverrideExpected.kt", "UselessOverrideTest.kt")
    }

    @Test
    @Tag(WarningNames.USELESS_SUPERTYPE)
    fun `fix several super`() {
        fixAndCompare("SeveralSuperTypesExpected.kt", "SeveralSuperTypesTest.kt")
    }
}
