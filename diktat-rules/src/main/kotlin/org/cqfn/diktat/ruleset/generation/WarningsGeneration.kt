package org.cqfn.diktat.ruleset.generation

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File
import org.cqfn.diktat.ruleset.constants.Warnings

private val AUTO_GENERATION_COMMENT =
        """
            | This document was auto generated, please don't modify it.
            | This document contains all enum properties from Warnings.kt as Strings.
        """.trimMargin()

fun main() {
    val enumValNames = Warnings.values().map { it.name }

    val propertyList = enumValNames.map {
        PropertySpec.builder(it, String::class)
                .addModifiers(KModifier.CONST)
                .initializer("\"$it\"")
                .build()
    }

    val fileBody = TypeSpec
            .objectBuilder("WarningNames")
            .addProperties(propertyList)
            .build()

    val kotlinFile = FileSpec
            .builder("generated", "WarningNames")
            .addType(fileBody)
            .indent("    ")
            .addComment(AUTO_GENERATION_COMMENT)
            .build()

    kotlinFile.writeTo(File("diktat-rules/src/main/kotlin")) // fixme: need to add it to pom
}
