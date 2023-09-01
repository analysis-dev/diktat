package com.saveourtool.diktat.ruleset.rules.chapter3

import com.saveourtool.diktat.common.config.rules.RulesConfig
import com.saveourtool.diktat.ruleset.constants.Warnings.PREVIEW_ANNOTATION
import com.saveourtool.diktat.ruleset.rules.DiktatRule
import com.saveourtool.diktat.ruleset.utils.getAllChildrenWithType
import com.saveourtool.diktat.ruleset.utils.getIdentifierName

import org.jetbrains.kotlin.KtNodeTypes.ANNOTATION_ENTRY
import org.jetbrains.kotlin.KtNodeTypes.FUN
import org.jetbrains.kotlin.KtNodeTypes.MODIFIER_LIST
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.psiUtil.isPrivate

/**
 * This rule checks, whether the method has `@Preview` annotation (Jetpack Compose)
 * If so, such method should be private and function name should end with `Preview` suffix
 */
class PreviewAnnotationRule(configRules: List<RulesConfig>) : DiktatRule(
    NAME_ID,
    configRules,
    listOf(PREVIEW_ANNOTATION)
) {
    override fun logic(node: ASTNode) {
        if (node.elementType == FUN) {
            checkFunctionSignature(node)
        }
    }

    private fun checkFunctionSignature(node: ASTNode) {
        node.findChildByType(MODIFIER_LIST)?.let { modList ->
            doCheck(node, modList)
        }
    }

    private fun doCheck(functionNode: ASTNode, modeList: ASTNode) {
        if (modeList.getAllChildrenWithType(ANNOTATION_ENTRY).isEmpty()) {
            return
        }

        val previewAnnotationNode = modeList.getAllChildrenWithType(ANNOTATION_ENTRY).firstOrNull {
            it.text.contains("$ANNOTATION_SYMBOL$PREVIEW_ANNOTATION_TEXT")
        }

        previewAnnotationNode?.let {
            val functionName = functionNode.getIdentifierName()?.text ?: return

            // warn if function is not private
            if (!((functionNode.psi as KtNamedFunction).isPrivate())) {
                PREVIEW_ANNOTATION.warnAndFix(
                    configRules,
                    emitWarn,
                    isFixMode,
                    "$functionName method should be private",
                    functionNode.startOffset,
                    functionNode
                ) {
                    // provide fix
                }
            }

            // warn if function has no `Preview` suffix
            if (!isMethodHasPreviewSuffix(functionName)) {
                PREVIEW_ANNOTATION.warnAndFix(
                    configRules,
                    emitWarn,
                    isFixMode,
                    "$functionName method should has `Preview` suffix",
                    functionNode.startOffset,
                    functionNode
                ) {
                    // provide fix
                }
            }
        }
    }

    private fun isMethodHasPreviewSuffix(functionName: String) =
        functionName.contains(PREVIEW_ANNOTATION_TEXT)

    companion object {
        const val ANNOTATION_SYMBOL = "@"
        const val NAME_ID = "preview-annotation"
        const val PREVIEW_ANNOTATION_TEXT = "Preview"
    }
}
