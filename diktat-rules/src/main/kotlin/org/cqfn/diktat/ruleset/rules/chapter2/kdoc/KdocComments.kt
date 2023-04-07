package org.cqfn.diktat.ruleset.rules.chapter2.kdoc

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getCommonConfiguration
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_DUPLICATE_PROPERTY
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_EXTRA_PROPERTY
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_NO_CLASS_BODY_PROPERTIES_IN_HEADER
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_NO_CONSTRUCTOR_PROPERTY
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT
import org.cqfn.diktat.ruleset.constants.Warnings.MISSING_KDOC_CLASS_ELEMENTS
import org.cqfn.diktat.ruleset.constants.Warnings.MISSING_KDOC_TOP_LEVEL
import org.cqfn.diktat.ruleset.rules.DiktatRule
import org.cqfn.diktat.ruleset.utils.*

import org.jetbrains.kotlin.KtNodeTypes.BLOCK
import org.jetbrains.kotlin.KtNodeTypes.CLASS
import org.jetbrains.kotlin.KtNodeTypes.CLASS_BODY
import org.jetbrains.kotlin.KtNodeTypes.FUN
import org.jetbrains.kotlin.KtNodeTypes.MODIFIER_LIST
import org.jetbrains.kotlin.KtNodeTypes.PRIMARY_CONSTRUCTOR
import org.jetbrains.kotlin.KtNodeTypes.PROPERTY
import org.jetbrains.kotlin.KtNodeTypes.VALUE_PARAMETER
import org.jetbrains.kotlin.KtNodeTypes.VALUE_PARAMETER_LIST
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiCommentImpl
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens.END
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens.KDOC
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens.TEXT
import org.jetbrains.kotlin.kdoc.parser.KDocKnownTag
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.lexer.KtTokens.BLOCK_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.EOL_COMMENT
import org.jetbrains.kotlin.lexer.KtTokens.IDENTIFIER
import org.jetbrains.kotlin.lexer.KtTokens.VAL_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.VAR_KEYWORD
import org.jetbrains.kotlin.lexer.KtTokens.WHITE_SPACE
import org.jetbrains.kotlin.psi.KtParameterList
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.psi.psiUtil.siblings
import org.jetbrains.kotlin.psi.stubs.elements.KtFileElementType

/**
 * This rule checks the following features in KDocs:
 * 1) All top-level (file level) functions and classes with public or internal access should have KDoc
 * 2) All internal elements in class like class, property or function should be documented with KDoc
 * 3) All properties declared in the primary constructor are documented using `@property` tag in class KDoc
 */
class KdocComments(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(KDOC_EXTRA_PROPERTY, KDOC_NO_CONSTRUCTOR_PROPERTY,
        KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT, MISSING_KDOC_CLASS_ELEMENTS, MISSING_KDOC_TOP_LEVEL,
        KDOC_DUPLICATE_PROPERTY
    )
) {
    private val config by lazy { configRules.getCommonConfiguration() }

    /**
     * @param node
     */
    override fun logic(node: ASTNode) {
        val filePath = node.getFilePath()
        if (!node.hasTestAnnotation() && !isLocatedInTest(filePath.splitPathToDirs(), config.testAnchors)) {
            when (node.elementType) {
                KtFileElementType.INSTANCE -> checkTopLevelDoc(node)
                CLASS -> checkClassElements(node)
                VALUE_PARAMETER -> checkValueParameter(node)
                PRIMARY_CONSTRUCTOR -> checkParameterList(node.findChildByType(VALUE_PARAMETER_LIST))
                BLOCK -> checkKdocPresent(node)
                else -> {
                    // this is a generated else block
                }
            }
        }
    }

    private fun checkKdocPresent(node: ASTNode) {
        node.findChildrenMatching { it.elementType == KDOC }.forEach {
            warnKdocInsideBlock(it)
        }
    }

    private fun warnKdocInsideBlock(kdocNode: ASTNode) {
        Warnings.COMMENTED_BY_KDOC.warnAndFix(
            configRules, emitWarn, isFixMode,
            "Redundant asterisk in block comment: \\**", kdocNode.startOffset, kdocNode
        ) {
            kdocNode.treeParent.addChild(PsiCommentImpl(BLOCK_COMMENT, kdocNode.text.replace("/**", "/*")), kdocNode)
            kdocNode.treeParent.removeChild(kdocNode)
        }
    }

    private fun checkParameterList(node: ASTNode?) {
        val kdocBeforeClass = node
            ?.parent { it.elementType == CLASS }
            ?.findChildByType(KDOC) ?: return

        val propertiesInKdoc = kdocBeforeClass
            .kDocTags()
            .filter { it.knownTag == KDocKnownTag.PROPERTY }
        val propertyNames = (node.psi as KtParameterList)
            .parameters
            .mapNotNull { it.nameIdentifier?.text }
        propertiesInKdoc
            .filterNot { it.getSubjectName() == null || it.getSubjectName() in propertyNames }
            .forEach { KDOC_EXTRA_PROPERTY.warn(configRules, emitWarn, isFixMode, it.text, it.node.startOffset, node) }
    }

    @Suppress("UnsafeCallOnNullableType", "ComplexMethod")
    private fun checkValueParameter(valueParameterNode: ASTNode) {
        if (valueParameterNode.parents().none { it.elementType == PRIMARY_CONSTRUCTOR } ||
                !valueParameterNode.hasChildOfType(VAL_KEYWORD) &&
                        !valueParameterNode.hasChildOfType(VAR_KEYWORD)
        ) {
            return
        }
        val prevComment = if (valueParameterNode.siblings(forward = false)
            .takeWhile { it.elementType != EOL_COMMENT && it.elementType != BLOCK_COMMENT }
            .all { it.elementType == WHITE_SPACE }
        ) {
            // take previous comment, if it's immediately before `valueParameterNode` or separated only with white space
            valueParameterNode.prevSibling { it.elementType == EOL_COMMENT || it.elementType == BLOCK_COMMENT }
        } else if (valueParameterNode.hasChildOfType(KDOC)) {
            valueParameterNode.findChildByType(KDOC)!!
        } else {
            null
        }
        val kdocBeforeClass = valueParameterNode.parent { it.elementType == CLASS }!!.findChildByType(KDOC)

        prevComment?.let {
            kdocBeforeClass?.let {
                checkKdocBeforeClass(valueParameterNode, kdocBeforeClass, prevComment)
            }
                ?: createKdocWithProperty(valueParameterNode, prevComment)
        }
            ?: kdocBeforeClass?.let {
                checkBasicKdocBeforeClass(valueParameterNode, kdocBeforeClass)
            }
            ?: createKdocBasicKdoc(valueParameterNode)
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkBasicKdocBeforeClass(node: ASTNode, kdocBeforeClass: ASTNode) {
        val propertyInClassKdoc = kdocBeforeClass
            .kDocTags()
            .firstOrNull { it.knownTag == KDocKnownTag.PROPERTY && it.getSubjectName() == node.findChildByType(IDENTIFIER)!!.text }
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
        prevComment: ASTNode
    ) {
        val propertyInClassKdoc = kdocBeforeClass
            .kDocTags()
            .firstOrNull { it.knownTag == KDocKnownTag.PROPERTY && it.getSubjectName() == node.findChildByType(IDENTIFIER)!!.text }
            ?.node
        val propertyInLocalKdoc = if (prevComment.elementType == KDOC) {
            prevComment
                .kDocTags()
                .firstOrNull { it.knownTag == KDocKnownTag.PROPERTY && it.getSubjectName() == node.findChildByType(IDENTIFIER)!!.text }
                ?.node
        } else {
            null
        }
        if (prevComment.elementType == KDOC || prevComment.elementType == BLOCK_COMMENT) {
            // there is a documentation before property that we can extract, and there is class KDoc, where we can move it to
            handleKdocAndBlock(node, prevComment, kdocBeforeClass, propertyInClassKdoc, propertyInLocalKdoc)
        } else {
            KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT.warnAndFix(configRules, emitWarn, isFixMode, node.findChildByType(IDENTIFIER)!!.text, prevComment.startOffset, node) {
                handleCommentBefore(node, kdocBeforeClass, prevComment, propertyInClassKdoc)
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun createKdocBasicKdoc(node: ASTNode) {
        if (node.getFirstChildWithType(MODIFIER_LIST).isAccessibleOutside() && !node.isOverridden()) {
            KDOC_NO_CONSTRUCTOR_PROPERTY.warnAndFix(configRules, emitWarn, isFixMode,
                "add <${node.findChildByType(IDENTIFIER)!!.text}> to KDoc", node.startOffset, node) {
                val newKdoc = KotlinParser().createNode("/**\n * @property ${node.findChildByType(IDENTIFIER)!!.text}\n */")
                val classNode = node.parent { it.elementType == CLASS }!!
                classNode.addChild(PsiWhiteSpaceImpl("\n"), classNode.firstChildNode)
                classNode.addChild(newKdoc.findChildByType(KDOC)!!, classNode.firstChildNode)
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun createKdocWithProperty(valueParameterNode: ASTNode, prevComment: ASTNode) {
        KDOC_NO_CONSTRUCTOR_PROPERTY_WITH_COMMENT.warnAndFix(configRules, emitWarn, isFixMode, prevComment.text, prevComment.startOffset, valueParameterNode) {
            val classNode = valueParameterNode.parent { it.elementType == CLASS }!!
            val newKdocText = if (prevComment.elementType == KDOC) {
                prevComment.text
            } else if (prevComment.elementType == EOL_COMMENT) {
                "/**\n * @property ${valueParameterNode.findChildByType(IDENTIFIER)!!.text} ${prevComment.text.removePrefix("//")}\n */"
            } else {
                "/**\n * @property ${valueParameterNode.findChildByType(IDENTIFIER)!!.text}${prevComment.text.removePrefix("/*").removeSuffix("*/")} */"
            }
            val newKdoc = KotlinParser().createNode(newKdocText).findChildByType(KDOC)!!
            classNode.addChild(PsiWhiteSpaceImpl("\n"), classNode.firstChildNode)
            classNode.addChild(newKdoc, classNode.firstChildNode)
            valueParameterNode.removeWithWhiteSpace(prevComment)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleKdocAndBlock(
        node: ASTNode,
        prevComment: ASTNode,
        kdocBeforeClass: ASTNode,
        propertyInClassKdoc: ASTNode?,
        propertyInLocalKdoc: ASTNode?
    ) {
        val kdocText = if (prevComment.elementType == KDOC) {
            prevComment.text
                .removePrefix("/**")
                .removeSuffix("*/")
                .trim('\n', ' ')
        } else {
            prevComment.text
                .removePrefix("/*")
                .removeSuffix("*/")
                .trim('\n', ' ')
        }
        // if property is documented with KDoc, which has a property tag inside, then it can contain some additional more complicated
        // structure, that will be hard to move automatically
        val isFixable = propertyInLocalKdoc == null
        KDOC_NO_CONSTRUCTOR_PROPERTY.warnAndFix(configRules, emitWarn, isFixMode, prevComment.text, prevComment.startOffset, node, isFixable) {
            propertyInClassKdoc?.let {
                // local docs should be appended to docs in class
                appendKdocTagContent(propertyInClassKdoc, "\n$kdocText")
            }
                ?: insertTextInKdoc(kdocBeforeClass, " * @property ${node.findChildByType(IDENTIFIER)!!.text} ${kdocText.removePrefix("*")}\n")

            node.removeWithWhiteSpace(prevComment)
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun handleCommentBefore(
        node: ASTNode,
        kdocBeforeClass: ASTNode,
        prevComment: ASTNode,
        propertyInClassKdoc: ASTNode?
    ) {
        propertyInClassKdoc?.let {
            if (propertyInClassKdoc.hasChildOfType(KDocTokens.TEXT)) {
                val kdocText = propertyInClassKdoc.findChildByType(KDocTokens.TEXT)!!
                (kdocText as LeafPsiElement).rawReplaceWithText("${kdocText.text} ${prevComment.text}")
            } else {
                propertyInClassKdoc.addChild(LeafPsiElement(KDocTokens.TEXT, prevComment.text), null)
            }
        }
            ?: run {
                insertTextInKdoc(kdocBeforeClass, "* @property ${node.findChildByType(IDENTIFIER)!!.text} ${prevComment.text.removeRange(0, 2)}\n")
            }
        node.treeParent.removeChildMergingSurroundingWhitespaces(prevComment)
    }

    private fun checkDuplicateProperties(kdoc: ASTNode) {
        val propertiesAndParams = kdoc.kDocTags()
            .filter { it.knownTag == KDocKnownTag.PROPERTY || it.knownTag == KDocKnownTag.PARAM }
        val traversedNodes: MutableSet<String?> = mutableSetOf()
        propertiesAndParams.forEach { property ->
            if (!traversedNodes.add(property.getSubjectName())) {
                KDOC_DUPLICATE_PROPERTY.warn(configRules, emitWarn, isFixMode, property.text, property.node.startOffset, kdoc)
            }
        }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun insertTextInKdoc(kdocBeforeClass: ASTNode, insertText: String) {
        val allKdocText = kdocBeforeClass.text
        val endKdoc = kdocBeforeClass.findChildByType(KDocTokens.END)!!.text
        val newKdocText = StringBuilder(allKdocText).insert(allKdocText.indexOf(endKdoc), insertText).toString()
        kdocBeforeClass.treeParent.replaceChild(kdocBeforeClass, KotlinParser().createNode(newKdocText).findChildByType(KDOC)!!)
    }

    /**
     * Append [content] to [kdocTagNode], e.g.
     * (`@property foo bar`, "baz") -> `@property foo bar baz`
     */
    private fun appendKdocTagContent(
        kdocTagNode: ASTNode, content: String,
    ) {
        kdocTagNode.findChildByType(KDocTokens.TEXT)?.let {
            kdocTagNode.replaceChild(
                it,
                LeafPsiElement(KDocTokens.TEXT, "${it.text}$content"),
            )
        }
            ?: kdocTagNode.addChild(
                LeafPsiElement(KDocTokens.TEXT, content),
                null,
            )
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
                .forEach { classElement ->
                    if (classElement.elementType == PROPERTY) {
                        // we check if property declared in class body is also documented in class header via `@property` tag
                        val classKdoc = node.getFirstChildWithType(KDOC)
                        val propertyInClassKdoc = classKdoc?.kDocTags()?.find {
                            it.knownTag == KDocKnownTag.PROPERTY && it.getSubjectName() == classElement.getIdentifierName()?.text
                        }
                        propertyInClassKdoc?.let {
                            // if property is documented as `@property`, then we suggest to move docs to the declaration inside the class body
                            KDOC_NO_CLASS_BODY_PROPERTIES_IN_HEADER.warn(configRules, emitWarn, isFixMode, classElement.text, classElement.startOffset, classElement)
                            return
                        }
                    }
                    // for everything else, we raise a missing kdoc warning
                    checkDoc(classElement, MISSING_KDOC_CLASS_ELEMENTS)
                }
        }
    }

    private fun checkTopLevelDoc(node: ASTNode) {
        // checking that all top level class declarations and functions have kDoc
        return (node.getAllChildrenWithType(CLASS) + node.getAllChildrenWithType(FUN))
            .forEach { checkDoc(it, MISSING_KDOC_TOP_LEVEL) }
    }

    /**
     * raises warning if protected, public or internal code element does not have a Kdoc
     */
    @Suppress("UnsafeCallOnNullableType")
    private fun checkDoc(node: ASTNode, warning: Warnings) {
        // if there is an annotation before function, AST is a bit more complex, so we need to look for Kdoc among
        // children of modifier list
        val kdoc = node.getFirstChildWithType(KDOC) ?: node.getFirstChildWithType(MODIFIER_LIST)?.getFirstChildWithType(KDOC)
        kdoc?.let {
            checkDuplicateProperties(kdoc)
        }
        val name = node.getIdentifierName()
        val isModifierAccessibleOutsideOrActual = node.getFirstChildWithType(MODIFIER_LIST).run {
            isAccessibleOutside() && this?.hasChildOfType(KtTokens.ACTUAL_KEYWORD) != true
        }

        if (isModifierAccessibleOutsideOrActual && kdoc == null && !isTopLevelFunctionStandard(node)) {
            warning.warn(configRules, emitWarn, isFixMode, name!!.text, node.startOffset, node)
        }
    }

    private fun isTopLevelFunctionStandard(node: ASTNode): Boolean = node.elementType == FUN && node.isStandardMethod()

    companion object {
        const val NAME_ID = "kdoc-comments"
        private val statementsToDocument = TokenSet.create(CLASS, FUN, PROPERTY)
    }
}

private fun ASTNode.removeWithWhiteSpace(prevComment: ASTNode) {
    if (prevComment.elementType == KDOC) {
        if (prevComment.treeNext.elementType == WHITE_SPACE) {
            removeChild(prevComment.treeNext)
        }
        removeChild(prevComment)
    } else {
        removeChildMergingSurroundingWhitespaces(prevComment)
    }
}

/**
 * If [child] node is surrounded by nodes with type `WHITE_SPACE`, remove [child] and merge surrounding nodes,
 * preserving only a single newline if present (i.e. leaving no empty lines after merging). In any case, [child] is removed
 * from the tree.
 */
private fun ASTNode.removeChildMergingSurroundingWhitespaces(child: ASTNode) {
    with(child) {
        if (treeNext?.elementType == WHITE_SPACE && treePrev?.elementType == WHITE_SPACE) {
            val mergedText = (treePrev.text + treeNext.text)
            val mergedSpace = if (mergedText.contains('\n')) {
                '\n' + mergedText.substringAfterLast('\n')
            } else {
                mergedText
            }
            treeParent.replaceWhiteSpaceText(treePrev, mergedSpace)
        }
        treeParent.removeChild(treeNext)
    }
    removeChild(child)
}
