import com.saveourtool.diktat.buildutils.configurePom
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentOperatingSystem
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.saveourtool.diktat.buildutils.kotlin-jvm-configuration")
    id("com.saveourtool.diktat.buildutils.code-quality-convention")
    id("com.saveourtool.diktat.buildutils.publishing-configuration")
    id("pl.droidsonroids.jacoco.testkit") version "1.0.12"
    id("org.gradle.test-retry") version "1.5.6"
    id("com.gradle.plugin-publish") version "1.2.1"
}

dependencies {
    implementation(kotlin("gradle-plugin-api"))

    implementation(projects.diktatRules)
    implementation(projects.diktatKtlintEngine)
    // merge sarif reports
    implementation(libs.sarif4k.jvm)
    implementation(libs.kotlinx.serialization.json)
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testImplementation(libs.ktlint.cli.reporter.core)
    testImplementation(libs.ktlint.cli.reporter.json)
    testImplementation(libs.ktlint.cli.reporter.plain)
    testImplementation(libs.ktlint.cli.reporter.sarif)
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        // kotlin 1.4 api is the latest support version in kotlin 1.9
        // min supported Gradle is 7.0
        languageVersion = "1.4"
        apiVersion = "1.4"
        jvmTarget = "1.8"
        freeCompilerArgs = freeCompilerArgs - "-Werror"
    }
}

gradlePlugin {
    website = "https://diktat.saveourtool.com/"
    vcsUrl = "https://github.com/saveourtool/diktat"
    plugins {
        create("diktatPlugin") {
            id = "com.saveourtool.diktat"
            implementationClass = "com.saveourtool.diktat.plugin.gradle.DiktatGradlePlugin"
        }
    }
}

afterEvaluate {
    publishing {
        publications {
            withType<MavenPublication> {
                pom {
                    configurePom(project)
                }
            }
        }
    }
}

// === testing & code coverage, jacoco is run independent from maven
val functionalTestTask by tasks.register<Test>("functionalTest")
tasks.withType<Test> {
    useJUnitPlatform()
}

// === integration testing
// fixme: should probably use KotlinSourceSet instead
val functionalTest: SourceSet = sourceSets.create("functionalTest") {
    compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath.get()
    runtimeClasspath += output + compileClasspath
}

@Suppress("GENERIC_VARIABLE_WRONG_DECLARATION", "MAGIC_NUMBER")
val functionalTestProvider: TaskProvider<Test> = tasks.named<Test>("functionalTest") {
    shouldRunAfter("test")
    testClassesDirs = functionalTest.output.classesDirs
    classpath = functionalTest.runtimeClasspath
    maxParallelForks = Runtime.getRuntime().availableProcessors()
    maxHeapSize = "1024m"
    retry {
        failOnPassedAfterRetry.set(false)
        maxFailures.set(10)
        maxRetries.set(3)
    }
    doLast {
        if (getCurrentOperatingSystem().isWindows) {
            // workaround for https://github.com/koral--/jacoco-gradle-testkit-plugin/issues/9
            logger.lifecycle("Sleeping for 5 sec after functionalTest to avoid error with file locking")
            Thread.sleep(5_000)
        }
    }
    finalizedBy(tasks.jacocoTestReport)
}
tasks.check { dependsOn(tasks.jacocoTestReport) }

jacocoTestKit {
    @Suppress("UNCHECKED_CAST")
    applyTo("functionalTestRuntimeOnly", functionalTestProvider as TaskProvider<Task>)
}
tasks.jacocoTestReport {
    shouldRunAfter(tasks.withType<Test>())
    executionData(
        layout.buildDirectory
            .dir("jacoco")
            .map { jacocoDir ->
                jacocoDir.asFileTree
                    .matching {
                        include("*.exec")
                    }
            }
    )
    reports {
        // xml report is used by codecov
        xml.required.set(true)
    }
}
