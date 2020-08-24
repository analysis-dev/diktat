package org.cqfn.diktat.ruleset.chapter2

import com.pinterest.ktlint.core.LintError
import org.cqfn.diktat.common.config.rules.RulesConfig
import generated.WarningNames
import org.cqfn.diktat.ruleset.constants.Warnings.HEADER_CONTAINS_DATE_OR_AUTHOR
import org.cqfn.diktat.ruleset.constants.Warnings.HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE
import org.cqfn.diktat.ruleset.constants.Warnings.HEADER_MISSING_OR_WRONG_COPYRIGHT
import org.cqfn.diktat.ruleset.constants.Warnings.HEADER_NOT_BEFORE_PACKAGE
import org.cqfn.diktat.ruleset.constants.Warnings.HEADER_WRONG_FORMAT
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.comments.HeaderCommentRule
import org.cqfn.diktat.util.TEST_FILE_NAME
import org.cqfn.diktat.util.lintMethod
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class HeaderCommentRuleTest {

    private val ruleId: String = "$DIKTAT_RULE_SET_ID:header-comment"

    private val rulesConfigList: List<RulesConfig> = listOf(
        RulesConfig(HEADER_MISSING_OR_WRONG_COPYRIGHT.name, true,
            mapOf("copyrightText" to "Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved."))
    )

    private val rulesConfigListCn: List<RulesConfig> = listOf(
        RulesConfig(HEADER_MISSING_OR_WRONG_COPYRIGHT.name, true,
            mapOf("copyrightText" to "版权所有 (c) 华为技术有限公司 2012-2020"))
    )

    private val copyrightBlock = """
        /*
         * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
         */
    """.trimIndent()

    @Test
    @Tag(WarningNames.HEADER_WRONG_FORMAT)
    fun `file header comment (positive example)`() {
        lintMethod(HeaderCommentRule(),
                """
                $copyrightBlock
                /**
                 * Very useful description, why this file has two classes
                 * foo bar baz
                 */

                package org.cqfn.diktat.example

                class Example1 { }

                class Example2 { }
            """.trimIndent(),
                rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.HEADER_WRONG_FORMAT)
    fun `file header comment with Chinese version copyright (positive example)`() {
        lintMethod(HeaderCommentRule(),
                """
                /*
                 * 版权所有 (c) 华为技术有限公司 2012-2020
                 */
                /**
                 * Very useful description, why this file has two classes
                 * foo bar baz
                 */

                package org.cqfn.diktat.example

                class Example1 { }

                class Example2 { }
            """.trimIndent(),
                rulesConfigList = rulesConfigListCn
        )
    }

    @Test
    @Tag(WarningNames.HEADER_MISSING_OR_WRONG_COPYRIGHT)
    fun `copyright should not be placed inside KDoc`() {
        lintMethod(HeaderCommentRule(),
                """
                /**
                 * Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
                 */
                /**
                 * Very useful description, why this file has two classes
                 * foo bar baz
                 */

                package org.cqfn.diktat.example

                class Example1 { }

                class Example2 { }
            """.trimIndent(),
                LintError(1, 1, ruleId, "${HEADER_MISSING_OR_WRONG_COPYRIGHT.warnText()} $TEST_FILE_NAME", true),
                rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.HEADER_MISSING_OR_WRONG_COPYRIGHT)
    fun `copyright should not be placed inside KDoc (Chinese version)`() {
        lintMethod(HeaderCommentRule(),
                """
                /**
                 * 版权所有 (c) 华为技术有限公司 2012-2020
                 */
                /**
                 * Very useful description, why this file has two classes
                 * foo bar baz
                 */

                package org.cqfn.diktat.example

                class Example1 { }

                class Example2 { }
            """.trimIndent(),
                LintError(1, 1, ruleId, "${HEADER_MISSING_OR_WRONG_COPYRIGHT.warnText()} $TEST_FILE_NAME", true),
                rulesConfigList = rulesConfigListCn
        )
    }

    @Test
    @Tag(WarningNames.HEADER_MISSING_OR_WRONG_COPYRIGHT)
    fun `copyright should not be placed inside single line comment`() {
        lintMethod(HeaderCommentRule(),
                """
                // Copyright (c) Huawei Technologies Co., Ltd. 2012-2020. All rights reserved.
                /**
                 * Very useful description, why this file has two classes
                 * foo bar baz
                 */

                package org.cqfn.diktat.example

                class Example1 { }

                class Example2 { }
            """.trimIndent(),
                LintError(1, 1, ruleId, "${HEADER_MISSING_OR_WRONG_COPYRIGHT.warnText()} $TEST_FILE_NAME", true),
                rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.HEADER_CONTAINS_DATE_OR_AUTHOR)
    fun `@author tag is not allowed in header comment`() {
        lintMethod(HeaderCommentRule(),
                """
                |$copyrightBlock
                |/**
                | * Description of this file
                | * @author anonymous
                | */
                |
                |package org.cqfn.diktat.example
                |
                |class Example { }
            """.trimMargin(),
                LintError(4, 1, ruleId, "${HEADER_CONTAINS_DATE_OR_AUTHOR.warnText()} * @author anonymous"),
                rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE)
    fun `file with zero classes should have header KDoc`() {
        lintMethod(HeaderCommentRule(),
                """
                package org.cqfn.diktat.example

                val CONSTANT = 42

                fun foo(): Int = 42
            """.trimIndent(),
                LintError(1, 1, ruleId, "${HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE.warnText()} $TEST_FILE_NAME", false),
                rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE)
    fun `file with multiple classes should have header KDoc`() {
        lintMethod(HeaderCommentRule(),
                """
                package org.cqfn.diktat.example

                class Example1 { }

                class Example2 { }
            """.trimIndent(),
                LintError(1, 1, ruleId, "${HEADER_MISSING_IN_NON_SINGLE_CLASS_FILE.warnText()} $TEST_FILE_NAME", false),
                rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.HEADER_WRONG_FORMAT)
    fun `header KDoc should have newline after it`() {
        lintMethod(HeaderCommentRule(),
                """
               |$copyrightBlock
               |/**
               | * Very useful description
               | * foo bar baz
               | */
               |package org.cqfn.diktat.example
               |
               |class Example { }
            """.trimMargin(),
                LintError(4, 1, ruleId, "${HEADER_WRONG_FORMAT.warnText()} header KDoc should have a new line after", true),
                rulesConfigList = rulesConfigList
        )
    }

    @Test
    @Tag(WarningNames.HEADER_NOT_BEFORE_PACKAGE)
    fun `header KDoc should be placed before package and imports`() {
        lintMethod(HeaderCommentRule(),
                """
                |package org.cqfn.diktat.example
                |
                |import org.cqfn.diktat.example.Foo
                |
                |/**
                | * This is a code snippet for tests
                | */
                |
                |/**
                | * This is an example class
                | */
                |class Example { }
            """.trimMargin(),
                LintError(5, 1, ruleId, "${HEADER_NOT_BEFORE_PACKAGE.warnText()} /TestFileName.kt", true),
                rulesConfigList = listOf()
        )
    }
}
