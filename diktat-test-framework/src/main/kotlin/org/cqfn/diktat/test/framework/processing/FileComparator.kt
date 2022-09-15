package org.cqfn.diktat.test.framework.processing

import io.github.petertrr.diffutils.diff
import io.github.petertrr.diffutils.patch.ChangeDelta
import io.github.petertrr.diffutils.text.DiffRowGenerator
import org.slf4j.LoggerFactory

import java.io.File
import java.io.IOException
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.name
import kotlin.io.path.readLines

/**
 * A class that is capable of comparing files content
 */
class FileComparator(
    private val expectedResultFile: Path,
    private val expectedResultList: List<String> = readFile(expectedResultFile),
    private val actualResultFile: Path,
    private val actualResultList: List<String> = readFile(actualResultFile)
) {
    private val diffGenerator = DiffRowGenerator(
        columnWidth = Int.MAX_VALUE,
        showInlineDiffs = true,
        mergeOriginalRevised = false,
        inlineDiffByWord = false,
        oldTag = { _, start -> if (start) "[" else "]" },
        newTag = { _, start -> if (start) "<" else ">" },
    )

    constructor(
        expectedResultFile: File,
        actualResultList: List<String>
    ) : this(
        expectedResultFile.toPath(),
        actualResultFile = Path("No file name.kt"),
        actualResultList = actualResultList
    )

    constructor(
        expectedResultFile: File,
        actualResultFile: File
    ) : this(
        expectedResultFile.toPath(),
        actualResultFile = actualResultFile.toPath())

    /**
     * @return true in case files are different
     * false - in case they are equals
     */
    @Suppress(
        "ReturnCount",
        "FUNCTION_BOOLEAN_PREFIX",
        "TOO_LONG_FUNCTION"
    )
    fun compareFilesEqual(): Boolean {
        try {
            if (expectedResultList.isEmpty()) {
                return false
            }
            val regex = (".*// ;warn:?(.*):(\\d*): (.+)").toRegex()
            val expectWithoutWarn = expectedResultList.filterNot { line ->
                line.contains(regex)
            }
            val patch = diff(expectWithoutWarn, actualResultList)

            if (patch.deltas.isEmpty()) {
                return true
            }
            val joinedDeltas = patch.deltas.joinToString(System.lineSeparator()) { delta ->
                when (delta) {
                    is ChangeDelta -> diffGenerator
                        .generateDiffRows(delta.source.lines, delta.target.lines)
                        .joinToString(prefix = "ChangeDelta, position ${delta.source.position}, lines:\n", separator = "\n\n") {
                            """-${it.oldLine}
                      |+${it.newLine}
                      |""".trimMargin()
                        }
                        .let { "ChangeDelta, position ${delta.source.position}, lines:\n$it" }
                    else -> delta.toString()
                }
            }

            log.error("""
                |Expected result from <${expectedResultFile.name}> and <${actualResultFile.name}> formatted are different.
                |See difference below:
                |$joinedDeltas
                """.trimMargin()
            )
        } catch (e: RuntimeException) {
            log.error("Not able to prepare diffs between <${expectedResultFile.name}> and <${actualResultFile.name}>", e)
        }
        return false
    }

    companion object {
        private val log = LoggerFactory.getLogger(FileComparator::class.java)

        /**
         * @param file file where to write these list to, separated with newlines.
         * @return a list of lines from the file, or an empty list if an I/O error
         *   has occurred.
         */
        private fun readFile(file: Path): List<String> =
            try {
                file.readLines()
            } catch (e: IOException) {
                log.error("Not able to read file: $file")
                emptyList()
            }
    }
}
