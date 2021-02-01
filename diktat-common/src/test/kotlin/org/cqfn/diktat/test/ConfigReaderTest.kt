package org.cqfn.diktat.test

import org.cqfn.diktat.common.config.rules.DIKTAT_COMMON
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.RulesConfigReader
import org.cqfn.diktat.common.config.rules.getCommonConfiguration
import org.cqfn.diktat.common.config.rules.kotlinVersion
import org.junit.jupiter.api.Test

class ConfigReaderTest {
    @Test
    fun `testing json reading`() {
        val rulesConfigList: List<RulesConfig>? = RulesConfigReader(javaClass.classLoader)
            .readResource("src/test/resources/test-rules-config.yml")
        requireNotNull(rulesConfigList)
        assert(rulesConfigList.any { it.name == "CLASS_NAME_INCORRECT" && it.enabled })
        assert(rulesConfigList.find { it.name == "CLASS_NAME_INCORRECT" }?.configuration == emptyMap<String, String>())
        assert(rulesConfigList.find { it.name == "DIKTAT_COMMON" }
            ?.configuration?.get("domainName") == "org.cqfn.diktat")
    }

    @Test
    fun `testing kotlin version`() {
        val rulesConfigList: List<RulesConfig>? = RulesConfigReader(javaClass.classLoader)
            .readResource("src/test/resources/test-rules-config.yml")
        val currentKotlinVersion = KotlinVersion.CURRENT
        requireNotNull(rulesConfigList)
        assert(rulesConfigList.getCommonConfiguration().value.kotlinVersion == currentKotlinVersion)
        assert(rulesConfigList.find { it.name == DIKTAT_COMMON }
            ?.configuration
            ?.get("kotlinVersion")
            ?.kotlinVersion() == currentKotlinVersion)
    }
}
