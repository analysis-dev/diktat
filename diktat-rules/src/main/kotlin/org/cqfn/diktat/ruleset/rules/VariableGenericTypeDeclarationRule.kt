package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.TYPE_ARGUMENT_LIST
import com.pinterest.ktlint.core.ast.ElementType.TYPE_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.jetbrains.kotlin.com.intellij.lang.ASTNode

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

    private fun handleProperty(node: ASTNode) {

        val callExpr = node.findChildByType(CALL_EXPRESSION)

        val rightSide = Regex("<([a-zA-Z, <>]*)>").find(node.findChildByType(CALL_EXPRESSION)?.text ?: "")
        val leftSide = Regex("<([a-zA-Z, <>]*)>").find(node.findChildByType(TYPE_REFERENCE)?.text ?: "")

        if ((rightSide != null && leftSide != null) && rightSide.groupValues.first() == leftSide.groupValues.first()) {
            Warnings.GENERIC_VARIABLE_WRONG_DECLARATION.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset, node) {
                callExpr!!.removeChild(callExpr.findChildByType(TYPE_ARGUMENT_LIST)!!)
            }
        }

        if (leftSide == null && rightSide != null) {
            Warnings.GENERIC_VARIABLE_WRONG_DECLARATION.warn(configRules, emitWarn, isFixMode, node.text, node.startOffset, node)
        }

    }
}
