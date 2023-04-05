/**
 * This file contains util methods for __KtLint__
 */

package org.cqfn.diktat.ktlint

import org.cqfn.diktat.api.DiktatCallback
import org.cqfn.diktat.api.DiktatRuleSet
import org.cqfn.diktat.common.config.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ktlint.DiktatErrorImpl.Companion.unwrap
import org.cqfn.diktat.ktlint.DiktatErrorImpl.Companion.wrap
import org.cqfn.diktat.ktlint.KtLintRuleSetWrapper.Companion.toKtLint
import com.pinterest.ktlint.core.KtLint
import com.pinterest.ktlint.core.KtLint.ExperimentalParams
import com.pinterest.ktlint.core.LintError
import mu.KotlinLogging
import org.intellij.lang.annotations.Language
import java.nio.file.Path
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.relativeTo

private val log = KotlinLogging.logger { }

val defaultCallback = DiktatCallback { error, _ ->
    log.warn { "Received linting error: $error" }
}

typealias LintErrorCallback = (LintError, Boolean) -> Unit

/**
 * Makes sure this _rule id_ is qualified with a _rule set id_.
 *
 * @param ruleSetId the _rule set id_; defaults to [DIKTAT_RULE_SET_ID].
 * @return the fully-qualified _rule id_ in the form of `ruleSetId:ruleId`.
 * @see DIKTAT_RULE_SET_ID
 * @since 1.2.4
 */
fun String.qualifiedWithRuleSetId(ruleSetId: String = DIKTAT_RULE_SET_ID): String =
    when {
        this.contains(':') -> this
        else -> "$ruleSetId:$this"
    }

/**
 * @param sourceRootDir
 * @return relative path to [sourceRootDir] as [String]
 */
fun Path.relativePathStringTo(sourceRootDir: Path): String = relativeTo(sourceRootDir).invariantSeparatorsPathString

/**
 * @return [DiktatCallback] from KtLint [LintErrorCallback]
 */
fun LintErrorCallback.wrap(): DiktatCallback = DiktatCallback { error, isCorrected ->
    this(error.unwrap(), isCorrected)
}

/**
 * @return KtLint [LintErrorCallback] from [DiktatCallback] or exception
 */
fun DiktatCallback.unwrap(): LintErrorCallback = { error, isCorrected ->
    this(error.wrap(), isCorrected)
}

/**
 * Enables ignoring autocorrected errors when in "fix" mode (i.e. when
 * [KtLint.format] is invoked).
 *
 * Before version 0.47, _Ktlint_ only reported non-corrected errors in "fix"
 * mode.
 * Now, this has changed.
 *
 * @receiver the instance of _Ktlint_ parameters.
 * @return the instance with the [callback][ExperimentalParams.cb] modified in
 *   such a way that it ignores corrected errors.
 * @see KtLint.format
 * @see ExperimentalParams.cb
 * @since 1.2.4
 */
private fun ExperimentalParams.ignoreCorrectedErrors(): ExperimentalParams =
    copy(cb = { error: LintError, corrected: Boolean ->
        if (!corrected) {
            cb(error, false)
        }
    })

/**
 * @param ruleSetSupplier
 * @param text
 * @param fileName
 * @param cb callback to be called on unhandled [LintError]s
 * @return formatted code
 */
@Suppress("LAMBDA_IS_NOT_LAST_PARAMETER")
fun format(
    ruleSetSupplier: () -> DiktatRuleSet,
    @Language("kotlin") text: String,
    fileName: String,
    cb: DiktatCallback = defaultCallback
): String {
    val ruleSets = listOf(ruleSetSupplier().toKtLint())
    return KtLint.format(
        ExperimentalParams(
            text = text,
            ruleSets = ruleSets,
            fileName = fileName.removeSuffix("_copy"),
            script = fileName.removeSuffix("_copy").endsWith("kts"),
            cb = cb.unwrap(),
            debug = true,
        ).ignoreCorrectedErrors()
    )
}

/**
 * @param ruleSetSupplier
 * @param text
 * @param fileName
 * @param cb callback to be called on unhandled [LintError]s
 * @return formatted code
 */
@Suppress("LAMBDA_IS_NOT_LAST_PARAMETER")
fun lint(
    ruleSetSupplier: () -> DiktatRuleSet,
    @Language("kotlin") text: String,
    fileName: String = "test.ks",
    cb: DiktatCallback = DiktatCallback.empty
) {
    val ruleSets = listOf(ruleSetSupplier().toKtLint())
    KtLint.lint(
        ExperimentalParams(
            text = text,
            ruleSets = ruleSets,
            fileName = fileName.removeSuffix("_copy"),
            script = fileName.removeSuffix("_copy").endsWith("kts"),
            cb = cb.unwrap(),
            debug = true,
        ).ignoreCorrectedErrors()
    )
}
