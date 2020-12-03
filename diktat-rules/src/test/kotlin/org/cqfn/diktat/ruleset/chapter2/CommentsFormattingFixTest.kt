package org.cqfn.diktat.ruleset.chapter2

import org.cqfn.diktat.ruleset.rules.kdoc.CommentsFormatting
import org.cqfn.diktat.util.FixTestBase

import generated.WarningNames.COMMENT_WHITE_SPACE
import generated.WarningNames.FIRST_COMMENT_NO_SPACES
import generated.WarningNames.IF_ELSE_COMMENTS
import generated.WarningNames.WRONG_NEWLINES_AROUND_KDOC
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Tags
import org.junit.jupiter.api.Test

class CommentsFormattingFixTest : FixTestBase("test/paragraph2/kdoc/", ::CommentsFormatting) {
    @Test
    @Tag(WRONG_NEWLINES_AROUND_KDOC)
    fun `there should be no blank line between kdoc and it's declaration code`() {
        fixAndCompare("KdocEmptyLineExpected.kt", "KdocEmptyLineTest.kt")
    }

    @Test
    @Tags(Tag(WRONG_NEWLINES_AROUND_KDOC), Tag(COMMENT_WHITE_SPACE), Tag(IF_ELSE_COMMENTS), Tag(FIRST_COMMENT_NO_SPACES))
    fun `check lines and spaces in comments`() {
        fixAndCompare("KdocCodeBlocksFormattingExpected.kt", "KdocCodeBlocksFormattingTest.kt")
    }

    @Test
    @Tags(Tag(WRONG_NEWLINES_AROUND_KDOC), Tag(FIRST_COMMENT_NO_SPACES))
    fun `test example from code style`() {
        fixAndCompare("KdocCodeBlockFormattingExampleExpected.kt", "KdocCodeBlockFormattingExampleTest.kt")
    }

    @Test
    @Tag(WRONG_NEWLINES_AROUND_KDOC)
    fun `regression - should not insert newline before the first comment in a file`() {
        fixAndCompare("NoPackageNoImportExpected.kt", "NoPackageNoImportTest.kt")
    }
}
