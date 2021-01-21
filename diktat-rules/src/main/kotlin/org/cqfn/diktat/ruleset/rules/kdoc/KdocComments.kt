package org.cqfn.diktat.ruleset.rules.kdoc

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getCommonConfiguration
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_EXTRA_PROPERTY
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_NO_CONSTRUCTOR_PROPERTY
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT
import org.cqfn.diktat.ruleset.constants.Warnings.MISSING_KDOC_CLASS_ELEMENTS
import org.cqfn.diktat.ruleset.constants.Warnings.MISSING_KDOC_TOP_LEVEL
import org.cqfn.diktat.ruleset.utils.*

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.BLOCK_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.CLASS
import com.pinterest.ktlint.core.ast.ElementType.CLASS_BODY
import com.pinterest.ktlint.core.ast.ElementType.EOL_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.FUN
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.KDOC
import com.pinterest.ktlint.core.ast.ElementType.KDOC_END
import com.pinterest.ktlint.core.ast.ElementType.KDOC_TEXT
import com.pinterest.ktlint.core.ast.ElementType.MODIFIER_LIST
import com.pinterest.ktlint.core.ast.ElementType.PRIMARY_CONSTRUCTOR
import com.pinterest.ktlint.core.ast.ElementType.PROPERTY
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER
import com.pinterest.ktlint.core.ast.ElementType.VALUE_PARAMETER_LIST
import com.pinterest.ktlint.core.ast.ElementType.VAL_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.VAR_KEYWORD
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.parent
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.kdoc.parser.KDocKnownTag
import org.jetbrains.kotlin.psi.KtParameterList
import org.jetbrains.kotlin.psi.psiUtil.parents

/**
 * This rule checks the following features in KDocs:
 * 1) All top-level (file level) functions and classes with public or internal access should have KDoc
 * 2) All internal elements in class like class, property or function should be documented with KDoc
 * 3) All properties declared in the primary constructor are documented using `@property` tag in class KDoc
 */
class KdocComments(private val configRules: List<RulesConfig>) : Rule("kdoc-comments") {
    private var isFixMode: Boolean = false
    private lateinit var emitWarn: EmitType

    /**
     * @param node
     * @param autoCorrect
     * @param emit
     */
    override fun visit(
        node: ASTNode,
        autoCorrect: Boolean,
        emit: EmitType
    ) {
        emitWarn = emit
        isFixMode = autoCorrect

        val config = configRules.getCommonConfiguration().value
        val filePath = node.getRootNode().getFilePath()
        if (!(node.hasTestAnnotation() || isLocatedInTest(filePath.splitPathToDirs(), config.testAnchors))) {
            when (node.elementType) {
                FILE -> checkTopLevelDoc(node)
                CLASS -> checkClassElements(node)
                VALUE_PARAMETER -> checkValueParameter(node)
                PRIMARY_CONSTRUCTOR -> checkParameterList(node.findChildByType(VALUE_PARAMETER_LIST))
                else -> {
                    // this is a generated else block
                }
            }
        }
    }

    private fun checkParameterList(node: ASTNode?) {
        val kdocBeforeClass = node
            ?.parent({ it.elementType == CLASS })
            ?.findChildByType(KDOC) ?: return
        val propertiesInKdoc = kdocBeforeClass
            .kDocTags()
            ?.filter { it.knownTag == KDocKnownTag.PROPERTY }
        val propertyNames = (node.psi as KtParameterList)
            .parameters
            .mapNotNull { it.nameIdentifier?.text }
        propertiesInKdoc
            ?.filterNot { it.getSubjectName() == null || it.getSubjectName() in propertyNames }
            ?.forEach { KDOC_EXTRA_PROPERTY.warn(configRules, emitWarn, isFixMode, it.text, it.node.startOffset, node) }
    }

    @Suppress("UnsafeCallOnNullableType", "ComplexMethod")
    private fun checkValueParameter(node: ASTNode) {
        if (node.parents().none { it.elementType == PRIMARY_CONSTRUCTOR } ||
                !(node.hasChildOfType(VAL_KEYWORD) || node.hasChildOfType(VAR_KEYWORD))) {
            return
        }
        val prevComment = if (node.treePrev.elementType == WHITE_SPACE &&
                (node.treePrev.treePrev.elementType == EOL_COMMENT ||
                        node.treePrev.treePrev.elementType == BLOCK_COMMENT)) {
            node.treePrev.treePrev
        } else if (node.hasChildOfType(KDOC)) {
            node.findChildByType(KDOC)!!
        } else if (node.treePrev.elementType == BLOCK_COMMENT) {
            node.treePrev
        } else {
            null
        }
        val kdocBeforeClass = node.parent({ it.elementType == CLASS })!!.findChildByType(KDOC)

        prevComment?.let {
            kdocBeforeClass?.let {
                checkKdocBeforeClass(node, kdocBeforeClass, prevComment)
            }
                ?: run {
                    createKdocWithProperty(node, prevComment)
                }
        }
            ?: run {
                kdocBeforeClass?.let {
                    checkBasicKdocBeforeClass(node, kdocBeforeClass)
                }
                    ?: run {
                        createKdocBasicKdoc(node)
                    }
            }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkBasicKdocBeforeClass(node: ASTNode, kdocBeforeClass: ASTNode) {
        val propertyInClassKdoc = kdocBeforeClass
            .kDocTags()
            ?.firstOrNull { it.knownTag == KDocKnownTag.PROPERTY && it.getSubjectName() == node.findChildByType(IDENTIFIER)!!.text }
        if (propertyInClassKdoc == null && node.getFirstChildWithType(MODIFIER_LIST).isAccessibleOutside()) {
            KDOC_NO_CONSTRUCTOR_PROPERTY.warnAndFix(configRules, emitWarn, isFixMode,
                "add <${node.findChildByType(IDENTIFIER)!!.text}> to KDoc", node.startOffset, node) {
                insertTextInKdoc(kdocBeforeClass, " * @property ${node.findChildByType(IDENTIFIER)!!.text}\n")
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkKdocBeforeClass(
        node: ASTNode,
        kdocBeforeClass: ASTNode,
        prevComment: ASTNode) {
        val propertyInClassKdoc = kdocBeforeClass
            .kDocTags()
            ?.firstOrNull { it.knownTag == KDocKnownTag.PROPERTY && it.getSubjectName() == node.findChildByType(IDENTIFIER)!!.text }
            ?.node
        val propertyInLocalKdoc = if (prevComment.elementType == KDOC) {
            prevComment
                .kDocTags()
                ?.firstOrNull { it.knownTag == KDocKnownTag.PROPERTY && it.getSubjectName() == node.findChildByType(IDENTIFIER)!!.text }
                ?.node
        } else {
            null
        }
        if (prevComment.elementType == KDOC || prevComment.elementType == BLOCK_COMMENT) {
            handleKdocAndBlock(node, prevComment, kdocBeforeClass, propertyInClassKdoc, propertyInLocalKdoc)
        } else {
            KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT.warnAndFix(configRules, emitWarn, isFixMode, node.findChildByType(IDENTIFIER)!!.text, prevComment.startOffset, node) {
                handleCommentBefore(node, kdocBeforeClass, prevComment, propertyInClassKdoc)
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun createKdocBasicKdoc(node: ASTNode) {
        if (node.getFirstChildWithType(MODIFIER_LIST).isAccessibleOutside()) {
            KDOC_NO_CONSTRUCTOR_PROPERTY.warnAndFix(configRules, emitWarn, isFixMode,
                "add <${node.findChildByType(IDENTIFIER)!!.text}> to KDoc", node.startOffset, node) {
                val newKdoc = KotlinParser().createNode("/**\n * @property ${node.findChildByType(IDENTIFIER)!!.text}\n */")
                val classNode = node.parent({ it.elementType == CLASS })!!
                classNode.addChild(PsiWhiteSpaceImpl("\n"), classNode.firstChildNode)
                classNode.addChild(newKdoc.findChildByType(KDOC)!!, classNode.firstChildNode)
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun createKdocWithProperty(node: ASTNode, prevComment: ASTNode) {
        KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT.warnAndFix(configRules, emitWarn, isFixMode, prevComment.text, prevComment.startOffset, node) {
            val classNode = node.parent({ it.elementType == CLASS })!!
            val newKdocText = if (prevComment.elementType == KDOC) {
                prevComment.text
            } else if (prevComment.elementType == EOL_COMMENT) {
                "/**\n * @property ${node.findChildByType(IDENTIFIER)!!.text} ${prevComment.text.removePrefix("//")}\n */"
            } else {
                "/**\n * @property ${node.findChildByType(IDENTIFIER)!!.text}${prevComment.text.removePrefix("/*").removeSuffix("*/")} */"
            }
            val newKdoc = KotlinParser().createNode(newKdocText).findChildByType(KDOC)!!
            classNode.addChild(PsiWhiteSpaceImpl("\n"), classNode.firstChildNode)
            classNode.addChild(newKdoc, classNode.firstChildNode)
            if (prevComment.elementType == EOL_COMMENT) {
                node.treeParent.removeRange(prevComment, node)
            } else {
                if (prevComment.treeNext.elementType == WHITE_SPACE) {
                    node.removeChild(prevComment.treeNext)
                }
                node.removeChild(prevComment)
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleKdocAndBlock(
        node: ASTNode,
        prevComment: ASTNode,
        kdocBeforeClass: ASTNode,
        propertyInClassKdoc: ASTNode?,
        propertyInLocalKdoc: ASTNode?) {
        val kdocText = if (prevComment.elementType == KDOC) {
            prevComment.text.removePrefix("/**").removeSuffix("*/")
        } else {
            prevComment.text.removePrefix("/*").removeSuffix("*/")
        }
        val isFixable = (propertyInClassKdoc != null && propertyInLocalKdoc != null) ||
                (propertyInClassKdoc == null && propertyInLocalKdoc == null && kdocText
                    .replace("\n+".toRegex(), "")
                    .lines()
                    .size != 1)
        KDOC_NO_CONSTRUCTOR_PROPERTY.warnAndFix(configRules, emitWarn, !isFixable, prevComment.text, prevComment.startOffset, node, !isFixable) {
            if (propertyInClassKdoc == null && propertyInLocalKdoc == null) {
                insertTextInKdoc(kdocBeforeClass, " * @property ${node.findChildByType(IDENTIFIER)!!.text} ${kdocText.replace("\n+".toRegex(), "").removePrefix("*")}")
            } else {
                insertTextInKdoc(kdocBeforeClass, "${kdocText.trim()}\n")
            }

            if (prevComment.treeNext.elementType == WHITE_SPACE) {
                node.removeChild(prevComment.treeNext)
            }
            node.removeChild(prevComment)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleCommentBefore(
        node: ASTNode,
        kdocBeforeClass: ASTNode,
        prevComment: ASTNode,
        propertyInClassKdoc: ASTNode?) {
        propertyInClassKdoc?.let {
            if (propertyInClassKdoc.hasChildOfType(KDOC_TEXT)) {
                val kdocText = propertyInClassKdoc.findChildByType(KDOC_TEXT)!!
                (kdocText as LeafPsiElement).replaceWithText("${kdocText.text} ${prevComment.text}")
            } else {
                propertyInClassKdoc.addChild(LeafPsiElement(KDOC_TEXT, prevComment.text), null)
            }
        }
            ?: run {
                insertTextInKdoc(kdocBeforeClass, "* @property ${node.findChildByType(IDENTIFIER)!!.text} ${prevComment.text.removeRange(0, 2)}\n")
            }
        node.treeParent.removeRange(prevComment, node)
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun insertTextInKdoc(kdocBeforeClass: ASTNode, insertText: String) {
        val allKdocText = kdocBeforeClass.text
        val endKdoc = kdocBeforeClass.findChildByType(KDOC_END)!!.text
        val newKdocText = StringBuilder(allKdocText).insert(allKdocText.indexOf(endKdoc), insertText).toString()
        kdocBeforeClass.treeParent.replaceChild(kdocBeforeClass, KotlinParser().createNode(newKdocText).findChildByType(KDOC)!!)
    }

    private fun checkClassElements(node: ASTNode) {
        val modifier = node.getFirstChildWithType(MODIFIER_LIST)
        val classBody = node.getFirstChildWithType(CLASS_BODY)

        // if parent class is public or internal than we can check it's internal code elements
        if (classBody != null && modifier.isAccessibleOutside()) {
            classBody
                .getChildren(statementsToDocument)
                .filterNot {
                    (it.elementType == FUN && it.isStandardMethod()) || (it.elementType == FUN && it.isOverridden()) || (it.elementType == PROPERTY && it.isOverridden())
                }
                .forEach { checkDoc(it, MISSING_KDOC_CLASS_ELEMENTS) }
        }
    }

    private fun checkTopLevelDoc(node: ASTNode) =
            // checking that all top level class declarations and functions have kDoc
            (node.getAllChildrenWithType(CLASS) + node.getAllChildrenWithType(FUN))
                .forEach { checkDoc(it, MISSING_KDOC_TOP_LEVEL) }

    /**
     * raises warning if protected, public or internal code element does not have a Kdoc
     */
    @Suppress("UnsafeCallOnNullableType")
    private fun checkDoc(node: ASTNode, warning: Warnings) {
        val kdoc = node.getFirstChildWithType(KDOC)
        val modifier = node.getFirstChildWithType(MODIFIER_LIST)
        val name = node.getIdentifierName()

        if (modifier.isAccessibleOutside() && kdoc == null && !isTopLevelFunctionStandard(node)) {
            warning.warn(configRules, emitWarn, isFixMode, name!!.text, node.startOffset, node)
        }
    }

    private fun isTopLevelFunctionStandard(node: ASTNode): Boolean = node.elementType == FUN && node.isStandardMethod()

    companion object {
        private val statementsToDocument = TokenSet.create(CLASS, FUN, PROPERTY)
    }
}
