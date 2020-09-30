package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.DOT_QUALIFIED_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.TYPE_ARGUMENT_LIST
import com.pinterest.ktlint.core.ast.ElementType.TYPE_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.utils.getAllChildrenWithType
import org.cqfn.diktat.ruleset.utils.hasChildOfType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.kotlin.psi.KtProperty

/**
 * This Rule checks if declaration of a generic variable is appropriate.
 * Not recommended: val myVariable: Map<Int, String> = emptyMap<Int, String>() or val myVariable = emptyMap<Int, String>()
 * Recommended: val myVariable: Map<Int, String> = emptyMap()
 */
// FIXME: we now don't have access to return types, so we can perform this check only if explicit type is present, but should be able also if it's not.
class VariableGenericTypeDeclarationRule(private val configRules: List<RulesConfig>) : Rule("variable-generic-type") {
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        emitWarn = emit
        isFixMode = autoCorrect

        when(node.elementType) {
            PROPERTY, VALUE_PARAMETER -> handleProperty(node)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleProperty(node: ASTNode) {
        val callExpr = node.findChildByType(CALL_EXPRESSION)
                ?: node.findChildByType(DOT_QUALIFIED_EXPRESSION)?.getAllChildrenWithType(CALL_EXPRESSION)?.lastOrNull()

        val rightSide = (callExpr?.psi as? KtCallExpression)?.typeArgumentList?.arguments
        val leftSide = if (node.elementType == PROPERTY) {
            (node.psi as? KtProperty)?.typeReference?.typeElement?.typeArgumentsAsTypes
        } else {
            (node.psi as? KtParameter)?.typeReference?.typeElement?.typeArgumentsAsTypes
        }

        if ((rightSide != null && leftSide != null) &&
                rightSide.size == leftSide.size &&
                rightSide.zip(leftSide).all { (first, second) -> first.text == second.text }) {
            Warnings.GENERIC_VARIABLE_WRONG_DECLARATION.warnAndFix(configRules, emitWarn, isFixMode,
                    "type arguments are unnecessary in ${callExpr.text}", node.startOffset, node) {
                callExpr.removeChild(callExpr.findChildByType(TYPE_ARGUMENT_LIST)!!)
            }
        }

        if (leftSide == null && rightSide != null) {
            Warnings.GENERIC_VARIABLE_WRONG_DECLARATION.warn(configRules, emitWarn, isFixMode, node.text, node.startOffset, node)
        }
    }
}
