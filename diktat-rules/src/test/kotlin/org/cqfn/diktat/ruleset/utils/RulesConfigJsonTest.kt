package org.cqfn.diktat.ruleset.utils

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.RulesConfigReader
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * Special test that checks that developer has not forgotten to add his warning to a diktat-analysis.yml
 * This file is needed to be in tact with latest changes in Warnings.kt
 */
class RulesConfigJsonTest {
    @Test
    fun `read rules config json`() {
        val allRulesFromConfig = readAllRulesFromConfig()
        val allRulesFromCode = readAllRulesFromCode()

        allRulesFromCode.forEach { rule ->
            val foundRule = allRulesFromConfig.getRuleConfig(rule)
            val jsonCodeSnippet = RulesConfig(rule.ruleName(), true, mapOf())
            val jacksonMapper = jacksonObjectMapper()

            val ruleJson = jacksonMapper.writeValueAsString(jsonCodeSnippet)
            Assertions.assertTrue(foundRule != null) {
                """
                   Cannot find warning ${rule.ruleName()} in rules-config.json.
                   You can fix it by adding the following code below to rules-config.json:
                   $ruleJson
                """
            }
        }

        allRulesFromConfig.forEach { warning ->
            val warningName = warning.name
            val ruleFound = allRulesFromCode.find { it.ruleName() == warningName || warningName == "DIKTAT_COMMON" } != null
            Assertions.assertTrue(ruleFound) {
                """
                    Found rule (warning) in rules-config.json: <$warningName> that does not exist in the code. Misprint or configuration was renamed? 
                """.trimIndent()
            }
        }
    }

    private fun readAllRulesFromConfig() =
            RulesConfigReader(javaClass.classLoader)
                    .readResource("diktat-analysis.yml") ?: listOf()

    private fun readAllRulesFromCode() =
            Warnings.values()
}
