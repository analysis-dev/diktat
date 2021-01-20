package org.cqfn.diktat.plugin.gradle

import org.cqfn.diktat.ruleset.rules.DIKTAT_CONF_PROPERTY
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.io.File
import java.nio.file.Files

class DiktatJavaExecTaskTest {
    private val projectBuilder = ProjectBuilder.builder()
    private lateinit var project: Project

    @BeforeEach
    fun setUp() {
        project = projectBuilder
            .withName("testProject")
            .build()
    }

    @Test
    fun `check command line for various inputs`() {
        assertCommandLineEquals(
            listOf(null, combinePathParts("src", "**", "*.kt"))
        ) {
            inputs = project.files("src/**/*.kt")
        }
    }

    @Test
    fun `check command line in debug mode`() {
        assertCommandLineEquals(
            listOf(null, "--debug", combinePathParts("src", "**", "*.kt"))
        ) {
            inputs = project.files("src/**/*.kt")
            debug = true
        }
    }

    @Test
    fun `check command line with excludes`() {
        assertCommandLineEquals(
            listOf(null, combinePathParts("src", "**", "*.kt"),
                "!${combinePathParts("src", "main", "kotlin", "generated")}"
            )
        ) {
            inputs = project.files("src/**/*.kt")
            excludes = project.files("src/main/kotlin/generated")
        }
    }

    @Test
    fun `check command line with non-existent inputs`() {
        val task = project.registerDiktatTask {
            inputs = project.files()
        }
        Assertions.assertFalse(task.shouldRun)
    }

    @Test
    fun `check system property with default config`() {
        val task = project.registerDiktatTask {
            inputs = project.files()
        }
        Assertions.assertEquals(File(project.projectDir, "diktat-analysis.yml").absolutePath, task.systemProperties[DIKTAT_CONF_PROPERTY])
    }

    @Test
    fun `check system property with custom config`() {
        val task = project.registerDiktatTask {
            inputs = project.files()
            diktatConfigFile = project.file("../diktat-analysis.yml")
        }
        Assertions.assertEquals(File(project.projectDir.parentFile, "diktat-analysis.yml").absolutePath, task.systemProperties[DIKTAT_CONF_PROPERTY])
    }

    @Test
    fun `check system property with multiproject build with default config`() {
        setupMultiProject()
        val subproject = project.subprojects.first()
        project.allprojects {
            it.registerDiktatTask {
                inputs = it.files()
            }
        }
        val task = project.tasks.getByName(DIKTAT_CHECK_TASK) as DiktatJavaExecTaskBase
        val subprojectTask = subproject.tasks.getByName(DIKTAT_CHECK_TASK) as DiktatJavaExecTaskBase
        Assertions.assertEquals(File(project.projectDir, "diktat-analysis.yml").absolutePath, task.systemProperties[DIKTAT_CONF_PROPERTY])
        Assertions.assertEquals(File(project.projectDir, "diktat-analysis.yml").absolutePath, subprojectTask.systemProperties[DIKTAT_CONF_PROPERTY])
    }

    @Test
    fun `check system property with multiproject build with custom config`() {
        setupMultiProject()
        val subproject = project.subprojects.first()
        project.allprojects {
            it.registerDiktatTask {
                inputs = it.files()
                diktatConfigFile = it.rootProject.file("diktat-analysis.yml")
            }
        }
        val task = project.tasks.getByName(DIKTAT_CHECK_TASK) as DiktatJavaExecTaskBase
        val subprojectTask = subproject.tasks.getByName(DIKTAT_CHECK_TASK) as DiktatJavaExecTaskBase
        Assertions.assertEquals(File(project.projectDir, "diktat-analysis.yml").absolutePath, task.systemProperties[DIKTAT_CONF_PROPERTY])
        Assertions.assertEquals(File(project.projectDir, "diktat-analysis.yml").absolutePath, subprojectTask.systemProperties[DIKTAT_CONF_PROPERTY])
    }

    @Test
    fun `check system property with multiproject build with custom config and two config files`() {
        setupMultiProject()
        val subproject = project.subprojects.single()
        subproject.file("diktat-analysis.yml").createNewFile()
        project.allprojects {
            it.registerDiktatTask {
                inputs = it.files()
                diktatConfigFile = it.file("diktat-analysis.yml")
            }
        }
        val task = project.tasks.getByName(DIKTAT_CHECK_TASK) as DiktatJavaExecTaskBase
        val subprojectTask = subproject.tasks.getByName(DIKTAT_CHECK_TASK) as DiktatJavaExecTaskBase
        Assertions.assertEquals(File(project.projectDir, "diktat-analysis.yml").absolutePath, task.systemProperties[DIKTAT_CONF_PROPERTY])
        Assertions.assertEquals(File(subproject.projectDir, "diktat-analysis.yml").absolutePath, subprojectTask.systemProperties[DIKTAT_CONF_PROPERTY])
    }

    private fun Project.registerDiktatTask(extensionConfiguration: DiktatExtension.() -> Unit): DiktatJavaExecTaskBase {
        DiktatGradlePlugin().apply(this)
        extensions.configure("diktat", extensionConfiguration)
        return tasks.getByName(DIKTAT_CHECK_TASK) as DiktatJavaExecTaskBase
    }

    private fun assertCommandLineEquals(expected: List<String?>, extensionConfiguration: DiktatExtension.() -> Unit) {
        val task = project.registerDiktatTask(extensionConfiguration)
        Assertions.assertIterableEquals(expected, task.commandLine)
    }

    private fun setupMultiProject() {
        ProjectBuilder.builder()
            .withParent(project)
            .withName("testSubproject")
            .withProjectDir(File(project.projectDir, "testSubproject").also {
                Files.createDirectory(it.toPath())
            })
            .build()
        project.file("diktat-analysis.yml").createNewFile()
    }

    private fun combinePathParts(vararg parts: String) = parts.joinToString(File.separator)
    
    companion object {
        private const val DIKTAT_CHECK_TASK = "diktatCheck"
    }
}
