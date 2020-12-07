/**
 * Utility classes for [IndentationRule]
 */

package org.cqfn.diktat.ruleset.utils.indentation

import org.cqfn.diktat.ruleset.rules.files.IndentationError
import org.jetbrains.kotlin.com.intellij.psi.PsiWhiteSpace

/**
 * @property configuration configuration of indentation rule
 */
internal abstract class CustomIndentationChecker(protected val configuration: IndentationConfig) {
    /**
     * This method checks if this white space is an exception from general rule
     * If true, checks if it is properly indented and fixes
     *
     * @param whiteSpace PSI element of type [PsiWhiteSpace]
     * @param indentError and [IndentationError] on this node
     * @return null true if node is not an exception, CheckResult otherwise
     */
    abstract fun checkNode(whiteSpace: PsiWhiteSpace, indentError: IndentationError): CheckResult?
}

/**
 * @property adjustNext Indicates whether the indent returned by this exception checker needs to be applied to other nodes with same parent
 * @property includeLastChild Indicates whether the white space before the last child node of the initiator node should have increased indent or not
 * @property isCorrect whether indentation check is correct
 * @property expectedIndent expected indentation
 */
internal data class CheckResult(
    val isCorrect: Boolean,
    val expectedIndent: Int,
    val adjustNext: Boolean,
    val includeLastChild: Boolean = true) {
    companion object {
        /**
         * @param actual actual indentation
         * @param expected expected indentation
         * @param adjustNext see [CheckResult.adjustNext]
         * @param includeLastChild see [CheckResult.includeLastChild]
         * @return an instance of [CheckResult]
         */
        fun from(actual: Int,
                 expected: Int,
                 adjustNext: Boolean = false,
                 includeLastChild: Boolean = true) =
                CheckResult(actual == expected, expected, adjustNext, includeLastChild)
    }
}
