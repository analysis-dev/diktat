package org.cqfn.diktat.plugin.gradle

import org.cqfn.diktat.plugin.gradle.DiktatGradlePlugin.Companion.DIKTAT_CHECK_TASK
import org.cqfn.diktat.plugin.gradle.DiktatGradlePlugin.Companion.DIKTAT_FIX_TASK
import org.cqfn.diktat.ruleset.rules.DIKTAT_CONF_PROPERTY

import generated.DIKTAT_VERSION
import generated.KTLINT_VERSION
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.file.FileCollection
import org.gradle.api.provider.Property
import org.gradle.api.tasks.IgnoreEmptyDirectories
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.JavaExec
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.SkipWhenEmpty
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.TaskProvider
import org.gradle.api.tasks.VerificationTask
import org.gradle.api.tasks.util.PatternFilterable
import org.gradle.api.tasks.util.PatternSet
import org.gradle.util.GradleVersion

import java.io.File
import javax.inject.Inject

/**
 * A base diktat task for gradle <6.8, which wraps [JavaExec].
 *
 * Note: class being `open` is required for gradle to create a task.
 */
open class DiktatJavaExecTaskBase @Inject constructor(
    gradleVersionString: String,
    diktatExtension: DiktatExtension,
    diktatConfiguration: Configuration,
    private val inputs: PatternFilterable,
    additionalFlags: Iterable<String> = emptyList()
) : JavaExec(), VerificationTask {
    /**
     * A backing [Property] for [getIgnoreFailures] and [setIgnoreFailures]
     */
    @get:Internal
    internal val ignoreFailuresProp: Property<Boolean> = project.objects.property(Boolean::class.javaObjectType)

    /**
     * Whether diktat should be executed via JavaExec or not.
     */
    @get:Internal
    internal var shouldRun = true

    /**
     * Files that will be analyzed by diktat
     */
    @get:IgnoreEmptyDirectories
    @get:SkipWhenEmpty
    @get:PathSensitive(PathSensitivity.RELATIVE)
    @get:InputFiles
    val actualInputs: FileCollection by lazy {
        with(diktatExtension) {
            // validate configuration
            require(inputs == null && excludes == null) {
                "`inputs` and `excludes` arguments for diktat task are deprecated and now should be changed for `inputs {}` " +
                        "with configuration for PatternFilterable. Please check https://github.com/diktat-static-analysis/diktat/README.md for more info."
            }
        }

        if (inputs.includes.isEmpty() && inputs.excludes.isEmpty()) {
            inputs.include("src/**/*.kt")
        }
        project.objects.fileCollection().from(
            project.fileTree("${project.projectDir}").apply {
                exclude("${project.buildDir}")
            }
                .matching(inputs)
        )
    }

    init {
        group = "verification"
        if (isMainClassPropertySupported(gradleVersionString)) {
            // `main` is deprecated and replaced with `mainClass` since gradle 6.4
            mainClass.set("com.pinterest.ktlint.Main")
        } else {
            main = "com.pinterest.ktlint.Main"
        }

        classpath = diktatConfiguration
        project.logger.debug("Setting diktatCheck classpath to ${diktatConfiguration.dependencies.toSet()}")
        if (diktatExtension.debug) {
            project.logger.lifecycle("Running diktat $DIKTAT_VERSION with ktlint $KTLINT_VERSION")
        }
        ignoreFailures = diktatExtension.ignoreFailures
        isIgnoreExitValue = ignoreFailures  // ignore returned value of JavaExec started process if lint errors shouldn't fail the build
        systemProperty(DIKTAT_CONF_PROPERTY, resolveConfigFile(diktatExtension.diktatConfigFile).also {
            project.logger.info("Setting system property for diktat config to $it")
        })
        args = additionalFlags.toMutableList().apply {
            if (diktatExtension.debug) {
                add("--debug")
            }
            actualInputs.also {
                if (it.isEmpty) {
                    /*
                     If ktlint receives empty patterns, it implicitly uses &#42;&#42;/*.kt, **/*.kts instead.
                     This can lead to diktat analyzing gradle buildscripts and so on. We want to prevent it.
                     */
                    project.logger.warn("Inputs for $name do not exist, will not run diktat")
                    shouldRun = false
                }
            }
                .files
                .also { files ->
                    project.logger.info("Analyzing ${files.size} files with diktat in project ${project.name}")
                    project.logger.debug("Analyzing $files")
                }
                .forEach {
                    addInput(it)
                }

            add(createReporterFlag(diktatExtension))
        }
        project.logger.debug("Setting JavaExec args to $args")
    }

    /**
     * Function to execute diKTat
     */
    @TaskAction
    override fun exec() {
        if (shouldRun) {
            super.exec()
        } else {
            project.logger.info("Skipping diktat execution")
        }
    }

    /**
     * @param ignoreFailures whether failure in this plugin should be ignored by a build
     */
    override fun setIgnoreFailures(ignoreFailures: Boolean) = ignoreFailuresProp.set(ignoreFailures)

    /**
     * @return whether failure in this plugin should be ignored by a build
     */
    @Suppress("FUNCTION_BOOLEAN_PREFIX")
    override fun getIgnoreFailures(): Boolean = ignoreFailuresProp.getOrElse(false)

    private fun createReporterFlag(diktatExtension: DiktatExtension): String {
        val flag: StringBuilder = StringBuilder()

        // appending the flag with the reporter
        setReporter(diktatExtension, flag)

        if (diktatExtension.output.isNotEmpty()) {
            flag.append(",output=${diktatExtension.output}")
        }

        return flag.toString()
    }

    private fun setReporter(diktatExtension: DiktatExtension, flag: java.lang.StringBuilder) {
        val name = diktatExtension.reporter.trim()
        val validReporters = listOf("sarif", "plain", "json", "html")
        if (name.isEmpty() || !validReporters.contains(name)) {
            project.logger.warn("Reporter name $name was not specified or is invalid. Falling to 'plain' reporter")
            flag.append("--reporter=plain")
        } else {
            flag.append("--reporter=$name")
        }
    }

    @Suppress("MagicNumber")
    private fun isMainClassPropertySupported(gradleVersionString: String) =
            GradleVersion.version(gradleVersionString) >= GradleVersion.version("6.4")

    private fun MutableList<String>.addInput(file: File) {
        add(file.toRelativeString(project.projectDir))
    }

    private fun resolveConfigFile(file: File): String {
        if (file.toPath().startsWith(project.rootDir.toPath())) {
            // In gradle, project.files() returns File relative to project.projectDir.
            // There is no need to resolve file further if it has been passed via gradle files API.
            return file.absolutePath
        }

        // otherwise, e.g. if file is passed as java.io.File with relative path, we try to find it
        return generateSequence(project.projectDir) { it.parentFile }
            .map { it.resolve(file) }
            .run {
                firstOrNull { it.exists() } ?: first()
            }
            .absolutePath
    }
}

/**
 * @param diktatExtension [DiktatExtension] with some values for task configuration
 * @param diktatConfiguration dependencies of diktat run
 * @param patternSet [PatternSet] to discover files for diktat check
 * @return a [TaskProvider]
 */
fun Project.registerDiktatCheckTask(diktatExtension: DiktatExtension,
                                    diktatConfiguration: Configuration,
                                    patternSet: PatternSet
): TaskProvider<DiktatJavaExecTaskBase> =
        tasks.register(
            DIKTAT_CHECK_TASK, DiktatJavaExecTaskBase::class.java, gradle.gradleVersion,
            diktatExtension, diktatConfiguration, patternSet
        )

/**
 * @param diktatExtension [DiktatExtension] with some values for task configuration
 * @param diktatConfiguration dependencies of diktat run
 * @param patternSet [PatternSet] to discover files for diktat fix
 * @return a [TaskProvider]
 */
fun Project.registerDiktatFixTask(diktatExtension: DiktatExtension,
                                  diktatConfiguration: Configuration,
                                  patternSet: PatternSet
): TaskProvider<DiktatJavaExecTaskBase> =
        tasks.register(
            DIKTAT_FIX_TASK, DiktatJavaExecTaskBase::class.java, gradle.gradleVersion,
            diktatExtension, diktatConfiguration, patternSet, listOf("-F ")
        )
