package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_MULTIPLE_MODIFIERS_ORDER

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.ANNOTATION_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.psiUtil.children

/**
 * @property configRules
 */
class MultipleModifiersSequence(private val configRules: List<RulesConfig>) : Rule("multiple-modifiers") {
    private var isFixMode: Boolean = false
    private lateinit var emitWarn: EmitType

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: EmitType) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == MODIFIER_LIST) {
            checkModifierList(node)
            checkAnnotation(node)
        }
    }

    private fun checkModifierList(node: ASTNode) {
        val modifierListOfPair = node
            .getChildren(KtTokens.MODIFIER_KEYWORDS)
            .toList()
            .map { Pair(it, modifierOrder.indexOf(it.elementType)) }
        val sortModifierListOfPair = modifierListOfPair.sortedBy { it.second }.map { it.first }
        modifierListOfPair.forEachIndexed { index, (modifierNode, _) ->
            if (modifierNode != sortModifierListOfPair[index]) {
                WRONG_MULTIPLE_MODIFIERS_ORDER.warnAndFix(configRules, emitWarn, isFixMode,
                    "${modifierNode.text} should be on position ${sortModifierListOfPair.indexOf(modifierNode) + 1}, but is on position ${index + 1}",
                    modifierNode.startOffset, modifierNode) {
                    val nodeAfter = modifierNode.treeNext
                    node.removeChild(modifierNode)
                    node.addChild((sortModifierListOfPair[index].clone() as ASTNode), nodeAfter)
                }
            }
        }
    }

    private fun checkAnnotation(node: ASTNode) {
        val firstModifierIndex = node
            .children()
            .indexOfFirst { it.elementType in KtTokens.MODIFIER_KEYWORDS }
            .takeIf { it >= 0 } ?: return
        node
            .getChildren(null)
            .filterIndexed { index, astNode -> astNode.elementType == ANNOTATION_ENTRY && index > firstModifierIndex }
            .forEach { astNode ->
                WRONG_MULTIPLE_MODIFIERS_ORDER.warnAndFix(configRules, emitWarn, isFixMode,
                    "${astNode.text} annotation should be before all modifiers",
                    astNode.startOffset, astNode) {
                    val spaceBefore = astNode.treePrev
                    node.removeChild(astNode)
                    if (spaceBefore != null && spaceBefore.elementType == WHITE_SPACE) {
                        node.removeChild(spaceBefore)
                        node.addChild(spaceBefore, node.firstChildNode)
                        node.addChild(astNode.clone() as ASTNode, spaceBefore)
                    } else {
                        node.addChild(PsiWhiteSpaceImpl(" "), node.getChildren(null).first())
                        node.addChild(astNode.clone() as ASTNode, node.getChildren(null).first())
                    }
                }
            }
    }

    companion object {
        private val modifierOrder = listOf(KtTokens.PUBLIC_KEYWORD, KtTokens.INTERNAL_KEYWORD, KtTokens.PROTECTED_KEYWORD,
            KtTokens.PRIVATE_KEYWORD, KtTokens.EXPECT_KEYWORD, KtTokens.ACTUAL_KEYWORD, KtTokens.FINAL_KEYWORD,
            KtTokens.OPEN_KEYWORD, KtTokens.ABSTRACT_KEYWORD, KtTokens.SEALED_KEYWORD, KtTokens.CONST_KEYWORD,
            KtTokens.EXTERNAL_KEYWORD, KtTokens.OVERRIDE_KEYWORD, KtTokens.LATEINIT_KEYWORD, KtTokens.TAILREC_KEYWORD,
            KtTokens.CROSSINLINE_KEYWORD, KtTokens.VARARG_KEYWORD, KtTokens.SUSPEND_KEYWORD, KtTokens.INNER_KEYWORD,
            KtTokens.OUT_KEYWORD, KtTokens.ENUM_KEYWORD, KtTokens.ANNOTATION_KEYWORD, KtTokens.COMPANION_KEYWORD,
            KtTokens.INLINE_KEYWORD, KtTokens.NOINLINE_KEYWORD, KtTokens.REIFIED_KEYWORD, KtTokens.INFIX_KEYWORD,
            KtTokens.OPERATOR_KEYWORD, KtTokens.DATA_KEYWORD, KtTokens.IN_KEYWORD, KtTokens.HEADER_KEYWORD,
            KtTokens.IMPL_KEYWORD)
    }
}
