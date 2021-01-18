package org.cqfn.diktat.ruleset.rules

import org.cqfn.diktat.common.config.rules.RulesConfig
import org.cqfn.diktat.common.config.rules.getCommonConfiguration
import org.cqfn.diktat.ruleset.constants.EmitType
import org.cqfn.diktat.ruleset.constants.Warnings.INCORRECT_PACKAGE_SEPARATOR
import org.cqfn.diktat.ruleset.constants.Warnings.PACKAGE_NAME_INCORRECT_CASE
import org.cqfn.diktat.ruleset.constants.Warnings.PACKAGE_NAME_INCORRECT_PATH
import org.cqfn.diktat.ruleset.constants.Warnings.PACKAGE_NAME_INCORRECT_PREFIX
import org.cqfn.diktat.ruleset.constants.Warnings.PACKAGE_NAME_INCORRECT_SYMBOLS
import org.cqfn.diktat.ruleset.constants.Warnings.PACKAGE_NAME_MISSING
import org.cqfn.diktat.ruleset.utils.*

import com.pinterest.ktlint.core.Rule
import com.pinterest.ktlint.core.ast.ElementType.DOT_QUALIFIED_EXPRESSION
import com.pinterest.ktlint.core.ast.ElementType.IDENTIFIER
import com.pinterest.ktlint.core.ast.ElementType.PACKAGE_DIRECTIVE
import com.pinterest.ktlint.core.ast.ElementType.REFERENCE_EXPRESSION
import com.pinterest.ktlint.core.ast.isLeaf
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.com.intellij.psi.impl.source.tree.PsiWhiteSpaceImpl
import org.jetbrains.kotlin.konan.file.File
import org.jetbrains.kotlin.lexer.KtTokens.PACKAGE_KEYWORD
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger

/**
 * Rule 1.3: package name is in lower case and separated by dots, code developed internally in your company (in example Huawei) should start
 * with it's domain (like com.huawei), and the package name is allowed to have numbers
 *
 * Current limitations and FixMe:
 * need to support autofixing of directories in the same way as package is named. For example if we have package name:
 * package a.b.c.D -> then class D should be placed in a/b/c/ directories
 */
@Suppress("ForbiddenComment", "TOO_MANY_LINES_IN_LAMBDA")
class PackageNaming(private val configRules: List<RulesConfig>) : Rule("package-naming") {
    private var isFixMode: Boolean = false
    private lateinit var emitWarn: EmitType
    private lateinit var domainName: String

    override fun visit(node: ASTNode,
                       autoCorrect: Boolean,
                       emit: EmitType) {
        isFixMode = autoCorrect
        emitWarn = emit

        val configuration by configRules.getCommonConfiguration()
        configuration.domainName?.let {
            domainName = it
            if (node.elementType == PACKAGE_DIRECTIVE) {
                val filePath = node.getRootNode().getFilePath()
                // calculating package name based on the directory where the file is placed
                val realPackageName = calculateRealPackageName(filePath)

                // if node isLeaf - this means that there is no package name declared
                if (node.isLeaf() && !filePath.isKotlinScript()) {
                    warnAndFixMissingPackageName(node, realPackageName, filePath)
                    return
                }

                // getting all identifiers from existing package name into the list like [org, diktat, project]
                val wordsInPackageName = node.findAllNodesWithSpecificType(IDENTIFIER)

                // no need to check that packageIdentifiers is empty, because in this case parsing will fail
                checkPackageName(wordsInPackageName, node)
                // fix in checkFilePathMatchesWithPackageName is much more aggressive than fixes in checkPackageName, they can conflict
                checkFilePathMatchesWithPackageName(wordsInPackageName, realPackageName, node)
            }
        } ?: if (visitorCounter.incrementAndGet() == 1) {
            log.error("Not able to find an external configuration for domain" +
                    " name in the common configuration (is it missing in yml config?)")
        }
    }

    /**
     * checking and fixing the case when package directive is missing in the file
     */
    private fun warnAndFixMissingPackageName(
        initialPackageDirectiveNode: ASTNode,
        realPackageName: List<String>,
        filePath: String) {
        val fileName = filePath.substringAfterLast(File.separator)
        PACKAGE_NAME_MISSING.warnAndFix(configRules, emitWarn, isFixMode, fileName,
            initialPackageDirectiveNode.startOffset, initialPackageDirectiveNode) {
            if (realPackageName.isNotEmpty()) {
                // creating node for package directive using Kotlin parser
                val newPackageDirectiveName = realPackageName.joinToString(PACKAGE_SEPARATOR)
                insertNewPackageName(initialPackageDirectiveNode, newPackageDirectiveName)
            }
        }
    }

    /**
     * calculating real package name based on the directory path where the file is stored
     *
     * @return list with words that are parts of package name like [org, diktat, name]
     */
    private fun calculateRealPackageName(fileName: String): List<String> {
        val filePathParts = fileName.splitPathToDirs()

        return if (!filePathParts.contains(PACKAGE_PATH_ANCHOR)) {
            log.error("Not able to determine a path to a scanned file or src directory cannot be found in it's path." +
                    " Will not be able to determine correct package name. It can happen due to missing <src> directory in the path")
            emptyList()
        } else {
            // creating a real package name:
            // 1) getting a path after the base project directory (after "src" directory)
            // 2) removing src/main/kotlin/java/e.t.c dirs and removing file name
            // 3) adding company's domain name at the beginning
            val fileSubDir = filePathParts.subList(filePathParts.lastIndexOf(PACKAGE_PATH_ANCHOR), filePathParts.size - 1)
                .dropWhile { languageDirNames.contains(it) }
            // no need to add DOMAIN_NAME to the package name if it is already in path
            val domainPrefix = if (!fileSubDir.joinToString(PACKAGE_SEPARATOR).startsWith(domainName)) domainName.split(PACKAGE_SEPARATOR) else emptyList()
            domainPrefix + fileSubDir
        }
    }

    private fun checkPackageName(wordsInPackageName: List<ASTNode>, packageDirectiveNode: ASTNode) {
        // all words should be in a lower case (lower case letters/digits/underscore)
        wordsInPackageName
            .filter { word -> word.text.hasUppercaseLetter() }
            .forEach {
                PACKAGE_NAME_INCORRECT_CASE.warnAndFix(configRules, emitWarn, isFixMode, it.text, it.startOffset, it) {
                    it.toLower()
                }
            }

        // package name should start from a company's domain name
        if (wordsInPackageName.isNotEmpty() && !isDomainMatches(wordsInPackageName)) {
            PACKAGE_NAME_INCORRECT_PREFIX.warnAndFix(configRules, emitWarn, isFixMode, domainName,
                wordsInPackageName[0].startOffset, wordsInPackageName[0]) {
                val oldPackageName = wordsInPackageName.joinToString(PACKAGE_SEPARATOR) { it.text }
                val newPackageName = "$domainName$PACKAGE_SEPARATOR$oldPackageName"
                insertNewPackageName(packageDirectiveNode, newPackageName)
            }
        }

        // all words should contain only ASCII letters or digits
        wordsInPackageName
            .filter { word -> !areCorrectSymbolsUsed(word.text) }
            .forEach { PACKAGE_NAME_INCORRECT_SYMBOLS.warn(configRules, emitWarn, isFixMode, it.text, it.startOffset, it) }

        // all words should contain only ASCII letters or digits
        wordsInPackageName.forEach { correctPackageWordSeparatorsUsed(it) }
    }

    /**
     * only letters, digits and underscore are allowed
     */
    private fun areCorrectSymbolsUsed(word: String): Boolean {
        // underscores are allowed in some cases - see "exceptionForUnderscore"
        val wordFromPackage = word.replace("_", "")
        return wordFromPackage.isASCIILettersAndDigits()
    }

    /**
     * in package name no other separators except dot should be used, package words (parts) should be concatenated
     * without any symbols or should use dot symbol - this is the only way
     */
    private fun correctPackageWordSeparatorsUsed(word: ASTNode) {
        if (word.text.contains("_") && !isExceptionForUnderscore(word.text)) {
            INCORRECT_PACKAGE_SEPARATOR.warnAndFix(configRules, emitWarn, isFixMode, word.text, word.startOffset, word) {
                (word as LeafPsiElement).replaceWithText(word.text.replace("_", ""))
            }
        }
    }

    /** Underscores! In some cases, if the package name starts with a number or other characters,
     * but these characters cannot be used at the beginning of the Java/Kotlin package name,
     * or the package name contains reserved Java keywords, underscores are allowed.
     * For example: org.example.hyphenated_name,int_.example, com.example._123name
     */
    private fun isExceptionForUnderscore(word: String): Boolean {
        val wordFromPackage = word.replace("_", "")

        return wordFromPackage[0].isDigit() ||
                wordFromPackage.isKotlinKeyWord() ||
                wordFromPackage.isJavaKeyWord()
    }

    /**
     * function simply checks that package name starts with a proper domain name
     */
    private fun isDomainMatches(packageNameParts: List<ASTNode>): Boolean {
        val packageNamePrefix = domainName.split(PACKAGE_SEPARATOR)
        if (packageNameParts.size < packageNamePrefix.size) {
            return false
        }

        for (i in packageNamePrefix.indices) {
            if (packageNameParts[i].text != packageNamePrefix[i]) {
                return false
            }
        }
        return true
    }

    @Suppress("UnsafeCallOnNullableType")
    private fun insertNewPackageName(packageDirectiveNode: ASTNode, packageName: String) {
        // package name can be dot qualified expression or a reference expression in case it contains only one word
        val packageNameNode = packageDirectiveNode.findChildByType(DOT_QUALIFIED_EXPRESSION)
            ?: packageDirectiveNode.findChildByType(REFERENCE_EXPRESSION)

        val generatedPackageDirective = KotlinParser()
            .createNode("$PACKAGE_KEYWORD $packageName", true)

        packageNameNode?.let {
            // simply replacing only node connected with the package name, all other nodes remain unchanged
            packageDirectiveNode.replaceChild(packageNameNode,
                generatedPackageDirective.findLeafWithSpecificType(DOT_QUALIFIED_EXPRESSION)!!)
        }
            ?: run {
                // there is missing package statement in a file, so it will be created and inserted
                val newPackageDirective = generatedPackageDirective.findLeafWithSpecificType(PACKAGE_DIRECTIVE)!!
                packageDirectiveNode.treeParent.replaceChild(packageDirectiveNode, newPackageDirective)
                newPackageDirective.treeParent.addChild(PsiWhiteSpaceImpl("\n\n"), newPackageDirective.treeNext)
            }
    }

    /**
     * checking and fixing package directive if it does not match with the directory where the file is stored
     */
    private fun checkFilePathMatchesWithPackageName(packageNameParts: List<ASTNode>,
                                                    realNameParts: List<String>,
                                                    packageDirective: ASTNode) {
        if (realNameParts.isNotEmpty() && packageNameParts.map { node -> node.text } != realNameParts) {
            val realPackageNameStr = realNameParts.joinToString(PACKAGE_SEPARATOR)
            val offset = packageNameParts[0].startOffset
            PACKAGE_NAME_INCORRECT_PATH.warnAndFix(configRules, emitWarn, isFixMode,
                realPackageNameStr, offset, packageNameParts[0]) {
                insertNewPackageName(packageDirective, realPackageNameStr)
            }
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger(PackageNaming::class.java)

        /**
         * Directory which is considered the start of sources file tree
         */
        const val PACKAGE_PATH_ANCHOR = "src"

        /**
         * Symbol that is used to separate parts in package name
         */
        const val PACKAGE_SEPARATOR = "."

        /**
         * tricky hack (counter) that helps not to raise multiple warnings about the package name if config is missing
         */
        var visitorCounter = AtomicInteger(0)

        /**
         * Targets described in [KMM documentation](https://kotlinlang.org/docs/reference/mpp-supported-platforms.html)
         */
        private val kmmTargets = listOf("common", "jvm", "js", "android", "ios", "androidNativeArm32", "androidNativeArm64", "iosArm32", "iosArm64", "iosX64",
            "watchosArm32", "watchosArm64", "watchosX86", "tvosArm64", "tvosX64", "macosX64", "linuxArm64", "linuxArm32Hfp", "linuxMips32", "linuxMipsel32", "linuxX64",
            "mingwX64", "mingwX86", "wasm32")

        /**
         * Directories that are supposed to be first in sources file paths, relative to [PACKAGE_PATH_ANCHOR].
         * For kotlin multiplatform projects directories for targets from [kmmTargets] are supported.
         */
        val languageDirNames = listOf("src", "main", "test", "java", "kotlin") + kmmTargets.flatMap { listOf("${it}Main", "${it}Test") }
    }
}
