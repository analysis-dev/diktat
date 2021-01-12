package org.cqfn.diktat.ruleset.rules.comments

import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings.HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE
import org.cqfn.diktat.ruleset.constants.Warnings.HEADER_MISSING_OR_WRONG_COPYRIGHT
import org.cqfn.diktat.ruleset.constants.Warnings.HEADER_NOT_BEFORE_PACKAGE
import org.cqfn.diktat.ruleset.constants.Warnings.HEADER_WRONG_FORMAT
import org.cqfn.diktat.ruleset.constants.Warnings.KDOC_CONTAINS_DATE_OR_AUTHOR
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_COPYRIGHT_YEAR
import org.cqfn.diktat.ruleset.utils.copyrightWords
import org.cqfn.diktat.ruleset.utils.findChildAfter
import org.cqfn.diktat.ruleset.utils.findChildBefore
import org.cqfn.diktat.ruleset.utils.getAllChildrenWithType
import org.cqfn.diktat.ruleset.utils.getFirstChildWithType
import org.cqfn.diktat.ruleset.utils.kDocTags
import org.cqfn.diktat.ruleset.utils.moveChildBefore

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import com.pinterest.ktlint.core.ast.ElementType.BLOCK_COMMENT
import com.pinterest.ktlint.core.ast.ElementType.FILE
import com.pinterest.ktlint.core.ast.ElementType.IMPORT_LIST
import com.pinterest.ktlint.core.ast.ElementType.KDOC
import com.pinterest.ktlint.core.ast.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.core.ast.ElementType.WHITE_SPACE
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.kdoc.parser.KDocKnownTag
import org.jetbrains.kotlin.kdoc.psi.impl.KDocTag

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField

/**
 * Visitor for header comment in .kt file:
 * 1) Ensure header comment is at the very top and properly formatted (has newline after KDoc end)
 * 2) Ensure copyright exists and is properly formatted
 * 3) Ensure there are no dates or authors
 * 4) Ensure files with many or zero classes have proper description
 */
@Suppress("ForbiddenComment")
class HeaderCommentRule(private val configRules: List<RulesConfig>) : Rule("header-comment") {
    private var isFixMode: Boolean = false
    private lateinit var emitWarn: EmitType

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: EmitType) {
        isFixMode = autoCorrect
        emitWarn = emit

        if (node.elementType == FILE) {
            checkCopyright(node)
            if (checkHeaderKdocPosition(node)) {
                checkHeaderKdoc(node)
            }
        }
    }

    private fun checkHeaderKdoc(node: ASTNode) {
        node.findChildBefore(PACKAGE_DIRECTIVE, KDOC)?.let { headerKdoc ->
            if (headerKdoc.treeNext != null && headerKdoc.treeNext.elementType == WHITE_SPACE &&
                    headerKdoc.treeNext.text.count { it == '\n' } != 2) {
                HEADER_WRONG_FORMAT.warnAndFix(configRules, emitWarn, isFixMode,
                    "header KDoc should have a new line after", headerKdoc.startOffset, headerKdoc) {
                    node.replaceChild(headerKdoc.treeNext, PsiWhiteSpaceImpl("\n\n"))
                }
            }
        }
            ?: run {
                val numDeclaredClassesAndObjects = node.getAllChildrenWithType(ElementType.CLASS).size +
                        node.getAllChildrenWithType(ElementType.OBJECT_DECLARATION).size
                if (numDeclaredClassesAndObjects != 1) {
                    HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE.warn(configRules, emitWarn, isFixMode,
                        "there are $numDeclaredClassesAndObjects declared classes and/or objects", node.startOffset, node)
                }
            }
    }

    /**
     * If corresponding rule is enabled, checks if header KDoc is positioned correctly and moves it in fix mode.
     * Algorithm is as follows: if there is no KDoc at the top of file (before package directive) and the one after imports
     * isn't bound to any identifier, than this KDoc is misplaced header KDoc.
     *
     * @return true if position check is not needed or if header KDoc is positioned correctly or it was moved by fix mode
     */
    @Suppress("FUNCTION_BOOLEAN_PREFIX")
    private fun checkHeaderKdocPosition(node: ASTNode): Boolean {
        val firstKdoc = node.findChildAfter(IMPORT_LIST, KDOC)
        // if `firstKdoc.treeParent` is File then it's a KDoc not bound to any other structures
        if (node.findChildBefore(PACKAGE_DIRECTIVE, KDOC) == null && firstKdoc != null && firstKdoc.treeParent.elementType == FILE) {
            HEADER_NOT_BEFORE_PACKAGE.warnAndFix(configRules, emitWarn, isFixMode, "header KDoc is located after package or imports", firstKdoc.startOffset, firstKdoc) {
                node.moveChildBefore(firstKdoc, node.getFirstChildWithType(PACKAGE_DIRECTIVE), true)
                // ensure there is no empty line between copyright and header kdoc
                node.findChildBefore(PACKAGE_DIRECTIVE, BLOCK_COMMENT)?.apply {
                    if (treeNext.elementType == WHITE_SPACE) {
                        node.replaceChild(treeNext, PsiWhiteSpaceImpl("\n"))
                    } else {
                        node.addChild(PsiWhiteSpaceImpl("\n"), this.treeNext)
                    }
                }
            }
            if (!isFixMode) {
                return false
            }
        }
        return true
    }

    private fun makeCopyrightCorrectYear(copyrightText: String): String {
        val hyphenYear = hyphenRegex.find(copyrightText)

        hyphenYear?.let {
            val copyrightYears = hyphenYear.value.split("-")
            if (copyrightYears[1].toInt() != curYear) {
                val validYears = "${copyrightYears[0]}-$curYear"
                return copyrightText.replace(hyphenRegex, validYears)
            }
        }

        val afterCopyrightYear = afterCopyrightRegex.find(copyrightText)
        val copyrightYears = afterCopyrightYear?.value?.split("(c)", "(C)", "©")
        return if (copyrightYears != null && copyrightYears[1].trim().toInt() != curYear) {
            val validYears = "${copyrightYears[0]}-$curYear"
            copyrightText.replace(afterCopyrightRegex, validYears)
        } else {
            ""
        }
    }

    @Suppress("TOO_LONG_FUNCTION", "ComplexMethod")
    private fun checkCopyright(node: ASTNode) {
        val configuration = CopyrightConfiguration(configRules.getRuleConfig(HEADER_MISSING_OR_WRONG_COPYRIGHT)?.configuration
            ?: emptyMap())
        if (!configuration.isCopyrightMandatory() && !configuration.hasCopyrightText()) {
            return
        }

        val copyrightText = configuration.getCopyrightText()

        val headerComment = node.findChildBefore(PACKAGE_DIRECTIVE, BLOCK_COMMENT)
        val isWrongCopyright = headerComment != null && !headerComment.text.flatten().contains(copyrightText.flatten())
        val isMissingCopyright = headerComment == null && configuration.isCopyrightMandatory()
        val isCopyrightInsideKdoc = (node.getAllChildrenWithType(KDOC) + node.getAllChildrenWithType(ElementType.EOL_COMMENT))
            .any { commentNode ->
                copyrightWords.any { commentNode.text.contains(it, ignoreCase = true) }
            }
        if (isWrongCopyright || isMissingCopyright || isCopyrightInsideKdoc) {
            val freeText = when {
                // If `isCopyrightInsideKdoc` then `isMissingCopyright` is true too, but warning text from `isCopyrightInsideKdoc` is preferable.
                isCopyrightInsideKdoc -> "copyright is placed inside KDoc, but should be inside a block comment"
                isWrongCopyright -> "copyright comment doesn't have correct copyright text"
                isMissingCopyright -> "copyright is mandatory, but is missing"
                else -> error("Should never get to this point")
            }
            HEADER_MISSING_OR_WRONG_COPYRIGHT.warnAndFix(configRules, emitWarn, isFixMode, freeText, node.startOffset, node) {
                headerComment?.let { node.removeChild(it) }
                // do not insert empty line before header kdoc
                val newLines = node.findChildBefore(PACKAGE_DIRECTIVE, KDOC)?.let { "\n" } ?: "\n\n"
                node.addChild(PsiWhiteSpaceImpl(newLines), node.firstChildNode)
                node.addChild(LeafPsiElement(BLOCK_COMMENT,
                    """
                        |/*
                        |${handleMultilineCopyright(copyrightText)}
                        |*/
                    """.trimMargin()),
                    node.firstChildNode
                )
            }
        }

        val copyrightWithCorrectYear = makeCopyrightCorrectYear(copyrightText)

        if (copyrightWithCorrectYear.isNotEmpty()) {
            WRONG_COPYRIGHT_YEAR.warnAndFix(configRules, emitWarn, isFixMode, "year should be $curYear", node.startOffset, node) {
                (headerComment as LeafElement).replaceWithText(headerComment.text.replace(copyrightText, copyrightWithCorrectYear))
            }
        }
    }

    /**
     * Deletes all spaces and newlines
     * Used to compare copyrights in yaml and file
     */
    private fun String.flatten(): String =
            replace("\n", "")
                .replace(" ", "")

    /**
     * If it is multiline copyright, this method deletes spaces in empty lines.
     * Otherwise, if it is one line copyright, it returns it with 4 spaces at the beginning.
     */
    private fun handleMultilineCopyright(copyrightText: String): String {
        if (copyrightText.startsWith(" ")) {
            return copyrightText
                .lines()
                .dropWhile { it.isBlank() }
                .reduce { acc, nextLine ->
                    when {
                        nextLine.isBlank() -> "$acc\n"
                        else -> "$acc\n$nextLine"
                    }
                }
        }

        return "    $copyrightText"
    }

    /**
     * Configuration for copyright
     */
    class CopyrightConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * @return Whether the copyright is mandatory in all files
         */
        fun isCopyrightMandatory() = config["isCopyrightMandatory"]?.toBoolean() ?: false

        /**
         * Whether copyright text is present in the configuration
         */
        internal fun hasCopyrightText() = config.keys.contains("copyrightText")

        /**
         * @return text of copyright as configured in the configuration file
         */
        fun getCopyrightText() = config["copyrightText"] ?: error("Copyright is not set in configuration")
    }

    companion object {
        val hyphenRegex = Regex("""\b(\d+-\d+)\b""")
        val afterCopyrightRegex = Regex("""((©|\([cC]\))+ *\d+)""")
        val curYear = LocalDate.now().year
    }
}
