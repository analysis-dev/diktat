/**
 * Utility methods and constants to work with strings
 */

package org.cqfn.diktat.ruleset.utils

import org.jetbrains.kotlin.lexer.KtTokens

@Suppress("VARIABLE_NAME_INCORRECT_FORMAT")
val JAVA = arrayOf("abstract", "assert", "boolean",
    "break", "byte", "case", "catch", "char", "class", "const",
    "continue", "default", "do", "double", "else", "extends", "false",
    "final", "finally", "float", "for", "goto", "if", "implements",
    "import", "instanceof", "int", "interface", "long", "native",
    "new", "null", "package", "private", "protected", "public",
    "return", "short", "static", "strictfp", "super", "switch",
    "synchronized", "this", "throw", "throws", "transient", "true",
    "try", "void", "volatile", "while")

@Suppress("VARIABLE_NAME_INCORRECT_FORMAT")
val KOTLIN = KtTokens.KEYWORDS.types.map { line -> line.toString() }
    .plus(KtTokens.SOFT_KEYWORDS.types.map { line -> line.toString() })

val loggerPropertyRegex = "(log|LOG|logger)".toRegex()

/**
 * @return whether [this] string represents a Java keyword
 */
fun String.isJavaKeyWord() = JAVA.contains(this)

/**
 * @return whether [this] string represents a Kotlin keyword
 */
fun String.isKotlinKeyWord() = KOTLIN.contains(this)

/**
 * @return whether [this] string contains only ASCII letters and/or digits
 */
@Suppress("FUNCTION_NAME_INCORRECT_CASE")
fun String.isASCIILettersAndDigits(): Boolean = this.all { it.isDigit() || it in 'A'..'Z' || it in 'a'..'z' }

/**
 * @return whether [this] string contains only digits
 */
fun String.isDigits(): Boolean = this.all { it.isDigit() }

/**
 * @return whether [this] string contains any uppercase letters
 */
fun String.hasUppercaseLetter(): Boolean = this.any { it.isUpperCase() }

/**
 * @return whether [this] string contains exactly one or zero letters
 */
@Suppress("FUNCTION_BOOLEAN_PREFIX")
fun String.containsOneLetterOrZero(): Boolean {
    val count = this.count { it.isLetter() }
    return count == 1 || count == 0
}

/**
 * @param sub a substring to search
 * @return count of ocurrences
 */
fun String.countSubStringOccurrences(sub: String) = this.split(sub).size - 1

/**
 * Splits [this] string by file path separator
 *
 * @return list of path parts
 */
fun String.splitPathToDirs(): List<String> =
        this.replace("\\", "/")
            .replace("//", "/")
            .split("/")

/**
 * method checks that string has prefix like:
 * mFunction, kLength or M_VAR
 *
 * @return true if string has prefix
 */
@Suppress("ForbiddenComment")
fun String.hasPrefix(): Boolean {
    // checking cases like mFunction
    if (this.isLowerCamelCase() && this.length >= 2 && this.substring(0, 2).count { it in 'A'..'Z' } == 1) {
        return true
    }
    // checking cases like M_VAL
    if (this.isUpperSnakeCase() && this.length >= 2 && this.substring(0, 2).contains('_')) {
        return true
    }
    return false
}

/**
 * removing the prefix in the word
 * M_VAR -> VAR
 * mVariable -> variable
 *
 * @return a string without prefix
 */
@Suppress("ForbiddenComment")
fun String.removePrefix(): String {
    // FixMe: there can be cases when after you will change variable name - it becomes a keyword
    if (this.isLowerCamelCase()) {
        return this[1].toLowerCase() + this.substring(2)
    }
    if (this.isUpperSnakeCase()) {
        return this.substring(2)
    }
    return this
}

/**
 * Checks if [this] String is a name of a kotlin script file by checking whether file extension equals 'kts'
 *
 * @return true if this is a kotlin script file name, false otherwise
 */
fun String.isKotlinScript() = endsWith(".kts")
