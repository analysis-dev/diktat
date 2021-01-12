package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings.NESTED_BLOCK
import org.cqfn.diktat.ruleset.utils.findAllNodesWithSpecificType
import org.cqfn.diktat.ruleset.utils.hasChildOfType

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.FUNCTION_LITERAL
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.LBRACE
import com.pinterest.ktlint.core.ast.ElementType.OBJECT_DECLARATION
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * Rule 5.1.2 Nested blokcs
 */
class NestedFunctionBlock(private val configRules: List<RulesConfig>) : Rule("nested-block") {
    private val configuration: NestedBlockConfiguration by lazy {
        NestedBlockConfiguration(configRules.getRuleConfig(NESTED_BLOCK)?.configuration ?: emptyMap())
    }
    private var isFixMode: Boolean = false
    private lateinit var emitWarn: EmitType

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: EmitType) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType in nullificationType) {
            countNestedBlocks(node, configuration.maxNestedBlockQuantity)
        }
    }

    private fun countNestedBlocks(node: ASTNode, maxNestedBlockCount: Long) {
        node.findAllNodesWithSpecificType(LBRACE)
            .reversed()
            .forEach { lbraceNode ->
                val blockParent = lbraceNode
                    .parents()
                    .takeWhile { it != node }
                    .takeIf { parentList -> parentList.map { it.elementType }.none { it in nullificationType } }
                    ?.count { it.hasChildOfType(LBRACE) }
                    ?: return
                if (blockParent > maxNestedBlockCount) {
                    NESTED_BLOCK.warn(configRules, emitWarn, isFixMode, node.findChildByType(IDENTIFIER)?.text ?: node.text,
                        node.startOffset, node)
                    return
                }
            }
    }

    /**
     * [RuleConfiguration] for analyzing nested code blocks
     */
    class NestedBlockConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * Maximum number of allowed levels of nested blocks
         */
        val maxNestedBlockQuantity = config["maxNestedBlockQuantity"]?.toLong() ?: MAX_NESTED_BLOCK_COUNT
    }

    companion object {
        private const val MAX_NESTED_BLOCK_COUNT = 4L

        /**
         * Nodes of these types reset counter of nested blocks
         */
        private val nullificationType = listOf(CLASS, FUN, OBJECT_DECLARATION, FUNCTION_LITERAL)
    }
}
