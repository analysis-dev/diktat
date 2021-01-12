package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings.EMPTY_BLOCK_STRUCTURE_ERROR
import org.cqfn.diktat.ruleset.utils.findLBrace
import org.cqfn.diktat.ruleset.utils.findLeafWithSpecificType
import org.cqfn.diktat.ruleset.utils.findParentNodeWithSpecificType
import org.cqfn.diktat.ruleset.utils.hasParent
import org.cqfn.diktat.ruleset.utils.isBlockEmpty
import org.cqfn.diktat.ruleset.utils.isOverridden
import org.cqfn.diktat.ruleset.utils.isPascalCase

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.FUNCTION_LITERAL
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.RBRACE
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl

/**
 * Rule that checks if empty code blocks (`{  }`) are used and checks their formatting.
 */
class EmptyBlock(private val configRules: List<RulesConfig>) : Rule("empty-block-structure") {
    private var isFixMode: Boolean = false
    private lateinit var emitWarn: EmitType

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: EmitType) {
        emitWarn = emit
        isFixMode = autoCorrect

        val configuration = EmptyBlockStyleConfiguration(
            configRules.getRuleConfig(EMPTY_BLOCK_STRUCTURE_ERROR)?.configuration ?: emptyMap()
        )
        searchNode(node, configuration)
    }

    private fun searchNode(node: ASTNode, configuration: EmptyBlockStyleConfiguration) {
        val newNode = node.findLBrace()?.treeParent ?: return
        checkEmptyBlock(newNode, configuration)
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkEmptyBlock(node: ASTNode, configuration: EmptyBlockStyleConfiguration) {
        if (node.treeParent.isOverridden() || isAnonymousSamClass(node)) {
            return
        }
        if (node.isBlockEmpty()) {
            if (!configuration.emptyBlockExist) {
                EMPTY_BLOCK_STRUCTURE_ERROR.warn(configRules, emitWarn, isFixMode, "empty blocks are forbidden unless it is function with override keyword",
                    node.startOffset, node)
            } else {
                val space = node.findChildByType(RBRACE)!!.treePrev
                if (configuration.emptyBlockNewline && !space.text.contains("\n")) {
                    EMPTY_BLOCK_STRUCTURE_ERROR.warnAndFix(configRules, emitWarn, isFixMode, "different style for empty block",
                        node.startOffset, node) {
                        if (space.elementType == WHITE_SPACE) {
                            (space.treeNext as LeafPsiElement).replaceWithText("\n")
                        } else {
                            node.addChild(PsiWhiteSpaceImpl("\n"), space.treeNext)
                        }
                    }
                } else if (!configuration.emptyBlockNewline && space.text.contains("\n")) {
                    EMPTY_BLOCK_STRUCTURE_ERROR.warnAndFix(configRules, emitWarn, isFixMode, "different style for empty block",
                        node.startOffset, node) {
                        node.removeChild(space)
                    }
                }
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType", "WRONG_WHITESPACE")
    private fun isAnonymousSamClass(node: ASTNode) : Boolean =
            if (node.elementType == FUNCTION_LITERAL && node.hasParent(CALL_EXPRESSION)) {
                // We are checking identifier because it is not class in AST
                // , SAM conversions are indistinguishable from lambdas.
                // So we just verify that identifier is in PascalCase
                val valueArgument = node.findParentNodeWithSpecificType(CALL_EXPRESSION)!!
                valueArgument.findLeafWithSpecificType(IDENTIFIER)?.text?.isPascalCase() ?: false
            } else {
                false
            }

    /**
     * [RuleConfiguration] for empty blocks formatting
     */
    class EmptyBlockStyleConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * Whether empty code blocks should be allowed
         */
        val emptyBlockExist = config["allowEmptyBlocks"]?.toBoolean() ?: false

        /**
         * Whether a newline after `{` is required in an empty block
         */
        val emptyBlockNewline = config["styleEmptyBlockWithNewline"]?.toBoolean() ?: true
    }
}
