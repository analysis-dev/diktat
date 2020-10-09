package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.COLONCOLON
import com.pinterest.ktlint.core.ast.ElementType.DOT_QUALIFIED_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.LITERAL_STRING_TEMPLATE_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.LONG_STRING_TEMPLATE_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.SHORT_STRING_TEMPLATE_ENTRY
import com.pinterest.ktlint.core.ast.ElementType.SHORT_TEMPLATE_ENTRY_START
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.utils.findAllNodesWithSpecificType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement

/**
 * In String templates there should not be redundant curly braces. In case of using a not complex statement (one argument)
 * there should not be curly braces.
 *
 * FixMe: The important caveat here: in "$foo" kotlin compiler adds implicit call to foo.toString() in case foo type is not string.
 */
class StringTemplateFormatRule(private val configRules: List<RulesConfig>) : Rule("string-template-format") {
    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        emitWarn = emit
        isFixMode = autoCorrect

        when (node.elementType) {
            LONG_STRING_TEMPLATE_ENTRY -> handleLongStringTemplate(node)
            SHORT_STRING_TEMPLATE_ENTRY -> handleShortStringTemplate(node)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleLongStringTemplate(node: ASTNode) {
        // Checking if in long templates {a.foo()} there are function calls or class toString call
        if (node.findAllNodesWithSpecificType(COLONCOLON).isEmpty() &&
                node.findAllNodesWithSpecificType(DOT_QUALIFIED_EXPRESSION).isEmpty()) {
            Warnings.STRING_TEMPLATE_CURLY_BRACES.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset, node) {
                val identifierName = node.findChildByType(REFERENCE_EXPRESSION)
                if (identifierName != null) {
                    val shortTemplate = CompositeElement(SHORT_STRING_TEMPLATE_ENTRY)
                    val reference = CompositeElement(REFERENCE_EXPRESSION)

                    node.treeParent.addChild(shortTemplate, node)
                    shortTemplate.addChild(LeafPsiElement(SHORT_TEMPLATE_ENTRY_START, "$"), null)
                    shortTemplate.addChild(reference)
                    reference.addChild(LeafPsiElement(IDENTIFIER, identifierName.text))
                    node.treeParent.removeChild(node)
                } else {
                    val stringTemplate = node.treeParent
                    val appropriateText = node.text.trim('$', '{', '}')
                    stringTemplate.addChild(LeafPsiElement(LITERAL_STRING_TEMPLATE_ENTRY, appropriateText), node)
                    stringTemplate.removeChild(node)
                }
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleShortStringTemplate(node: ASTNode) {
        val identifierName = node.findChildByType(REFERENCE_EXPRESSION)?.text

        if (identifierName != null && node.treeParent.text.trim('"', '$') == identifierName) {
            Warnings.STRING_TEMPLATE_QUOTES.warnAndFix(configRules, emitWarn, isFixMode, node.text, node.startOffset, node) {
                val identifier = node.findChildByType(REFERENCE_EXPRESSION)!!.copyElement()
                // node.treeParent is String template that we need to delete
                node.treeParent.treeParent.addChild(identifier, node.treeParent)
                node.treeParent.treeParent.removeChild(node.treeParent)
            }
        }
    }
}
