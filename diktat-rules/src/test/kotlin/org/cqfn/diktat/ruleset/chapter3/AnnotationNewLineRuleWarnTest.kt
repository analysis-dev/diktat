package org.cqfn.diktat.ruleset.chapter3

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.cqfn.diktat.ruleset.constants.Warnings
import org.cqfn.diktat.ruleset.rules.AnnotationNewLineRule
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.util.LintTestBase
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class AnnotationNewLineRuleWarnTest : LintTestBase(::AnnotationNewLineRule) {
    private val ruleId = "$DIKTAT_RULE_SET_ID:annotation-new-line"


    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `annotation class test good`(){
        lintMethod(
                """
                    |
                    |@SomeAnnotation
                    |@SecondAnnotation
                    |class A {
                    |   val a = 5
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `annotation class test good 2`(){
        lintMethod(
                """
                    |@SomeAnnotation class A {
                    |   val a = 5
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `annotation class test bad`() {
        lintMethod(
                """
                    |@SomeAnnotation @SecondAnnotation
                    |class A {
                    |   val a = 5
                    |}
                """.trimMargin(),
                LintError(1,1, ruleId, "${Warnings.ANNOTATION_NEW_LINE.warnText()} @SomeAnnotation not on a single line", true),
                LintError(1,17, ruleId, "${Warnings.ANNOTATION_NEW_LINE.warnText()} @SecondAnnotation not on a single line", true)
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `annotation class test bad 2`() {
        lintMethod(
                """
                    |@SomeAnnotation @SecondAnnotation class A {
                    |   val a = 5
                    |}
                """.trimMargin(),
                LintError(1,1, ruleId, "${Warnings.ANNOTATION_NEW_LINE.warnText()} @SomeAnnotation not on a single line", true),
                LintError(1,17, ruleId, "${Warnings.ANNOTATION_NEW_LINE.warnText()} @SecondAnnotation not on a single line", true)
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `annotation fun test good`() {
        lintMethod(
                """
                    |class A {
                    |   val a = 5
                    |   
                    |  @SomeAnnotation
                    |  @SecondAnnotation
                    |  fun someFunc() {
                    |       val a = 3
                    |  }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `annotation fun test good 2`() {
        lintMethod(
                """
                    |class A {
                    |   val a = 5
                    |   
                    |  @SomeAnnotation fun someFunc() {
                    |       val a = 3
                    |  }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `annotation fun test bad`() {
        lintMethod(
                """
                    |class A {
                    |   val a = 5
                    |   
                    |  @SomeAnnotation @SecondAnnotation fun someFunc() {
                    |       val a = 3
                    |  }
                    |}
                """.trimMargin(),
                LintError(4,3, ruleId, "${Warnings.ANNOTATION_NEW_LINE.warnText()} @SomeAnnotation not on a single line", true),
                LintError(4,19, ruleId, "${Warnings.ANNOTATION_NEW_LINE.warnText()} @SecondAnnotation not on a single line", true)
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `annotation fun test bad 2`() {
        lintMethod(
                """
                    |class A {
                    |   val a = 5
                    |   
                    |  @SomeAnnotation @SecondAnnotation 
                    |  fun someFunc() {
                    |       val a = 3
                    |  }
                    |}
                """.trimMargin(),
                LintError(4,3, ruleId, "${Warnings.ANNOTATION_NEW_LINE.warnText()} @SomeAnnotation not on a single line", true),
                LintError(4,19, ruleId, "${Warnings.ANNOTATION_NEW_LINE.warnText()} @SecondAnnotation not on a single line", true)
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `annotation constructor test good`() {
        lintMethod(
                """
                    |public class Conf
                    |@Inject
                    |constructor(conf: Int) {
                    |
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `annotation constructor test good 2`() {
        lintMethod(
                """
                    |public class Conf @Inject constructor(conf: Int) {
                    |
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `annotation constructor test good 3`() {
        lintMethod(
                """
                    |public class Conf 
                    |@Inject 
                    |@SomeAnnotation 
                    |constructor(conf: Int) {
                    |
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `annotation secondary constructor test good`() {
        lintMethod(
                """
                    |public class Conf {
                    |   @FirstAnnotation constructor(conf: Conf) {
                    |   
                    |   }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `annotation secondary constructor test bad`() {
        lintMethod(
                """
                    |public class Conf {
                    |   @FirstAnnotation @SecondAnnotation constructor(conf: Conf) {
                    |   
                    |   }
                    |}
                """.trimMargin(),
                LintError(2,4, ruleId, "${Warnings.ANNOTATION_NEW_LINE.warnText()} @FirstAnnotation not on a single line", true),
                LintError(2,21, ruleId, "${Warnings.ANNOTATION_NEW_LINE.warnText()} @SecondAnnotation not on a single line", true)
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `annotation constructor test bad`() {
        lintMethod(
                """
                    |public class Conf @Inject @SomeAnnotation constructor(conf: Int) {
                    |
                    |}
                """.trimMargin(),
                LintError(1,19, ruleId, "${Warnings.ANNOTATION_NEW_LINE.warnText()} @Inject not on a single line", true),
                LintError(1,27, ruleId, "${Warnings.ANNOTATION_NEW_LINE.warnText()} @SomeAnnotation not on a single line", true)
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `annotation constructor test bad 2`() {
        lintMethod(
                """
                    |public class Conf @Inject 
                    |@SomeAnnotation constructor(conf: Int) {
                    |
                    |}
                """.trimMargin(),
                LintError(1,19, ruleId, "${Warnings.ANNOTATION_NEW_LINE.warnText()} @Inject not on a single line", true),
                LintError(2,1, ruleId, "${Warnings.ANNOTATION_NEW_LINE.warnText()} @SomeAnnotation not on a single line", true)
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `no warns in func params`() {
        lintMethod(
                """
                    |public class Conf {
                    |   fun someFunc(@SomeAnnotation conf: JsonConf, @SecondAnnotation some: Int) {
                    |   
                    |   }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.ANNOTATION_NEW_LINE)
    fun `no warns in func params 2`() {
        lintMethod(
                """
                    |public class Conf {
                    |   fun someFunc(@SomeAnnotation @AnotherAnnotation conf: JsonConf, @SecondAnnotation @ThirdAnnotation some: Int) {
                    |   
                    |   }
                    |}
                """.trimMargin()
        )
    }
}
