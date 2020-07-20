package org.cqfn.diktat.common.config.rules

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.cqfn.diktat.common.config.reader.JsonResourceConfigReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.File
import java.net.URL
import java.util.stream.Collectors

interface Rule {
    fun ruleName(): String
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class RulesConfig(
    val name: String,
    val enabled: Boolean,
    val configuration: Map<String, String>
)

open class RuleConfiguration(protected val config: Map<String, String>)
object EmptyConfiguration: RuleConfiguration(mapOf())

/**
 * class returns the list of configurations that we have read from a json: rules-config.json
 */
open class RulesConfigReader(override val classLoader: ClassLoader) : JsonResourceConfigReader<List<RulesConfig>>() {
    companion object {
        val log: Logger = LoggerFactory.getLogger(RulesConfigReader::class.java)
    }

    override fun parseResource(fileStream: BufferedReader): List<RulesConfig> {
        val mapper = jacksonObjectMapper()
        val jsonValue = fileStream.lines().collect(Collectors.joining())
        return mapper.readValue(jsonValue)
    }

    /**
     * instead of reading the resource as it is done in the interface we will read a file by the absolute path here
     * if the path is provided, else will read the hardcoded file 'rules-config.json' from the package
     */
    override fun getConfigFile(resourceFileName: String): BufferedReader? {
        val resourceFile = File(resourceFileName)
        return if (resourceFile.exists()) {
            log.debug("Using rules-config.json file from the following path: ${resourceFile.absolutePath}")
            File(resourceFileName).bufferedReader()
        } else {
            log.debug("Using the default rules-config.json file from the class path")
            classLoader.getResourceAsStream(resourceFileName)?.bufferedReader()
        }
    }
}


// ================== utils for List<RulesConfig> from json config

fun List<RulesConfig>.getRuleConfig(rule: Rule): RulesConfig? {
    return this.find { it.name == rule.ruleName() }
}

/**
 * checking if in json config particular rule is enabled or disabled
 * (!) the default value is "true" (in case there is no config specified)
 */
fun List<RulesConfig>.isRuleEnabled(rule: Rule): Boolean {
    val ruleMatched = getRuleConfig(rule)
    return ruleMatched?.enabled ?: true
}
