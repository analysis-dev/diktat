/**
 * The file contains main method
 */

package com.saveourtool.diktat

import com.saveourtool.diktat.api.DiktatProcessorListener
import com.saveourtool.diktat.cli.DiktatMode
import com.saveourtool.diktat.cli.DiktatProperties

import io.github.oshai.kotlinlogging.KotlinLogging

import java.nio.file.Path
import java.nio.file.Paths

import kotlin.io.path.absolutePathString

private val log = KotlinLogging.logger { }

private val loggingListener = object : DiktatProcessorListener {
    override fun before(file: Path) {
        log.debug {
            "Start processing the file: $file"
        }
    }
}

fun main(args: Array<String>) {
    val properties = DiktatProperties.parse(diktatReporterFactory, args)
    properties.configureLogger()

    log.debug {
        "Loading diktatRuleSet using config ${properties.config}"
    }
    val currentFolder = Paths.get(".").toAbsolutePath().normalize()
    val diktatRunnerArguments = properties.toRunnerArguments(
        sourceRootDir = currentFolder,
        loggingListener = loggingListener,
    )

    when (properties.mode) {
        DiktatMode.CHECK -> DiktatRunner.checkAll(diktatRunnerArguments)
        DiktatMode.FIX -> DiktatRunner.fixAll(diktatRunnerArguments) { updatedFile ->
            log.warn {
                "Original and formatted content differ, writing to ${updatedFile.absolutePathString()}..."
            }
        }
    }
}
