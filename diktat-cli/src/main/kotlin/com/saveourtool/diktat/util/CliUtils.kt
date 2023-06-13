/**
 * This class contains util methods to operate with java.nio.file.Path for CLI
 */

package com.saveourtool.diktat.util

import java.io.File
import java.nio.file.FileSystem
import java.nio.file.FileSystems
import java.nio.file.InvalidPathException
import java.nio.file.Path
import java.nio.file.PathMatcher
import java.nio.file.Paths
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.PathWalkOption
import kotlin.io.path.absolutePathString
import kotlin.io.path.exists
import kotlin.io.path.walk

// all roots
private val roots: Set<String> = FileSystems.getDefault()
    .rootDirectories
    .asSequence()
    .map { it.absolutePathString() }
    .toSet()

/**
 * Create a matcher and return a filter that uses it.
 *
 * @param glob glob pattern to filter files
 * @return a sequence of files which matches to [glob]
 */
@OptIn(ExperimentalPathApi::class)
fun Path.walkByGlob(glob: String): Sequence<Path> = fileSystem.globMatcher(glob)
    .let { matcher ->
        this.walk(PathWalkOption.INCLUDE_DIRECTORIES)
            .filter { matcher.matches(it) }
    }

/**
 * @return path or null if path is invalid or doesn't exist
 */
fun String.tryToPathIfExists(): Path? = try {
    Paths.get(this).takeIf { it.exists() }
} catch (e: InvalidPathException) {
    null
}

private fun FileSystem.globMatcher(glob: String): PathMatcher = if (isAbsoluteGlob(glob)) {
    getPathMatcher("glob:${glob.toUnixSeparator()}")
} else {
    getPathMatcher("glob:**/${glob.toUnixSeparator()}")
}

private fun String.toUnixSeparator(): String = replace(File.separatorChar, '/')

private fun isAbsoluteGlob(glob: String): Boolean = glob.startsWith("**") || roots.any { glob.startsWith(it, true) }
