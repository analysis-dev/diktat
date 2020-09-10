package org.cqfn.diktat.test

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.RulesConfigReader
import org.junit.jupiter.api.Test

class ConfigReaderTest {
    @Test
    fun `testing json reading`() {
        val rulesConfigList: List<RulesConfig>? = RulesConfigReader(javaClass.classLoader).readResource("src/test/resources/test-rules-config.yml")
        assert(rulesConfigList?.filter { it.name == "CLASS_NAME_INCORRECT" && it.enabled }!!.isNotEmpty())
        assert(rulesConfigList.find { it.name == "CLASS_NAME_INCORRECT" }?.configuration == mapOf<String, String>())
        assert(rulesConfigList.find { it.name == "DIKTAT_COMMON" }!!
                .configuration == mapOf("domainName" to "org.cqfn.diktat"))
    }
}
