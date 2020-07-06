package org.cqfn.diktat.ruleset.utils

import org.jetbrains.kotlin.lexer.KtTokens

val JAVA = arrayOf("abstract", "assert", "boolean",
        "break", "byte", "case", "catch", "char", "class", "const",
        "continue", "default", "do", "double", "else", "extends", "false",
        "final", "finally", "float", "for", "goto", "if", "implements",
        "import", "instanceof", "int", "interface", "long", "native",
        "new", "null", "package", "private", "protected", "public",
        "return", "short", "static", "strictfp", "super", "switch",
        "synchronized", "this", "throw", "throws", "transient", "true",
        "try", "void", "volatile", "while")

val KOTLIN = KtTokens.KEYWORDS.types.map { line -> line.toString() }
        .plus(KtTokens.SOFT_KEYWORDS.types.map { line -> line.toString() })

fun String.isJavaKeyWord() = JAVA.contains(this)
fun String.isKotlinKeyWord() = KOTLIN.contains(this)

fun String.isASCIILettersAndDigits(): Boolean = this.all { it.isDigit() || it in 'A'..'Z' || it in 'a'..'z' }

fun String.isDigits(): Boolean = this.all { it.isDigit() }

fun String.hasUppercaseLetter(): Boolean = this.any { it.isUpperCase() }

fun String.containsOneLetterOrZero(): Boolean {
    val count = this.count { it.isLetter() }
    return count == 1 || count == 0
}

fun String.countSubStringOccurrences(sub: String) = this.split(sub).size - 1

fun String.splitPathToDirs(): List<String> =
    this.replace("\\", "/")
        .replace("//", "/")
        .split("/")

/**
 * method checks that string has prefix like:
 * mFunction, kLength or M_VAR
 */
@Suppress("ForbiddenComment")
fun String.hasPrefix(): Boolean {
    // checking cases like mFunction
    if (this.isLowerCamelCase() && this.length >= 2 && this.substring(0, 2).count { it in 'A'..'Z' } == 1) return true
    // checking cases like M_VAL
    if (this.isUpperSnakeCase() && this.length >= 2 && this.substring(0, 2).contains('_')) return true
    return false
}

/**
 * removing the prefix in the word
 * M_VAR -> VAR
 * mVariable -> variable
 */
@Suppress("ForbiddenComment")
fun String.removePrefix(): String {
    // FixMe: there can be cases when after you will change variable name - it becomes a keyword
    if (this.isLowerCamelCase()) return this[1].toLowerCase() + this.substring(2)
    if (this.isUpperSnakeCase()) return this.substring(2)
    return this
}




