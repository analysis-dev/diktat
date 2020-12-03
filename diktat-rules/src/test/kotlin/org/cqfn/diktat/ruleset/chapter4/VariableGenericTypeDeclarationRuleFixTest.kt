package org.cqfn.diktat.ruleset.chapter4

import org.cqfn.diktat.ruleset.rules.VariableGenericTypeDeclarationRule
import org.cqfn.diktat.util.FixTestBase

import generated.WarningNames
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class VariableGenericTypeDeclarationRuleFixTest : FixTestBase("test/paragraph4/generics", ::VariableGenericTypeDeclarationRule) {
    @Test
    @Tag(WarningNames.GENERIC_VARIABLE_WRONG_DECLARATION)
    fun `basic fix test`() {
        fixAndCompare("VariableGenericTypeDeclarationExpected.kt", "VariableGenericTypeDeclarationTest.kt")
    }
}
