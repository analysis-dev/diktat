package org.cqfn.diktat.plugin.gradle

import com.pinterest.ktlint.core.Reporter
import org.gradle.api.file.FileCollection

open class DiktatExtension {
    /**
     * Paths that will be scanned for .kt(s) files
     */
    lateinit var inputs: FileCollection

    /**
     * Flag that indicates whether to turn debug logging on
     */
    var debug = false

    /**
     * Ktlint's [Reporter] which will be used during run.
     * Private until I find a way to configure it.
     */
    internal lateinit var reporter: Reporter

    /**
     * Path to diktat yml config file. Can be either absolute or relative to project's root directory.
     * Private until gradle supports kotlin 1.4 and we can pass this value to DiktatRuleSetProvider
     */
    internal var diktatConfigFile: String = "diktat-analysis.yml"
}
