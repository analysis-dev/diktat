package org.cqfn.diktat.ruleset.utils

import org.cqfn.diktat.ruleset.constants.Warnings
import org.junit.jupiter.api.Test

class WarningTest {
    @Test
    fun `checking that warnings has all proper fields filled`() {
        Warnings.values().forEach { warn ->
            assert(warn.ruleId.split(".").size == 3)
        }
    }
}
