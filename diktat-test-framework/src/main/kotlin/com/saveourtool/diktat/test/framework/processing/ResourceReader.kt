package com.saveourtool.diktat.test.framework.processing

import com.saveourtool.diktat.test.framework.util.readTextOrNull
import mu.KotlinLogging
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.isRegularFile
import kotlin.io.path.toPath
import kotlin.io.path.writeText

/**
 * A base interface to read resources for testing purposes
 */
interface ResourceReader : Function1<String, Path?> {
    /**
     * @param resourceName
     * @return [Path] for provider [resourceName]
     */
    override fun invoke(resourceName: String): Path?

    companion object {
        private val log = KotlinLogging.logger {}

        /**
         * Default implementation of [ResourceReader]
         */
        val default: ResourceReader = object : ResourceReader {
            override fun invoke(resourceName: String): Path? = javaClass.classLoader.getResource(resourceName)
                ?.toURI()
                ?.toPath()
                .also {
                    if (it == null || !it.isRegularFile()) {
                        log.error { "Not able to find file for running test: $resourceName" }
                    }
                }
        }

        /**
         * @param tempDir the temporary directory (usually injected by _JUnit_).
         * @param replacements a map of replacements which will be applied to actual and expected content before comparing.
         * @return Instance of [ResourceReader] with replacements of content
         */
        fun withReplacements(
            tempDir: Path,
            replacements: Map<String, String>,
        ): ResourceReader = object : ResourceReader {
            override fun invoke(resourceName: String): Path? = default.invoke(resourceName)
                ?.let { originalFile ->
                    tempDir.resolve(resourceName)
                        .also { resultFile ->
                            originalFile.readTextOrNull()?.replaceAll(replacements)
                                ?.let {
                                    resultFile.parent.createDirectories()
                                    resultFile.writeText(it)
                                }
                        }
                }
        }

        private fun String.replaceAll(replacements: Map<String, String>): String = replacements.entries
            .fold(this) { result, replacement ->
                result.replace(replacement.key, replacement.value)
            }
    }
}
