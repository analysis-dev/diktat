package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.utils.*

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.ANNOTATION_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.PRIMARY_CONSTRUCTOR
import com.pinterest.ktlint.core.ast.ElementType.SECONDARY_CONSTRUCTOR
import com.pinterest.ktlint.core.ast.isWhiteSpace
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl

/**
 * This rule makes each annotation applied to a class, method or constructor is on its own line. Except: if first annotation of constructor, class or method
 */
class AnnotationNewLineRule(private val configRules: List<RulesConfig>) : Rule("annotation-new-line") {
    private var isFixMode: Boolean = false
    private lateinit var emitWarn: EmitType

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: EmitType) {
        emitWarn = emit
        isFixMode = autoCorrect

        when (node.elementType) {
            CLASS, FUN, PRIMARY_CONSTRUCTOR, SECONDARY_CONSTRUCTOR -> checkAnnotation(node)
            else -> return
        }
    }

    private fun checkAnnotation(node: ASTNode) {
        node.findChildByType(MODIFIER_LIST)?.let { modList ->
            fixAnnotation(modList)
        }
    }

    private fun fixAnnotation(node: ASTNode) {
        if (node.getAllChildrenWithType(ANNOTATION_ENTRY).size <= 1) {
            return
        }

        node.getAllChildrenWithType(ANNOTATION_ENTRY).forEach {
            if (!it.isFollowedByNewlineWithComment() || !it.isBeginNewLineWithComment()) {
                deleteSpaces(it, !it.isFollowedByNewlineWithComment())
            }
        }
    }

    private fun deleteSpaces(node: ASTNode,
                             rightSide: Boolean) {
        Warnings.ANNOTATION_NEW_LINE.warnAndFix(configRules, emitWarn, isFixMode, "${node.text} not on a single line",
            node.startOffset, node) {
            if (rightSide) {
                if (node.treeNext?.isWhiteSpace() == true) {
                    node.removeChild(node.treeNext)
                }
                node.treeParent.addChild(PsiWhiteSpaceImpl("\n"), node.treeNext)
            }

            if (node == node.treeParent.getFirstChildWithType(node.elementType)) {
                // Current node is ANNOTATION_ENTRY. treeParent(ModifierList) -> treeParent(PRIMARY_CONSTRUCTOR)
                // Checks if there is a white space before grandparent node
                val hasSpaceBeforeGrandparent = node
                    .treeParent
                    .treeParent
                    .treePrev
                    .isWhiteSpace()
                if (hasSpaceBeforeGrandparent) {
                    (node.treeParent.treeParent.treePrev as LeafPsiElement).replaceWithText("\n")
                }
            }
        }
    }
}
