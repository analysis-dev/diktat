package org.cqfn.diktat.ruleset.rules.files

import org.cqfn.diktat.common.config.rules.RuleConfiguration
import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getRuleConfig
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings.FILE_IS_TOO_LONG
import org.cqfn.diktat.ruleset.utils.getFileName
import org.cqfn.diktat.ruleset.utils.splitPathToDirs

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.slf4j.LoggerFactory

/**
 * Rule that checks number of lines in a file
 */
class FileSize(private val configRules: List<RulesConfig>) : Rule("file-size") {
    private var isFixMode: Boolean = false
    private var fileName: String? = null
    private lateinit var emitWarn: EmitType

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: EmitType) {
        emitWarn = emit
        isFixMode = autoCorrect
        if (node.elementType == ElementType.FILE) {
            fileName = node.getFileName()
            val configuration = FileSizeConfiguration(
                this.configRules.getRuleConfig(FILE_IS_TOO_LONG)?.configuration ?: mapOf()
            )
            val ignoreFolders = configuration.ignoreFolders

            val realFilePath = calculateFilePath(fileName)

            if (!realFilePath.contains(SRC_PATH)) {
                log.error("$SRC_PATH directory is not found in file path")
            } else {
                if (ignoreFolders.none { realFilePath.containsAll(it.splitPathToDirs()) }) {
                    checkFileSize(node, configuration.maxSize)
                }
            }
            return
        }
    }

    private fun calculateFilePath(fileName: String?): List<String> = fileName?.splitPathToDirs()
        ?: run {
            log.error("Could not find absolute path to file")
            listOf()
        }

    private fun checkFileSize(node: ASTNode, maxSize: Long) {
        val size = node
            .text
            .split("\n")
            .size
        if (size > maxSize) {
            FILE_IS_TOO_LONG.warn(configRules, emitWarn, isFixMode, size.toString(), node.startOffset, node)
        }
    }

    /**
     * [RuleConfiguration] for maximun number of lines in a file
     */
    class FileSizeConfiguration(config: Map<String, String>) : RuleConfiguration(config) {
        /**
         * Maximum allowed number of lines in a file
         */
        val maxSize = config["maxSize"]?.toLongOrNull() ?: MAX_SIZE

        /**
         * List of folders, files from which are ignored during the check. For example, for tests.
         */
        val ignoreFolders = config["ignoreFolders"]?.replace("\\s+".toRegex(), "")?.split(IGNORE_FOLDERS_SEPARATOR) ?: ignoreFolder
    }

    companion object {
        private val log = LoggerFactory.getLogger(FileSize::class.java)
        const val IGNORE_FOLDERS_SEPARATOR = ","
        const val MAX_SIZE = 2000L
        const val SRC_PATH = "src"
        private val ignoreFolder: List<String> = emptyList()
    }
}
