package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.BOOLEAN_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.CALL_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.CHARACTER_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.CHARACTER_LITERAL
import com.pinterest.ktlint.core.ast.ElementType.CLOSING_QUOTE
import com.pinterest.ktlint.core.ast.ElementType.EQ
import com.pinterest.ktlint.core.ast.ElementType.FLOAT_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.FLOAT_LITERAL
import com.pinterest.ktlint.core.ast.ElementType.INTEGER_CONSTANT
import com.pinterest.ktlint.core.ast.ElementType.INTEGER_LITERAL
import com.pinterest.ktlint.core.ast.ElementType.NULL
import com.pinterest.ktlint.core.ast.ElementType.NULLABLE_TYPE
import com.pinterest.ktlint.core.ast.ElementType.OPEN_QUOTE
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.STRING_TEMPLATE
import com.pinterest.ktlint.core.ast.ElementType.TRUE_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.TYPE_REFERENCE
import com.pinterest.ktlint.core.ast.ElementType.USER_TYPE
import com.pinterest.ktlint.core.ast.ElementType.VAL_KEYWORD
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.Warnings.NULLABLE_PROPERTY_TYPE
import org.cqfn.diktat.ruleset.utils.KotlinParser
import org.cqfn.diktat.ruleset.utils.hasChildOfType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.CompositeElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.tree.IElementType

class NullableTypeRule(private val configRules: List<RulesConfig>) : Rule("nullable-type") {

    private lateinit var emitWarn: ((offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit)
    private var isFixMode: Boolean = false

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
        isFixMode = autoCorrect
        emitWarn = emit

        if (node.elementType == PROPERTY)
            checkProperty(node)
    }

    private fun checkProperty(node: ASTNode) {
        if (node.hasChildOfType(VAL_KEYWORD) && node.hasChildOfType(EQ) && node.hasChildOfType(TYPE_REFERENCE) && node.hasChildOfType(NULL)) {
            val fixedParam = isFixable(node)
            NULLABLE_PROPERTY_TYPE.warnAndFix(configRules, emitWarn, isFixMode, "initialize explicitly",
                    node.findChildByType(NULL)!!.startOffset, fixedParam != null) {
                if (fixedParam != null) findSubstitution(node, fixedParam)
            }
        }
    }

    private fun isFixable(node: ASTNode): FixedParam? {
        val reference = node.findChildByType(TYPE_REFERENCE)!!.findChildByType(NULLABLE_TYPE)!!.findChildByType(USER_TYPE)?.findChildByType(REFERENCE_EXPRESSION)
                ?: return null
        return when (reference.text) {
            "Boolean" -> FixedParam(BOOLEAN_CONSTANT, TRUE_KEYWORD, "true")
            "Int", "Short", "Byte" -> FixedParam(INTEGER_CONSTANT, INTEGER_LITERAL, "0")
            "Double" -> FixedParam(FLOAT_CONSTANT, FLOAT_LITERAL, "0.0")
            "Float" -> FixedParam(FLOAT_CONSTANT, FLOAT_LITERAL, "0.0F")
            "Long" -> FixedParam(INTEGER_CONSTANT, INTEGER_LITERAL, "0L")
            "Char" -> FixedParam(CHARACTER_CONSTANT, CHARACTER_LITERAL, "\'\'")
            "String" -> FixedParam(null, null, "", true)
            else -> isFixableForCollection(reference.text)
        }
    }

    private fun isFixableForCollection(referenceText: String) =
            when(referenceText) {
                "List", "Iterable" -> FixedParam(null, null, "emptyList()")
                "Map" -> FixedParam(null, null, "emptyMap()")
                "Array" -> FixedParam(null, null, "emptyArray()")
                "Set" -> FixedParam(null, null, "emptySet()")
                "Sequence" -> FixedParam(null, null, "emptySequence()")
                "Queue" -> FixedParam(null, null, "LinkedList()")
                "MutableList" -> FixedParam(null, null, "mutableListOf()")
                "MutableMap" -> FixedParam(null, null, "mutableMapOf()")
                "MutableSet" -> FixedParam(null, null, "mutableSetOf()")
                "LinkedList" -> FixedParam(null, null, "LinkedList()")
                "LinkedHashMap" -> FixedParam(null, null, "LinkedHashMap()")
                "LinkedHashSet" -> FixedParam(null, null, "LinkedHashSet()")
                else -> null
            }

    private fun findSubstitution(node: ASTNode, fixedParam: FixedParam) {
        if (fixedParam.isString)
            replaceValueForString(node)
        else if (fixedParam.insertConstantType != null && fixedParam.insertType != null)
            replaceValue(node, fixedParam.insertConstantType, fixedParam.insertType, fixedParam.textNode)
        else
            replaceValueByText(node, fixedParam.textNode)
    }

    private fun replaceValueByText(node: ASTNode, nodeText: String) {
        val newNode = KotlinParser().createNode(nodeText)
        if (newNode.elementType == CALL_EXPRESSION) {
            node.addChild(newNode, node.findChildByType(NULL))
            node.removeChild(node.findChildByType(NULL)!!)
        }
    }

    private fun replaceValue(node: ASTNode, insertConstantType: IElementType, insertType: IElementType, textNode: String) {
        val value = CompositeElement(insertConstantType)
        node.addChild(value, node.findChildByType(NULL)!!)
        node.removeChild(node.findChildByType(NULL)!!)
        value.addChild(LeafPsiElement(insertType, textNode))
    }

    private fun replaceValueForString(node: ASTNode) {
        val value = CompositeElement(STRING_TEMPLATE)
        node.addChild(value, node.findChildByType(NULL)!!)
        node.removeChild(node.findChildByType(NULL)!!)
        value.addChild(LeafPsiElement(OPEN_QUOTE, ""))
        value.addChild(LeafPsiElement(CLOSING_QUOTE, ""))
    }

    data class FixedParam(val insertConstantType: IElementType?, val insertType: IElementType?, val textNode: String, val isString: Boolean = false)
}
