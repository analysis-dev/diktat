package org.cqfn.diktat.ruleset.generation

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import java.io.File
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.comments.HeaderCommentRule.Companion.afterCopyrightRegex
import org.cqfn.diktat.ruleset.rules.comments.HeaderCommentRule.Companion.curYear
import org.cqfn.diktat.ruleset.rules.comments.HeaderCommentRule.Companion.hyphenRegex

private val autoGenerationComment =
        """
            | This document was auto generated, please don't modify it.
            | This document contains all enum properties from Warnings.kt as Strings.
        """.trimMargin()

fun main() {
    generateWarningNames()
    validateYear()
}

private fun generateWarningNames() {
    val enumValNames = Warnings.values().map { it.name }

    val propertyList = enumValNames.map {
        PropertySpec
                .builder(it, String::class)
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
            .addComment(autoGenerationComment)
            .build()

    kotlinFile.writeTo(File("diktat-rules/src/main/kotlin"))  // fixme: need to add it to pom
}

private fun validateYear() {
    val file = File("diktat-rules/src/test/resources/test/paragraph2/header/CopyrightDifferentYearExpected.kt")
    val tempFile = createTempFile()
    tempFile.printWriter().use { writer ->
        file.forEachLine { line ->
            writer.println(when {
                hyphenRegex.matches(line) -> hyphenRegex.replace(line) {
                    val years = it.value.split("-")
                    val validYears = "${years[0]}-${curYear}"
                    line.replace(hyphenRegex, validYears)
                }
                afterCopyrightRegex.matches(line) -> afterCopyrightRegex.replace(line) {
                    val copyrightYears = it.value.split("(c)", "(C)", "©")
                    val validYears = "${copyrightYears[0]}-${curYear}"
                    line.replace(afterCopyrightRegex, validYears)
                }
                else -> line
            })
        }
    }
    file.delete()
    tempFile.renameTo(file)
}
