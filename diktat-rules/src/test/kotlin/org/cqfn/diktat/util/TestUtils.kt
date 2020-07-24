package org.cqfn.diktat.util

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.Rule
import org.assertj.core.api.Assertions
import org.assertj.core.api.SoftAssertions
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.utils.log

const val TEST_FILE_NAME = "/TestFileName.kt"

@Suppress("ForbiddenComment")
fun lintMethod(rule: Rule,
               code: String,
               vararg lintErrors: LintError,
               rulesConfigList: List<RulesConfig>? = null) {
    val res = mutableListOf<LintError>()
    KtLint.lint(
            KtLint.Params(
                    fileName = TEST_FILE_NAME,
                    text = code,
                    ruleSets = listOf(DiktatRuleSetProvider4Test(rule, rulesConfigList).get()),
                    cb = { e, _ -> res.add(e) }
            )
    )
    Assertions.assertThat(res)
            .hasSize(lintErrors.size)
            .allSatisfy { actual ->
                val expected = lintErrors[res.indexOf(actual)]
                SoftAssertions.assertSoftly {
                    it.assertThat(actual.line).`as`("Line").isEqualTo(expected.line)
                    it.assertThat(actual.col).`as`("Column").isEqualTo(expected.col)
                    it.assertThat(actual.ruleId).`as`("Rule id").isEqualTo(expected.ruleId)
                    it.assertThat(actual.detail).`as`("Detailed message").isEqualTo(expected.detail)
                    // fixme: in ktlint canBeAutoCorrected is not included in equals/hashCode for `backward compatibility`
                }
            }
}

internal fun Rule.format(text: String, fileName: String,
                         rulesConfigList: List<RulesConfig>? = emptyList()): String {
    return KtLint.format(
            KtLint.Params(
                    text = text,
                    ruleSets = listOf(DiktatRuleSetProvider4Test(this, rulesConfigList).get()),
                    fileName = fileName,
                    cb = { lintError, _ ->
                        log.warn("Received linting error: $lintError")
                    }
            )
    )
}
