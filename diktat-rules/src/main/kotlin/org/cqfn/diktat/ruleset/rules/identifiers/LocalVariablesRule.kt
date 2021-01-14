package org.cqfn.diktat.ruleset.rules.identifiers

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings.LOCAL_VARIABLE_EARLY_DECLARATION
import org.cqfn.diktat.ruleset.utils.containsOnlyConstants
import org.cqfn.diktat.ruleset.utils.getDeclarationScope
import org.cqfn.diktat.ruleset.utils.getLineNumber
import org.cqfn.diktat.ruleset.utils.lastLineNumber
import org.cqfn.diktat.ruleset.utils.numNewLines
import org.cqfn.diktat.ruleset.utils.search.findAllVariablesWithUsages

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import com.pinterest.ktlint.core.ast.isPartOfComment
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace
import org.jetbrains.kotlin.psi.KtBlockExpression
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty
import org.jetbrains.kotlin.psi.psiUtil.getParentOfType
import org.jetbrains.kotlin.psi.psiUtil.parents
import org.jetbrains.kotlin.psi.psiUtil.parentsWithSelf
import org.jetbrains.kotlin.psi.psiUtil.referenceExpression
import org.jetbrains.kotlin.psi.psiUtil.siblings
import org.jetbrains.kotlin.psi.psiUtil.startOffset

/**
 * This rule checks that local variables are declared close to the point where they are first used.
 * Current algorithm assumes that scopes are always `BLOCK`s.
 * 1. Warns if there are statements between variable declaration and it's first usage
 * 2. It is allowed to declare variables in outer scope compared to usage scope. It could be useful to store state, e.g. between loop iterations.
 *
 * Current limitations due to usage of AST only:
 * * Only properties without initialization or initialized with expressions based on constants are supported.
 * * Properties initialized with constructor calls cannot be distinguished from method call and are no supported.
 */
class LocalVariablesRule(private val configRules: List<RulesConfig>) : Rule("local-variables") {
    private var isFixMode: Boolean = false
    private lateinit var emitWarn: EmitType

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: EmitType) {
        emitWarn = emit
        isFixMode = autoCorrect

        if (node.elementType == FILE) {
            // collect all local properties and associate with corresponding references
            val propertiesToUsages = collectLocalPropertiesWithUsages(node)

            // find all usages which include more than one property
            val multiPropertyUsages = groupPropertiesByUsages(propertiesToUsages)

            multiPropertyUsages
                .forEach { (statement, properties) ->
                    handleConsecutiveDeclarations(statement, properties)
                }

            propertiesToUsages
                .filterNot { it.key in multiPropertyUsages.values.flatten() }
                .forEach { handleLocalProperty(it.key, it.value) }
        }
    }

    private fun collectLocalPropertiesWithUsages(node: ASTNode) = node
        .findAllVariablesWithUsages { propertyNode ->
            propertyNode.isLocal && propertyNode.name != null && propertyNode.parent is KtBlockExpression &&
                    (propertyNode.isVar && propertyNode.initializer == null ||
                            (propertyNode.initializer?.containsOnlyConstants() ?: false) ||
                            (propertyNode.initializer as? KtCallExpression).isWhitelistedMethod())
        }

        .filterNot { it.value.isEmpty() }

    @Suppress("TYPE_ALIAS")
    private fun groupPropertiesByUsages(propertiesToUsages: Map<KtProperty, List<KtNameReferenceExpression>>) = propertiesToUsages
        .mapValues { (property, usages) ->
            getFirstUsageStatementOrBlock(usages, property.getDeclarationScope())
        }
        .map { it.value to it.key }
        .groupByTo(mutableMapOf(), { it.first }) { it.second }
        .filter { it.value.size > 1 }
        .toMap<PsiElement, List<KtProperty>>()

    @Suppress("UnsafeCallOnNullableType")
    private fun handleLocalProperty(property: KtProperty, usages: List<KtNameReferenceExpression>) {
        val declarationScope = property.getDeclarationScope()

        val firstUsageStatementLine = getFirstUsageStatementOrBlock(usages, declarationScope).node.getLineNumber()
        val firstUsage = usages.minByOrNull { it.node.getLineNumber() }!!

        // should skip val and var before it's statement
        val offset = property
            .siblings(forward = true, withItself = false)
            .takeWhile { it != getFirstUsageStatementOrBlock(usages, declarationScope) }
            .filter { it is KtProperty }
            .count()
        checkLineNumbers(property, firstUsageStatementLine, firstUsageLine = firstUsage.node.getLineNumber(), offset = offset)
    }

    @Suppress("TOO_LONG_FUNCTION")
    private fun handleConsecutiveDeclarations(statement: PsiElement, properties: List<KtProperty>) {
        val numLinesAfterLastProp =
                properties
                    .last()
                    .node
                    .treeNext
                    .takeIf { it.elementType == WHITE_SPACE }
                    ?.let {
                        // minus one is needed to except \n after property
                        it.numNewLines() - 1
                    }
                    ?: 0

        val sortedProperties = properties.sortedBy { it.node.getLineNumber() }
        // need to check that properties are declared consecutively with only maybe empty lines
        sortedProperties
            .zip(
                (properties.size - 1 downTo 0).map { index ->
                    val siblings = sortedProperties[properties.lastIndex - index].siblings(forward = true, withItself = false)

                    // Also we need to count number of comments to skip. See `should skip comments` test
                    // For the last property we don't need to count, because they will be counted in checkLineNumbers
                    // We count number of comments beginning from next property
                    val numberOfComments = siblings
                        .takeWhile { it != statement }
                        .dropWhile { it !is KtProperty }
                        .filter { it.node.isPartOfComment() }
                        .count()

                    // We should also skip all vars that were not included in properties list, but they are between statement and current property
                    val numberOfVarWithInitializer = siblings
                        .takeWhile { it != statement }
                        .filter { it is KtProperty && it !in properties }
                        .count()

                    // If it is not last property we should consider number on new lines after last property in list
                    if (index != 0) {
                        index + numLinesAfterLastProp + numberOfComments + numberOfVarWithInitializer
                    } else {
                        index + numberOfComments + numberOfVarWithInitializer
                    }
                }
            )
            .forEach { (property, offset) ->
                checkLineNumbers(property, statement.node.getLineNumber(), offset)
            }
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun checkLineNumbers(
        property: KtProperty,
        firstUsageStatementLine: Int,
        offset: Int = 0,
        firstUsageLine: Int? = null) {
        val numLinesToSkip = property
            .siblings(forward = true, withItself = false)
            .takeWhile { it is PsiWhiteSpace || it.node.isPartOfComment() }
            .let { siblings ->
                siblings
                    .last()
                    .node
                    .lastLineNumber() - siblings
                    .first()
                    .node
                    .getLineNumber() - 1
            }

        if (firstUsageStatementLine - numLinesToSkip != property.node.lastLineNumber() + 1 + offset) {
            LOCAL_VARIABLE_EARLY_DECLARATION.warn(configRules, emitWarn, isFixMode,
                warnMessage(property.name!!, property.node.getLineNumber(), firstUsageLine
                    ?: firstUsageStatementLine), property.startOffset, property.node)
        }
    }

    /**
     * Returns the [KtBlockExpression] with which a property should be compared.
     *
     * @return either the line on which the property is used if it is first used in the same scope, or the block in the same scope as declaration
     */
    @Suppress("UnsafeCallOnNullableType", "GENERIC_VARIABLE_WRONG_DECLARATION")
    private fun getFirstUsageStatementOrBlock(usages: List<KtNameReferenceExpression>, declarationScope: KtBlockExpression?): PsiElement {
        val firstUsage = usages.minByOrNull { it.node.getLineNumber() }!!
        val firstUsageScope = firstUsage.getParentOfType<KtBlockExpression>(true)

        return if (firstUsageScope == declarationScope) {
            // property is first used in the same scope where it is declared, we check line of statement where it is first used
            firstUsage
                .parents
                .find { it.parent == declarationScope }!!
        } else {
            // first usage is in deeper block compared to declaration, need to check how close is declaration to the first line of the block
            usages.minByOrNull { it.node.getLineNumber() }!!
                .parentsWithSelf
                .find { it.parent == declarationScope }!!
        }
    }

    private fun KtCallExpression?.isWhitelistedMethod() =
            this?.run {
                (referenceExpression() as KtNameReferenceExpression).getReferencedName() in functionInitializers &&
                        valueArguments.isEmpty()
            } ?: false

    private fun warnMessage(
        name: String,
        declared: Int,
        used: Int) = "<$name> is declared on line <$declared> and is used for the first time on line <$used>"

    companion object {
        private var functionInitializers = listOf(
            "emptyList", "emptySet", "emptyMap", "emptyArray", "emptySequence",
            "listOf", "setOf", "mapOf", "arrayOf", "arrayListOf",
            "mutableListOf", "mutableSetOf", "mutableMapOf",
            "linkedMapOf", "linkedSetOf"
        )
    }
}
