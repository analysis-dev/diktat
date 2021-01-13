import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.gradle.nativeplatform.platform.internal.DefaultNativePlatform.getCurrentOperatingSystem

plugins {
    `java-gradle-plugin`
    kotlin("jvm") version "1.4.21"
    jacoco
    id("pl.droidsonroids.jacoco.testkit") version "1.0.7"
}

repositories {
    flatDir {
        // to use snapshot diktat without necessary installing
        dirs("../diktat-rules/target")
    }
    mavenLocal()  // to use snapshot diktat
    mavenCentral()
    jcenter()
}

// default value is needed for correct gradle loading in IDEA; actual value from maven is used during build
val ktlintVersion = project.properties.getOrDefault("ktlintVersion", "0.39.0") as String
val diktatVersion = project.version.takeIf { it.toString() != Project.DEFAULT_VERSION } ?: "0.3.0"
val junitVersion = project.properties.getOrDefault("junitVersion", "5.7.0") as String
val jacocoVersion = project.properties.getOrDefault("jacocoVersion", "0.8.6") as String
dependencies {
    implementation(kotlin("gradle-plugin-api"))

    implementation("com.pinterest.ktlint:ktlint-core:$ktlintVersion") {
        exclude("com.pinterest.ktlint", "ktlint-ruleset-standard")
    }
    implementation("com.pinterest.ktlint:ktlint-reporter-plain:$ktlintVersion")
    implementation("org.cqfn.diktat:diktat-rules:$diktatVersion")

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

val generateVersionsFile by tasks.registering {
    val versionsFile = File("$buildDir/generated/src/generated/Versions.kt")

    outputs.file(versionsFile)

    doFirst {
        versionsFile.parentFile.mkdirs()
        versionsFile.writeText(
            """
            package generated

            internal const val DIKTAT_VERSION = "$diktatVersion"
            internal const val KTLINT_VERSION = "$ktlintVersion"

            """.trimIndent()
        )
    }
}
sourceSets.main.get().java.srcDir("$buildDir/generated/src")

tasks.withType<KotlinCompile> {
    kotlinOptions {
        // fixme: kotlin 1.3 is required for gradle <6.8
        languageVersion = "1.3"
        apiVersion = "1.3"
        jvmTarget = "1.8"
    }

    dependsOn.add(generateVersionsFile)
}

gradlePlugin {
    plugins {
        create("diktatPlugin") {
            id = "org.cqfn.diktat.diktat-gradle-plugin"
            implementationClass = "org.cqfn.diktat.plugin.gradle.DiktatGradlePlugin"
        }
    }
}

java {
    withSourcesJar()
}

// === testing & code coverage, jacoco is run independent from maven
val functionalTestTask by tasks.register<Test>("functionalTest")
val jacocoMergeTask by tasks.register<JacocoMerge>("jacocoMerge")
tasks.withType<Test> {
    useJUnitPlatform()
}
jacoco.toolVersion = jacocoVersion

// === integration testing
// fixme: should probably use KotlinSourceSet instead
val functionalTest = sourceSets.create("functionalTest") {
    compileClasspath += sourceSets.main.get().output + configurations.testRuntimeClasspath
    runtimeClasspath += output + compileClasspath
}
tasks.getByName<Test>("functionalTest") {
    dependsOn("test")
    testClassesDirs = functionalTest.output.classesDirs
    classpath = functionalTest.runtimeClasspath
    maxParallelForks = Runtime.getRuntime().availableProcessors()
    doLast {
        if (getCurrentOperatingSystem().isWindows) {
            // workaround for https://github.com/koral--/jacoco-gradle-testkit-plugin/issues/9
            logger.lifecycle("Sleeping for 5 sec after functionalTest to avoid error with file locking")
            Thread.sleep(5_000)
        }
    }
    finalizedBy(jacocoMergeTask)
}
tasks.check { dependsOn(tasks.jacocoTestReport) }
jacocoTestKit {
    applyTo("functionalTestRuntimeOnly", tasks.named("functionalTest"))
}
tasks.getByName("jacocoMerge", JacocoMerge::class) {
    dependsOn(functionalTestTask)
    executionData(
        fileTree("$buildDir/jacoco").apply {
            include("*.exec")
        }
    )
}
tasks.jacocoTestReport {
    dependsOn(jacocoMergeTask)
    executionData("$buildDir/jacoco/jacocoMerge.exec")
    reports {
        // xml report is used by codecov
        xml.isEnabled = true
    }
}
