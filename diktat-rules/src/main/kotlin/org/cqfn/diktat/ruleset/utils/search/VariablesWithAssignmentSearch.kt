package org.cqfn.diktat.ruleset.utils.search

import com.pinterest.ktlint.core.ast.ElementType
import org.cqfn.diktat.ruleset.utils.findAllNodesWithSpecificType
import org.cqfn.diktat.ruleset.utils.isGoingAfter
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtBinaryExpression
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtNameReferenceExpression
import org.jetbrains.kotlin.psi.KtProperty

class VariablesWithAssignmentSearch(fileNode: ASTNode,
                                    filterForVariables: (KtProperty) -> Boolean) : VariablesSearch(fileNode, filterForVariables) {

    /**
     * searching for all assignments of variables in current context [this]
     */
    override fun KtElement.getAllSearchResults(property: KtProperty): List<KtNameReferenceExpression> {
        return this.node.findAllNodesWithSpecificType(ElementType.BINARY_EXPRESSION)
                // filtering out all usages that are declared in the same context but are going before the variable declaration
                // AND checking that there is an assignment
                .filter {
                    // FixMe: bug is here with a search of postfix/prefix variables assignment (like ++).
                    // FixMe: Currently we check only val a = 5, ++a is not checked here
                    // FixMe: also there can be some tricky cases with setters, but I am not able to imagine them now
                    it.isGoingAfter(property.node) &&
                            (it.psi as KtBinaryExpression).operationToken == ElementType.EQ &&
                            (it.psi as KtBinaryExpression).left?.node?.elementType == ElementType.REFERENCE_EXPRESSION
                }
                .map { (it.psi as KtBinaryExpression).left as KtNameReferenceExpression }
                // checking that name of the property in usage matches with the name in the declaration
                .filter { it.getReferencedNameAsName() == property.nameAsName }
                .filterNot { expression ->
                    // to avoid false triggering on objects' fields with same name as property
                    expression.isReferenceToFieldOfObject() ||
                            // to exclude usages of local properties from other context (shadowed) and lambda arguments with same name
                            isReferenceToOtherVariableWithSameName(expression, this, property)
                }
                .toList()
    }
}

// the default value for filtering condition is always true
fun ASTNode.findAllVariablesWithAssignments(filterForVariables: (KtProperty) -> Boolean = ::default) =
        VariablesWithAssignmentSearch(this, filterForVariables).collectVariables()
