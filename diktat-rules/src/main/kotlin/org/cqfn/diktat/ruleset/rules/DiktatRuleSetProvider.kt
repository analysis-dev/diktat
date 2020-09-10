package org.cqfn.diktat.ruleset.rules

import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider
import org.cqfn.diktat.common.config.rules.RulesConfigReader
import org.cqfn.diktat.ruleset.rules.comments.CommentsRule
import org.cqfn.diktat.ruleset.rules.comments.HeaderCommentRule
import org.cqfn.diktat.ruleset.rules.files.BlankLinesRule
import org.cqfn.diktat.ruleset.rules.files.FileSize
import org.cqfn.diktat.ruleset.rules.files.FileStructureRule
import org.cqfn.diktat.ruleset.rules.files.IndentationRule
import org.cqfn.diktat.ruleset.rules.files.NewlinesRule
import org.cqfn.diktat.ruleset.rules.kdoc.KdocComments
import org.cqfn.diktat.ruleset.rules.kdoc.KdocFormatting
import org.cqfn.diktat.ruleset.rules.kdoc.KdocMethods
import org.slf4j.LoggerFactory

/**
 * this constant will be used everywhere in the code to mark usage of Diktat ruleset
 */
const val DIKTAT_RULE_SET_ID = "diktat-ruleset"

class DiktatRuleSetProvider(private val jsonRulesConfig: String = "diktat-analysis.yml") : RuleSetProvider {
    override fun get(): RuleSet {
        log.debug("Will run $DIKTAT_RULE_SET_ID with $jsonRulesConfig (it can be placed to the run directory or the default file from resources will be used)")
        val configRules = RulesConfigReader(javaClass.classLoader).readResource(jsonRulesConfig) ?: listOf()
        val rules = listOf(
                ::CommentsRule,
                ::KdocComments,
                ::KdocMethods,
                ::KdocFormatting,
                ::FileNaming,
                ::PackageNaming,
                ::FileSize,
                ::IdentifierNaming,
                ::ClassLikeStructuresOrderRule,
                ::BracesInConditionalsAndLoopsRule,
                ::BlockStructureBraces,
                ::EmptyBlock,
                ::EnumsSeparated,
                ::SingleLineStatementsRule,
                ::ConsecutiveSpacesRule,
                ::LongNumericalValuesSeparatedRule,
                ::MultipleModifiersSequence,
                ::AnnotationNewLineRule,
                ::HeaderCommentRule,
                ::SortRule,
                ::StringConcatenationRule,
                ::LineLength,
                ::BlankLinesRule,
                ::WhiteSpaceRule,
                ::WhenMustHaveElseRule,
                ::FileStructureRule,  // this rule should be right before indentation because it should operate on already valid code
                ::NewlinesRule,  // newlines need to be inserted right before fixing indentation
                ::IndentationRule  // indentation rule should be the last because it fixes formatting after all the changes done by previous rules
        )
                .map {
                    it.invoke(configRules)
                }
                .toTypedArray()
        return RuleSet(
                DIKTAT_RULE_SET_ID,
                *rules
        )
    }

    companion object {
        private val log = LoggerFactory.getLogger(DiktatRuleSetProvider::class.java)
    }
}
