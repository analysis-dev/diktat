package org.cqfn.diktat.util

import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.LintError
import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.RuleSet
import com.pinterest.ktlint.core.RuleSetProvider
import org.assertj.core.api.Assertions
import org.assertj.core.api.SoftAssertions
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.ruleset.utils.log
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.konan.file.File
import java.util.concurrent.atomic.AtomicInteger

internal val testFileName = "${File.separator}TestFileName.kt"

internal fun List<LintError>.assertEquals(vararg expectedLintErrors: LintError) =
        Assertions.assertThat(this)
                .hasSize(expectedLintErrors.size)
                .allSatisfy { actual ->
                    val expected = expectedLintErrors[this.indexOf(actual)]
                    SoftAssertions.assertSoftly {
                        it.assertThat(actual.line).`as`("Line").isEqualTo(expected.line)
                        it.assertThat(actual.col).`as`("Column").isEqualTo(expected.col)
                        it.assertThat(actual.ruleId).`as`("Rule id").isEqualTo(expected.ruleId)
                        it.assertThat(actual.detail).`as`("Detailed message").isEqualTo(expected.detail)
                        it.assertThat(actual.canBeAutoCorrected).`as`("Can be autocorrected").isEqualTo(expected.canBeAutoCorrected)
                    }
                }

internal fun format(ruleSetProviderRef: (rulesConfigList: List<RulesConfig>?) -> RuleSetProvider,
                    text: String,
                    fileName: String,
                    rulesConfigList: List<RulesConfig>? = null,
                    cb: (lintError: LintError, corrected: Boolean) -> Unit = defaultCallback): String {
    return KtLint.format(
            KtLint.Params(
                    text = text,
                    ruleSets = listOf(ruleSetProviderRef.invoke(rulesConfigList).get()),
                    fileName = fileName,
                    cb = cb,
                    userData = mapOf("file_path" to fileName)
            )
    )
}

internal val defaultCallback: (lintError: LintError, corrected: Boolean) -> Unit = { lintError, _ ->
    log.warn("Received linting error: $lintError")
}

/**
 * This utility function lets you run arbitrary code on every node of given [code].
 * It also provides you with counter which can be incremented inside [applyToNode] and then will be compared to [expectedAsserts].
 * This allows you to keep track of how many assertions have actually been run on your code during tests.
 */
internal fun applyToCode(code: String,
                         expectedAsserts: Int,
                         applyToNode: (node: ASTNode, counter: AtomicInteger) -> Unit) {
    val counter = AtomicInteger(0)
    KtLint.lint(
            KtLint.Params(
                    text = code,
                    ruleSets = listOf(
                            RuleSet("test", object : Rule("astnode-utils-test") {
                                override fun visit(node: ASTNode,
                                                   autoCorrect: Boolean,
                                                   emit: (offset: Int, errorMessage: String, canBeAutoCorrected: Boolean) -> Unit) {
                                    applyToNode(node, counter)
                                }
                            })
                    ),
                    cb = { _, _ -> Unit }
            )
    )
    Assertions.assertThat(counter.get()).`as`("Number of expected asserts").isEqualTo(expectedAsserts)
}
