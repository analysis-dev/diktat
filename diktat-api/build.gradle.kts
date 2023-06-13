plugins {
    id("com.saveourtool.diktat.buildutils.kotlin-jvm-configuration")
    id("com.saveourtool.diktat.buildutils.code-quality-convention")
    id("com.saveourtool.diktat.buildutils.publishing-signing-default-configuration")
}

project.description = "This module builds diktat-api"

dependencies {
    implementation(libs.kotlin.compiler.embeddable)
}

val generateDiktatVersionFile by tasks.registering {
    val outputDir = File("$buildDir/generated/src")
    val versionsFile = outputDir.resolve("generated/DiktatVersion.kt")

    val diktatVersion = version.toString()

    inputs.property("diktat version", diktatVersion)
    outputs.dir(outputDir)

    doFirst {
        versionsFile.parentFile.mkdirs()
        versionsFile.writeText(
            """
            package generated

            const val DIKTAT_VERSION = "$diktatVersion"

            """.trimIndent()
        )
    }
}

kotlin.sourceSets.getByName("main") {
    kotlin.srcDir(
        generateDiktatVersionFile.map {
            it.outputs.files.singleFile
        }
    )
}
