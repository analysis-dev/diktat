package org.cqfn.diktat.ruleset.rules.chapter3

import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getCommonConfiguration
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings.TRAILING_COMMA

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.COLLECTION_LITERAL_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.COMMA
import com.pinterest.ktlint.core.ast.ElementType.DESTRUCTURING_DECLARATION
import com.pinterest.ktlint.core.ast.ElementType.DESTRUCTURING_DECLARATION_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.INDICES
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.STRING_TEMPLATE
import com.pinterest.ktlint.core.ast.ElementType.TYPE_ARGUMENT_LIST
import com.pinterest.ktlint.core.ast.ElementType.TYPE_PARAMETER
import com.pinterest.ktlint.core.ast.ElementType.TYPE_PARAMETER_LIST
import com.pinterest.ktlint.core.ast.ElementType.TYPE_PROJECTION
import com.pinterest.ktlint.core.ast.ElementType.VALUE_ARGUMENT
import com.pinterest.ktlint.core.ast.ElementType.VALUE_ARGUMENT_LIST
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER_LIST
import com.pinterest.ktlint.core.ast.ElementType.WHEN_CONDITION_WITH_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.WHEN_ENTRY
import com.pinterest.ktlint.core.ast.children
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.psi.psiUtil.siblings
import org.jetbrains.kotlin.utils.addToStdlib.ifNotEmpty

/**
 * [1] Enumerations (In another rule)
 * [2] Value arguments
 * [3] Class properties and parameters
 * [4] Function value parameters
 * [5] Parameters with optional type (including setters)
 * [6] Indexing suffix
 * [7] Lambda parameters
 * [8] when entry
 * [9] Collection literals (in annotations)Type arguments
 * [10] Type arguments
 * [11] Type parameters
 * [12] Destructuring declarations
 */
class TrailingCommaRule(private val configRules: List<RulesConfig>) : Rule("trailing-comma") {
    private var isFixMode: Boolean = false
    private val configuration by lazy {
        TrailingCommaConfiguration(
            this.configRules.getRuleConfig(TRAILING_COMMA)?.configuration ?: emptyMap()
        )
    }
    private lateinit var emitWarn: EmitType

    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitType
    ) {
        emitWarn = emit
        isFixMode = autoCorrect

        val commonConfig by configRules.getCommonConfiguration()
        if (commonConfig.kotlinVersion >= ktVersion) {
            val (type, config) = when (node.elementType) {
                VALUE_ARGUMENT_LIST -> Pair(VALUE_ARGUMENT, configuration.getParam("valueArgument"))
                VALUE_PARAMETER_LIST -> Pair(VALUE_PARAMETER, configuration.getParam("valueParameter"))
                INDICES -> Pair(REFERENCE_EXPRESSION, configuration.getParam("referenceExpression"))
                WHEN_ENTRY -> Pair(WHEN_CONDITION_WITH_EXPRESSION, configuration.getParam("whenConditions"))
                COLLECTION_LITERAL_EXPRESSION -> Pair(STRING_TEMPLATE, configuration.getParam("collectionLiteral"))
                TYPE_ARGUMENT_LIST -> Pair(TYPE_PROJECTION, configuration.getParam("typeArgument"))
                TYPE_PARAMETER_LIST -> Pair(TYPE_PARAMETER, configuration.getParam("typeParameter"))
                DESTRUCTURING_DECLARATION -> Pair(
                    DESTRUCTURING_DECLARATION_ENTRY,
                    configuration.getParam("destructuringDeclaration")
                )
                else -> return
            }
            val astNode = node
                .children()
                .toList()
                .ifNotEmpty { this.lastOrNull { it.elementType == type } }
            astNode?.checkTrailingComma(config)
        }
    }

    private fun ASTNode.checkTrailingComma(config: Boolean) {
        val isNextComma = this.siblings(true).map { it.elementType }.contains(COMMA)
        if (!isNextComma && config) {
            // we should write type of node in warning, to make it easier for user to find the parameter
            TRAILING_COMMA.warnAndFix(configRules, emitWarn, isFixMode, "after ${this.elementType}: ${this.text}", this.startOffset, this) {
                val parent = this.treeParent
                parent.addChild(LeafPsiElement(COMMA, ","), this.treeNext)
            }
        }
    }

    /**
     * Configuration for trailing comma
     */
    class TrailingCommaConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * @param name parameters name
         *
         * @return param based on its name
         */
        fun getParam(name: String) = config[name]?.toBoolean() ?: false
    }

    companion object {
        val ktVersion = KotlinVersion(1, 4)
    }
}
