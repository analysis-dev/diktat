package org.cqfn.diktat.ruleset.rules.chapter6

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings.RUN_IN_SCRIPT
import org.cqfn.diktat.ruleset.utils.*

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.DOT_QUALIFIED_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.LAMBDA_ARGUMENT
import com.pinterest.ktlint.core.ast.ElementType.LAMBDA_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.PARENTHESIZED
import com.pinterest.ktlint.core.ast.ElementType.SCRIPT_INITIALIZER
import com.pinterest.ktlint.core.ast.ElementType.VALUE_ARGUMENT
import com.pinterest.ktlint.core.ast.ElementType.VALUE_ARGUMENT_LIST
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement

/**
 * Rule that checks if kts script contains other functions except run code
 * In .kts files allow use only property declaration, function, classes, and code inside `run` block
 * In gradle.kts files allow to call binary expression with EQ, expression and dot qualified expression in addition to everything used in .kts files
 */
class RunInScript(private val configRules: List<RulesConfig>) : Rule("run-script") {
    private var isFixMode: Boolean = false
    private lateinit var emitWarn: EmitType

    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitType
    ) {
        isFixMode = autoCorrect
        emitWarn = emit

        if (node.elementType == SCRIPT_INITIALIZER && node.getRootNode().getFilePath().isKotlinScript()) {
            if (node.getRootNode().getFilePath().isGradleScript()) {
                checkGradleNode(node)
            } else {
                checkScript(node)
            }
        }
    }

    private fun checkGradleNode(node: ASTNode) {
        val astNode = if (node.hasEqBinaryExpression()) {
            return
        } else {
            when (node.firstChildNode.elementType) {
                PARENTHESIZED -> node.firstChildNode
                else -> node
            }
        }
        if (!astNode.hasChildOfType(CALL_EXPRESSION) && !astNode.hasChildOfType(DOT_QUALIFIED_EXPRESSION)) {
            warnRunInScript(astNode)
        }
    }

    private fun checkScript(node: ASTNode) {
        val isLambdaArgument = node.firstChildNode.hasChildOfType(LAMBDA_ARGUMENT)
        val isLambdaInsideValueArgument = node.firstChildNode.findChildByType(VALUE_ARGUMENT_LIST)?.findChildByType(VALUE_ARGUMENT)?.findChildByType(LAMBDA_EXPRESSION) != null
        if (!isLambdaArgument && !isLambdaInsideValueArgument) {
            warnRunInScript(node)
        }
    }

    private fun warnRunInScript(node: ASTNode) {
        RUN_IN_SCRIPT.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset, node) {
            if (node.firstChildNode.elementType != DOT_QUALIFIED_EXPRESSION) {
                val parent = node.treeParent
                val newNode = KotlinParser().createNode("run {\n ${node.text}\n} \n")
                val newScript = CompositeElement(SCRIPT_INITIALIZER)
                parent.addChild(newScript, node)
                newScript.addChild(newNode)
                parent.removeChild(node)
            }
        }
    }
}
