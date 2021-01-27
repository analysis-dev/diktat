package org.cqfn.diktat.plugin.gradle

import org.gradle.api.file.FileCollection
import java.io.File

/**
 * An extension to configure diktat in build.gradle(.kts) file
 */
open class DiktatExtension {
    /**
     * Boolean flag to support `ignoreFailures` property of [VerificationTask].
     */
    var ignoreFailures: Boolean = false

    /**
     * Flag that indicates whether to turn debug logging on
     */
    var debug = false

    /**
     * Type of the reporter to use
     */
    var reporterType: String = "plain"

    /**
     * Type of output
     * Default: System.out
     */
    var output: String = ""

    /**
     * Path to diktat yml config file. Can be either absolute or relative to project's root directory.
     * Default value: `diktat-analysis.yml` in rootDir.
     */
    lateinit var diktatConfigFile: File

    /**
     * Paths that will be excluded from diktat run
     */
    lateinit var excludes: FileCollection

    /**
     * Paths that will be scanned for .kt(s) files
     */
    lateinit var inputs: FileCollection
}
