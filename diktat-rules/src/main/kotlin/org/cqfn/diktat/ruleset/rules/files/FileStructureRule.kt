package org.cqfn.diktat.ruleset.rules.files

import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getCommonConfiguration
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings.FILE_CONTAINS_ONLY_COMMENTS
import org.cqfn.diktat.ruleset.constants.Warnings.FILE_INCORRECT_BLOCKS_ORDER
import org.cqfn.diktat.ruleset.constants.Warnings.FILE_NO_BLANK_LINE_BETWEEN_BLOCKS
import org.cqfn.diktat.ruleset.constants.Warnings.FILE_UNORDERED_IMPORTS
import org.cqfn.diktat.ruleset.constants.Warnings.FILE_WILDCARD_IMPORTS
import org.cqfn.diktat.ruleset.rules.PackageNaming.Companion.PACKAGE_SEPARATOR
import org.cqfn.diktat.ruleset.utils.StandardPlatforms
import org.cqfn.diktat.ruleset.utils.copyrightWords
import org.cqfn.diktat.ruleset.utils.handleIncorrectOrder
import org.cqfn.diktat.ruleset.utils.moveChildBefore

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.BLOCK_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.EOL_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.FILE_ANNOTATION_LIST
import com.pinterest.ktlint.core.ast.ElementType.IMPORT_DIRECTIVE
import com.pinterest.ktlint.core.ast.ElementType.IMPORT_LIST
import com.pinterest.ktlint.core.ast.ElementType.KDOC
import com.pinterest.ktlint.core.ast.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.children
import com.pinterest.ktlint.core.ast.isPartOfComment
import com.pinterest.ktlint.core.ast.isWhiteSpace
import com.pinterest.ktlint.core.ast.nextSibling
import com.pinterest.ktlint.core.ast.prevSibling
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.com.intellij.psi.tree.TokenSet
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtImportDirective
import org.jetbrains.kotlin.psi.psiUtil.siblings

/**
 * Visitor for checking internal file structure.
 * 1. Checks file contains not only comments
 * 2. Ensures the following blocks order: Copyright, Header Kdoc, @file annotation, package name, Import statements,
 *    top class header and top function header comments, top-level classes or top-level functions
 * 3. Ensures there is a blank line between these blocks
 * 4. Ensures imports are ordered alphabetically without blank lines
 * 5. Ensures there are no wildcard imports
 */
class FileStructureRule(private val configRules: List<RulesConfig>) : Rule("file-structure") {
    private var isFixMode: Boolean = false
    private val domainName by lazy {
        configRules
            .getCommonConfiguration()
            .value
            .domainName
    }
    private val standardImportsAsName = StandardPlatforms
        .values()
        .map { it to it.packages }
        .toMap()
        .mapValues { (_, value) ->
            value.map { it.split(PACKAGE_SEPARATOR).map(Name::identifier) }
        }
    private lateinit var emitWarn: EmitType

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: EmitType) {
        isFixMode = autoCorrect
        emitWarn = emit

        if (node.elementType == ElementType.FILE) {
            val wildcardImportsConfig = WildCardImportsConfig(
                this.configRules.getRuleConfig(FILE_WILDCARD_IMPORTS)?.configuration ?: emptyMap()
            )
            val importsGroupingConfig = ImportsGroupingConfig(
                this.configRules.getRuleConfig(FILE_UNORDERED_IMPORTS)?.configuration ?: emptyMap()
            )
            node.findChildByType(IMPORT_LIST)
                ?.let { checkImportsOrder(it, wildcardImportsConfig, importsGroupingConfig) }
            if (checkFileHasCode(node)) {
                checkCodeBlocksOrderAndEmptyLines(node)
            }
            return
        }
    }

    @Suppress("FUNCTION_BOOLEAN_PREFIX")
    private fun checkFileHasCode(node: ASTNode): Boolean {
        val codeTokens = TokenSet.andNot(
            TokenSet.ANY,
            TokenSet.create(WHITE_SPACE, KDOC, BLOCK_COMMENT, EOL_COMMENT, PACKAGE_DIRECTIVE, IMPORT_LIST)
        )
        val hasCode = node.getChildren(codeTokens).isNotEmpty()
        if (!hasCode) {
            val freeText = if (node.text.isEmpty()) "file is empty" else "file contains no code"
            FILE_CONTAINS_ONLY_COMMENTS.warn(configRules, emitWarn, isFixMode, freeText, node.startOffset, node)
        }
        return hasCode
    }

    @Suppress("ComplexMethod", "TOO_LONG_FUNCTION")
    private fun checkCodeBlocksOrderAndEmptyLines(node: ASTNode) {
        // From KtFile.kt: 'scripts have no package directive, all other files must have package directives'.
        // Kotlin compiler itself enforces it's position in the file if it is present.
        // If package directive is missing in .kt file (default package), the node is still present in the AST.
        val packageDirectiveNode = (node.psi as KtFile)
            .packageDirective
            ?.takeUnless { it.isRoot }
            ?.node
        // There is a private property node.psi.importLists, but it's size can't be > 1 in valid kotlin code. It exists to help in situations
        // when, e.g. merge conflict marker breaks the imports list. We shouldn't handle this situation here.
        val importsList = (node.psi as KtFile)
            .importList
            ?.takeIf { it.imports.isNotEmpty() }
            ?.node

        // this node will be an anchor with respect to which we will look for all other nodes
        val firstCodeNode = packageDirectiveNode
            ?: importsList
            ?: node.children().firstOrNull {
                // taking nodes with actual code
                !it.isWhiteSpace() && !it.isPartOfComment() &&
                        // but not the ones we are going to move
                        it.elementType != FILE_ANNOTATION_LIST &&
                        // if we are here, then IMPORT_LIST either is not present in the AST, or is empty. Either way, we don't need to select it.
                        it.elementType != IMPORT_LIST &&
                        // if we are here, then package is default and we don't need to select the empty PACKAGE_DIRECTIVE node.
                        it.elementType != PACKAGE_DIRECTIVE
            }
            ?: return  // at this point it means the file contains only comments
        // We consider the first block comment of the file to be the one that possibly contains copyright information.
        var copyrightComment = firstCodeNode.prevSibling { it.elementType == BLOCK_COMMENT }
            ?.takeIf { blockCommentNode ->
                copyrightWords.any { blockCommentNode.text.contains(it, ignoreCase = true) }
            }
        var headerKdoc = firstCodeNode.prevSibling { it.elementType == KDOC }
        // Annotations with target`file` can only be placed before `package` directive.
        var fileAnnotations = node.findChildByType(FILE_ANNOTATION_LIST)
        // We also collect all other elements that are placed on top of the file.
        // These may be other comments, so we just place them before the code starts.
        val otherNodesBeforeCode = firstCodeNode.siblings(forward = false)
            .filterNot {
                it.isWhiteSpace() ||
                        it == copyrightComment || it == headerKdoc || it == fileAnnotations
            }
            .toList()
            .reversed()

        // checking order
        listOfNotNull(copyrightComment, headerKdoc, fileAnnotations, *otherNodesBeforeCode.toTypedArray()).handleIncorrectOrder({
            getSiblingBlocks(copyrightComment, headerKdoc, fileAnnotations, firstCodeNode, otherNodesBeforeCode)
        }) { astNode, beforeThisNode ->
            FILE_INCORRECT_BLOCKS_ORDER.warnAndFix(configRules, emitWarn, isFixMode, astNode.text.lines().first(), astNode.startOffset, astNode) {
                val result = node.moveChildBefore(astNode, beforeThisNode, true)
                result.newNodes.first().run {
                    // reassign values to the nodes that could have been moved
                    when (elementType) {
                        BLOCK_COMMENT -> copyrightComment = this
                        KDOC -> headerKdoc = this
                        FILE_ANNOTATION_LIST -> fileAnnotations = this
                    }
                }
                astNode.treeNext?.let { node.replaceChild(it, PsiWhiteSpaceImpl("\n\n")) }
            }
        }

        // checking empty lines
        insertNewlinesBetweenBlocks(listOf(copyrightComment, headerKdoc, fileAnnotations, packageDirectiveNode, importsList))
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkImportsOrder(
        node: ASTNode,
        wildCardImportsConfig: WildCardImportsConfig,
        importsGroupingConfig: ImportsGroupingConfig
    ) {
        val imports = node.getChildren(TokenSet.create(IMPORT_DIRECTIVE)).toList()

        // importPath can be null if import name cannot be parsed, which should be a very rare case, therefore !! should be safe here
        imports
            .filter {
                (it.psi as KtImportDirective).importPath!!.run {
                    isAllUnder && toString() !in wildCardImportsConfig.allowedWildcards
                }
            }
            .forEach { FILE_WILDCARD_IMPORTS.warn(configRules, emitWarn, isFixMode, it.text, it.startOffset, it) }

        val sortedImportsGroups = if (importsGroupingConfig.useRecommendedImportsOrder) {
            regroupImports(imports.map { it.psi as KtImportDirective })
                .map { group -> group.map { it.node } }
        } else {
            listOf(imports)
        }
            .map { group -> group.sortedBy { it.text } }

        if (sortedImportsGroups.flatten() != imports) {
            FILE_UNORDERED_IMPORTS.warnAndFix(configRules, emitWarn, isFixMode, "${sortedImportsGroups.flatten().first().text}...", node.startOffset, node) {
                rearrangeImports(node, imports, sortedImportsGroups)
            }
        }
    }

    private fun rearrangeImports(
        node: ASTNode,
        imports: List<ASTNode>,
        sortedImportsGroups: List<List<ASTNode>>) {
        require(node.elementType == IMPORT_LIST)
        // move all commented lines among import before imports block
        node.getChildren(TokenSet.create(EOL_COMMENT))
            .forEach {
                node.treeParent.addChild(it.clone() as ASTNode, node)
                node.treeParent.addChild(PsiWhiteSpaceImpl("\n"), node)
            }

        node.removeRange(imports.first(), imports.last())
        sortedImportsGroups.filterNot { it.isEmpty() }
            .run {
                forEachIndexed { groupIndex, group ->
                    group.forEachIndexed { index, importNode ->
                        node.addChild(importNode, null)
                        if (index != group.size - 1) {
                            node.addChild(PsiWhiteSpaceImpl("\n"), null)
                        }
                    }
                    if (groupIndex != size - 1) {
                        node.addChild(PsiWhiteSpaceImpl("\n\n"), null)
                    }
                }
            }
    }

    private fun insertNewlinesBetweenBlocks(blocks: List<ASTNode?>) {
        blocks.forEach { astNode ->
            // if package directive is missing, node is still present, but it's text is empty, so we need to check treeNext to get meaningful results
            astNode?.nextSibling { it.text.isNotEmpty() }?.apply {
                if (elementType == WHITE_SPACE && text.count { it == '\n' } != 2) {
                    FILE_NO_BLANK_LINE_BETWEEN_BLOCKS.warnAndFix(configRules, emitWarn, isFixMode, astNode.text.lines().first(),
                        astNode.startOffset, astNode) {
                        (this as LeafPsiElement).replaceWithText("\n\n${text.replace("\n", "")}")
                    }
                }
            }
        }
    }

    /**
     * @return a pair of nodes between which [this] node should be placed, i.e. after the first and before the second element
     */
    private fun ASTNode.getSiblingBlocks(
        copyrightComment: ASTNode?,
        headerKdoc: ASTNode?,
        fileAnnotations: ASTNode?,
        firstCodeNode: ASTNode,
        otherNodesBeforeFirst: List<ASTNode>
    ): Pair<ASTNode?, ASTNode> = when (this) {
        copyrightComment -> null to listOfNotNull(headerKdoc, fileAnnotations, otherNodesBeforeFirst.firstOrNull(), firstCodeNode).first()
        headerKdoc -> copyrightComment to (fileAnnotations ?: otherNodesBeforeFirst.firstOrNull() ?: firstCodeNode)
        fileAnnotations -> (headerKdoc ?: copyrightComment) to (otherNodesBeforeFirst.firstOrNull() ?: firstCodeNode)
        else -> (headerKdoc ?: copyrightComment) to firstCodeNode
    }

    @Suppress("TYPE_ALIAS")
    private fun regroupImports(imports: List<KtImportDirective>): List<List<KtImportDirective>> {
        val (android, notAndroid) = imports.partition {
            it.isStandard(StandardPlatforms.ANDROID)
        }

        val (ownDomain, tmp) = notAndroid.partition { import ->
            import
                .importPath
                ?.fqName
                ?.pathSegments()
                ?.zip(domainName.split(PACKAGE_SEPARATOR).map(Name::identifier))
                ?.all { it.first == it.second }
                ?: false
        }

        val (others, javaAndKotlin) = tmp.partition {
            !it.isStandard(StandardPlatforms.JAVA) && !it.isStandard(StandardPlatforms.KOTLIN)
        }

        val (java, kotlin) = javaAndKotlin.partition { it.isStandard(StandardPlatforms.JAVA) }

        return listOf(android, ownDomain, others, java, kotlin)
    }

    private fun KtImportDirective.isStandard(platformName: StandardPlatforms) = standardImportsAsName[platformName]?.any { names ->
        names.zip(importPath?.fqName?.pathSegments() ?: emptyList())
            .all { it.first == it.second }
    } ?: false

    /**
     * [RuleConfiguration] for wildcard imports
     */
    class WildCardImportsConfig(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * A list of imports that are allowed to use wildcards. Input is in a form "foo.bar.*,foo.baz.*".
         */
        val allowedWildcards = config["allowedWildcards"]?.split(",")?.map { it.trim() } ?: emptyList()
    }

    /**
     * [RuleConfiguration] for imports grouping according to the recommendation from diktat code style
     */
    class ImportsGroupingConfig(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * Use imports grouping according to recommendation 3.1
         */
        val useRecommendedImportsOrder = config["useRecommendedImportsOrder"]?.toBoolean() ?: true
    }
}
