package org.cqfn.diktat.test.framework.processing

import com.github.difflib.DiffUtils
import com.github.difflib.patch.ChangeDelta
import com.github.difflib.text.DiffRowGenerator
import org.slf4j.LoggerFactory

import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.util.ArrayList
import java.util.stream.Collectors

/**
 * A class that is capable of comparing files content
 */
class FileComparator {
    private val expectedResultFile: File
    private val actualResultList: List<String?>
    private val diffGenerator = DiffRowGenerator.create()
        .showInlineDiffs(true)
        .mergeOriginalRevised(true)
        .inlineDiffByWord(false)
        .oldTag { start -> if (start) "[" else "]" }
        .newTag { start -> if (start) "<" else ">" }
        .build()

    constructor(expectedResultFile: File, actualResultList: List<String?>) {
        this.expectedResultFile = expectedResultFile
        this.actualResultList = actualResultList
    }

    constructor(expectedResultFile: File, actualResultFile: File) {
        this.expectedResultFile = expectedResultFile
        this.actualResultList = readFile(actualResultFile.absolutePath)
    }

    /**
     * @return true in case files are different
     * false - in case they are equals
     */
    @Suppress("ReturnCount", "FUNCTION_BOOLEAN_PREFIX")
    fun compareFilesEqual(): Boolean {
        try {
            val expect = readFile(expectedResultFile.absolutePath)
            if (expect.isEmpty()) {
                return false
            }
            val patch = DiffUtils.diff(expect, actualResultList)
            if (patch.deltas.isEmpty()) {
                return true
            }
            val joinedDeltas = patch.deltas.joinToString(System.lineSeparator()) { delta ->
                when (delta) {
                    is ChangeDelta -> diffGenerator
                        .generateDiffRows(delta.source.lines, delta.target.lines)
                        .joinToString(System.lineSeparator()) { it.oldLine }
                        .let { "[ChangeDelta, position ${delta.source.position}, lines: [$it]]" }
                    else -> delta.toString()
                }
            }

            log.error("""
                |Expected result from <${expectedResultFile.name}> and actual formatted are different.
                |See difference below (for ChangeDelta [text] indicates removed text, <text> - inserted):
                |$joinedDeltas
                """.trimMargin()
            )
        } catch (e: RuntimeException) {
            log.error("Not able to prepare diffs between <${expectedResultFile.name}> and <$actualResultList>", e)
        }
        return false
    }

    /**
     * @param fileName - file where to write these list to, separated with newlines
     * @return a list of lines from the file
     */
    fun readFile(fileName: String): List<String> {
        var list: List<String> = ArrayList()
        try {
            Files.newBufferedReader(Paths.get(fileName)).use { list = it.lines().collect(Collectors.toList()) }
        } catch (e: IOException) {
            log.error("Not able to read file: $fileName")
        }
        return list
    }

    companion object {
        private val log = LoggerFactory.getLogger(FileComparator::class.java)
    }
}
