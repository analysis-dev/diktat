package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings.NO_BRACES_IN_CONDITIONALS_AND_LOOPS
import org.cqfn.diktat.ruleset.utils.findChildrenMatching
import org.cqfn.diktat.ruleset.utils.isSingleLineIfElse

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.BLOCK
import com.pinterest.ktlint.core.ast.ElementType.DO_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.DO_WHILE
import com.pinterest.ktlint.core.ast.ElementType.ELSE_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.FOR
import com.pinterest.ktlint.core.ast.ElementType.IF
import com.pinterest.ktlint.core.ast.ElementType.IF_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.LBRACE
import com.pinterest.ktlint.core.ast.ElementType.RBRACE
import com.pinterest.ktlint.core.ast.ElementType.WHEN
import com.pinterest.ktlint.core.ast.ElementType.WHILE
import com.pinterest.ktlint.core.ast.ElementType.WHILE_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.isPartOfComment
import com.pinterest.ktlint.core.ast.prevSibling
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtIfExpression
import org.jetbrains.kotlin.psi.KtLoopExpression
import org.jetbrains.kotlin.psi.KtWhenExpression
import org.jetbrains.kotlin.psi.psiUtil.astReplace

/**
 * Rule that checks that all conditionals and loops have braces.
 */
class BracesInConditionalsAndLoopsRule(private val configRules: List<RulesConfig>) : Rule("braces-rule") {
    private var isFixMode: Boolean = false
    private lateinit var emitWarn: EmitType

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: EmitType) {
        emitWarn = emit
        isFixMode = autoCorrect

        when (node.elementType) {
            IF -> checkIfNode(node)
            WHEN -> checkWhenBranches(node)
            FOR, WHILE, DO_WHILE -> checkLoop(node)
            else -> return
        }
    }

    /**
     * Check braces in if-else statements. Check for both IF and ELSE needs to be done in one method to discover single-line if-else statements correctly.
     */
    @Suppress("ForbiddenComment", "UnsafeCallOnNullableType", "ComplexMethod", "TOO_LONG_FUNCTION")
    private fun checkIfNode(node: ASTNode) {
        val ifPsi = node.psi as KtIfExpression
        val thenNode = ifPsi.then?.node
        val elseKeyword = ifPsi.elseKeyword
        val elseNode = ifPsi.`else`?.node
        val indent = node
            .prevSibling { it.elementType == WHITE_SPACE }
            ?.text
            ?.lines()
            ?.last()
            ?.count { it == ' ' } ?: 0

        if (node.isSingleLineIfElse()) {
            return
        }

        if (thenNode?.elementType != BLOCK) {
            NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnAndFix(configRules, emitWarn, isFixMode, "IF",
                (thenNode?.prevSibling { it.elementType == IF_KEYWORD } ?: node).startOffset, node) {
                thenNode?.run {
                    (psi as KtElement).replaceWithBlock(indent)
                    if (elseNode != null && elseKeyword != null) {
                        node.replaceChild(elseKeyword.prevSibling.node, PsiWhiteSpaceImpl(" "))
                    }
                }
                    ?: run {
                        val nodeAfterCondition = ifPsi.rightParenthesis!!.node.treeNext
                        node.insertEmptyBlockBetweenChildren(nodeAfterCondition, nodeAfterCondition, indent)
                    }
            }
        }

        if (elseKeyword != null && elseNode?.elementType != IF && elseNode?.elementType != BLOCK) {
            NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnAndFix(configRules, emitWarn, isFixMode, "ELSE",
                (elseNode?.treeParent?.prevSibling { it.elementType == ELSE_KEYWORD } ?: node).startOffset, node) {
                elseNode?.run {
                    (psi as KtElement).replaceWithBlock(indent)
                }
                    ?: run {
                        // `else` can have empty body e.g. when there is a semicolon after: `else ;`
                        node.insertEmptyBlockBetweenChildren(elseKeyword.node.treeNext, null, indent)
                    }
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkLoop(node: ASTNode) {
        val loopBody = (node.psi as KtLoopExpression).body
        val loopBodyNode = loopBody?.node
        if (loopBodyNode == null || loopBodyNode.elementType != BLOCK) {
            NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnAndFix(configRules, emitWarn, isFixMode, node.elementType.toString(), node.startOffset, node) {
                // fixme proper way to calculate indent? or get step size (instead of hardcoded 4)
                val indent = node.prevSibling { it.elementType == WHITE_SPACE }!!
                    .text
                    .lines()
                    .last()
                    .count { it == ' ' }
                loopBody?.run {
                    replaceWithBlock(indent)
                }
                    ?: run {
                        // this corresponds to do-while with empty body
                        node.insertEmptyBlockBetweenChildren(
                            node.findChildByType(DO_KEYWORD)!!.treeNext,
                            node.findChildByType(WHILE_KEYWORD)!!.treePrev,
                            indent
                        )
                    }
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkWhenBranches(node: ASTNode) {
        (node.psi as KtWhenExpression)
            .entries
            .asSequence()
            .filter { it.expression != null && it.expression!!.node.elementType == BLOCK }
            .map { it.expression as KtBlockExpression }
            .filter { block ->
                block.statements.size == 1 &&
                        block.findChildrenMatching { it.isPartOfComment() }.isEmpty()
            }
            .forEach {
                NO_BRACES_IN_CONDITIONALS_AND_LOOPS.warnAndFix(configRules, emitWarn, isFixMode, "WHEN", it.node.startOffset, it.node) {
                    it.astReplace(it.firstStatement!!.node.psi)
                }
            }
    }

    private fun KtElement.replaceWithBlock(indent: Int) {
        this.astReplace(KtBlockExpression(
            "{\n${" ".repeat(indent + INDENT_STEP)}$text\n${" ".repeat(indent)}}"
        ))
    }

    private fun ASTNode.insertEmptyBlockBetweenChildren(
        firstChild: ASTNode,
        secondChild: ASTNode?,
        indent: Int) {
        val emptyBlock = CompositeElement(ElementType.BLOCK_CODE_FRAGMENT)
        addChild(emptyBlock, firstChild)
        addChild(PsiWhiteSpaceImpl(" "), emptyBlock)
        emptyBlock.addChild(LeafPsiElement(LBRACE, "{"))
        emptyBlock.addChild(PsiWhiteSpaceImpl("\n${" ".repeat(indent)}"))
        emptyBlock.addChild(LeafPsiElement(RBRACE, "}"))
        secondChild?.let {
            replaceChild(it, PsiWhiteSpaceImpl(" "))
        }
    }
    companion object {
        private const val INDENT_STEP = 4
    }
}
