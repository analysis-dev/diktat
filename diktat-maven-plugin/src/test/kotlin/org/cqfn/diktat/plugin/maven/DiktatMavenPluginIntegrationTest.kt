package org.cqfn.diktat.plugin.maven

import com.soebes.itf.jupiter.extension.MavenGoal
import com.soebes.itf.jupiter.extension.MavenJupiterExtension
import com.soebes.itf.jupiter.extension.MavenTest
import com.soebes.itf.jupiter.extension.SystemProperty
import com.soebes.itf.jupiter.maven.MavenExecutionResult
import org.junit.jupiter.api.Assertions
import java.io.File
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.readText

/**
 * Integration tests for diktat-maven-plugin. Run against the project from diktat-examples.
 * The whole pipeline is as follows:
 * * For each test case, test data is copied from examples with respect to maven-itf requirements, .mvn/jvm.config is copied too
 *   Note: for maven itf test name should equal example project's directory name, which we have in pom.xml.
 * * maven-failsafe-plugin launches tests; for each test case a separate maven process is spawned
 * * maven execution results are analyzed here; .mvn/jvm.config is used to attach jacoco java agent to every maven process and generate individual execution reports
 */
@OptIn(ExperimentalPathApi::class)
@MavenJupiterExtension
@SystemProperty(value = "diktat.version", content = "0.2.1-SNAPSHOT")  // todo set pluin version for example project properly
class DiktatMavenPluginIntegrationTest {
    @MavenTest
    @MavenGoal("diktat:check@diktat")
    fun diktatCheck(result: MavenExecutionResult) {
        Assertions.assertEquals(1, result.returnCode)
        Assertions.assertFalse(result.isError)
        Assertions.assertFalse(result.isSuccesful)
        Assertions.assertTrue(result.isFailure)

        val mavenLog = result.mavenLog.stdout.readText()
        Assertions.assertTrue(
            mavenLog.contains("[HEADER_MISSING_OR_WRONG_COPYRIGHT]")
        )

        File(result.mavenProjectResult.baseDir, "target/jacoco-it.exec").copyTo(
            File("target/jacoco-it-1.exec")
        )
    }

    @MavenTest
    @MavenGoal("diktat:fix@diktat")
    fun diktatFix(result: MavenExecutionResult) {
        Assertions.assertEquals(1, result.returnCode)
        Assertions.assertFalse(result.isError)
        Assertions.assertFalse(result.isSuccesful)
        Assertions.assertTrue(result.isFailure)

        val mavenLog = result.mavenLog.stdout.readText()
        Assertions.assertTrue(
            mavenLog.contains("Original and formatted content differ, writing to Test.kt...")
        )
        Assertions.assertTrue(
            mavenLog.contains(Regex("There are \\d+ lint errors"))
        )
        Assertions.assertTrue(
            mavenLog.contains("[MISSING_KDOC_TOP_LEVEL]")
        )

        File(result.mavenProjectResult.baseDir, "target/jacoco-it.exec").copyTo(
            File("target/jacoco-it-2.exec")
        )
    }
}
