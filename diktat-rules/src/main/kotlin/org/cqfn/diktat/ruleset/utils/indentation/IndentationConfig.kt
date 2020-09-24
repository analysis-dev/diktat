package org.cqfn.diktat.ruleset.utils.indentation

import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.ruleset.rules.files.IndentationRule

internal class IndentationConfig(config: Map<String, String>) : RuleConfiguration(config) {
    val newlineAtEnd = config["newlineAtEnd"]?.toBoolean() ?: true

    /**
     * If true, in parameter list when parameters are split by newline they are indented with two indentations instead of one
     */
    val extendedIndentOfParameters = config["extendedIndentOfParameters"]?.toBoolean() ?: true

    /**
     * If true, if first parameter in parameter list is on the same line as opening parenthesis, then other parameters
     * can be aligned with it
     */
    val alignedParameters = config["alignedParameters"]?.toBoolean() ?: true

    /**
     * If true, if expression is split by newline after operator like +/-/`*`, then the next line is indented with two indentations instead of one
     */
    val extendedIndentAfterOperators = config["extendedIndentAfterOperators"]?.toBoolean() ?: true

    /**
     * The indentation size for each file
     */
    val indentationSize = config["indentationSize"]?.toInt() ?: IndentationRule.INDENT_SIZE
}
