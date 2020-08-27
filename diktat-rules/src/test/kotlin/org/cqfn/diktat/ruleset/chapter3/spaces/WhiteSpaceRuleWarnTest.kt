package org.cqfn.diktat.ruleset.chapter3.spaces

import com.pinterest.ktlint.core.LintError
import generated.WarningNames
import org.cqfn.diktat.ruleset.constants.Warnings.WRONG_WHITESPACE
import org.cqfn.diktat.ruleset.rules.DIKTAT_RULE_SET_ID
import org.cqfn.diktat.ruleset.rules.WhiteSpaceRule
import org.cqfn.diktat.util.lintMethod
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test

class WhiteSpaceRuleWarnTest {
    private val ruleId = "$DIKTAT_RULE_SET_ID:horizontal-whitespace"
    private fun keywordWarn(keyword: String, sep: String) =
            "${WRONG_WHITESPACE.warnText()} keyword '$keyword' should be separated from '$sep' with a whitespace"

    private val lbraceWarn = "${WRONG_WHITESPACE.warnText()} there should be a whitespace before '{'"
    private val eolSpaceWarn = "${WRONG_WHITESPACE.warnText()} there should be no spaces in the end of line"
    private fun binaryOpWarn(op: String, before: Int?, after: Int?, reqBefore: Int, reqAfter: Int?) =
//            "${WRONG_WHITESPACE.warnText()} $op should ${if (isException) "not " else ""}be surrounded by whitespaces"
            "${WRONG_WHITESPACE.warnText()} $op should have $reqBefore space(s) before" +
                    (if (reqAfter != null) " and $reqAfter space(s) after" else "") +
                    ", but has" +
                    (if (before != null) " $before space(s) before" else "") +
                    (if (before != null && after != null) " and" else "") +
                    (if (after != null) " $after space(s) after" else "")

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `keywords should have space before opening parenthesis and braces - positive example`() {
        lintMethod(WhiteSpaceRule(),
                """
                    |class Example {
                    |    constructor(val a: Int)
                    |
                    |    fun foo() {
                    |         if (condition) { }
                    |         else { }
                    |         for (i in 1..100) { }
                    |         when (expression) { }
                    |    }
                    |}
                """.trimMargin()
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `keywords should have space before opening parenthesis`() {
        lintMethod(WhiteSpaceRule(),
                """
                    |class Example {
                    |    fun foo() {
                    |        if(condition) { }
                    |        for  (i in 1..100) { }
                    |        when(expression) { }
                    |    }
                    |}
                """.trimMargin(),
                LintError(3, 11, ruleId, keywordWarn("if", "("), true),
                LintError(4, 14, ruleId, keywordWarn("for", "("), true),
                LintError(5, 13, ruleId, keywordWarn("when", "("), true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `constructor should not have space before opening parenthesis`() {
        lintMethod(WhiteSpaceRule(),
                """
                    |class Example {
                    |    constructor (val a: Int)
                    |}
                """.trimMargin(),
                LintError(2, 5, ruleId, "${WRONG_WHITESPACE.warnText()} keyword 'constructor' should not be separated from '(' with a whitespace", true)
        )
    }

    @Test
    @Tag(WarningNames.WRONG_WHITESPACE)
    fun `keywords should have space before opening braces`() {
        lintMethod(WhiteSpaceRule(),
                """
                    |class Example {
                    |    fun foo() {
                    |         if (condition) { }
                    |         else{}
                    |         try{ }
                    |         finally{ }
                    |    }
                    |}
                """.trimMargin(),
                LintError(4, 14, ruleId, keywordWarn("else", "{"), true),
                LintError(5, 13, ruleId, keywordWarn("try", "{"), true),
                LintError(6, 17, ruleId, keywordWarn("finally", "{"), true)
        )
    }

    @Test
    fun `keywords should have space before opening braces - else without braces`() {
        lintMethod(WhiteSpaceRule(),
                """
                    |fun foo() {
                    |     if (condition)
                    |         bar()
                    |     else
                    |         baz()
                    |     
                    |     if (condition) bar() else  baz()
                    |}
                """.trimMargin(),
                LintError(7, 33, ruleId, keywordWarn("else", "baz"), true)
        )
    }

    @Test
    fun `all opening braces should have leading space`() {
        lintMethod(WhiteSpaceRule(),
                """
                    |class Example{
                    |    fun foo(){
                    |        list.run{
                    |            map{ bar(it) }
                    |        }
                    |    }
                    |}
                """.trimMargin(),
                LintError(1, 14, ruleId, lbraceWarn, true),
                LintError(2, 14, ruleId, lbraceWarn, true),
                LintError(3, 17, ruleId, lbraceWarn, true),
                LintError(4, 16, ruleId, lbraceWarn, true)
        )
    }

    @Test
    fun `all opening braces should have leading space - exception for lambdas as arguments`() {
        lintMethod(WhiteSpaceRule(),
                """
                    |fun foo(a: (Int) -> Int, b: Int) {
                    |    foo({x: Int -> x}, 5)
                    |}
                    |
                    |fun bar(a: (Int) -> Int, b: Int) {
                    |    bar( {x: Int -> x}, 5)
                    |}
                    |
                    |val lambda = { x: Int -> 2 * x }
                """.trimMargin(),
                LintError(6, 10, ruleId, "${WRONG_WHITESPACE.warnText()} there should be no whitespace before '{' of lambda inside argument list", true)
        )
    }

    @Test
    fun `binary operators should be surrounded by spaces - positive example`() {
        lintMethod(WhiteSpaceRule(),
                """
                    |class Example<T> where T : UpperType {
                    |    fun foo(t: T) = t + 1
                    |    
                    |    fun bar() {
                    |        listOf<T>().map(this::foo).filter { elem -> predicate(elem) }
                    |    }
                    |}
                """.trimMargin()
        )
    }

    @Test
    fun `should not false positively trigger when operators are surrounded with newlines`() {
        lintMethod(WhiteSpaceRule(),
                """
                    |class Example<T> where T
                    |                 :
                    |                 UpperType {
                    |    fun foo(t: T) =
                    |            t + 1
                    |    
                    |    fun bar() {
                    |        listOf<T>()
                    |            .map(this
                    |                ::foo)
                    |            .filter { elem ->
                    |                 predicate(elem)
                    |             }
                    |    }
                    |}
                """.trimMargin()
        )
    }

    @Test
    fun `binary operators should be surrounded by spaces`() {
        lintMethod(WhiteSpaceRule(),
                """
                    |class Example<T, R, Q> where T:UpperType, R: UpperType, Q :UpperType {
                    |    fun foo(t: T) = t+ 1
                    |    fun foo2(t: T) = t+1
                    |    fun foo3(t: T) = t +1
                    |    
                    |    fun bar() {
                    |        listOf<T>() .map(this ::foo) ?.filter { elem ->predicate(elem) } !!.first()
                    |        listOf<T>() . map(this :: foo) ?. filter { elem->predicate(elem) } !! .first()
                    |        listOf<T>(). map(this:: foo)?. filter { elem-> predicate(elem) }!!. first()
                    |    }
                    |}
                """.trimMargin(),
                LintError(1, 31, ruleId, binaryOpWarn(":", 0, 0, 1, 1), true),
                LintError(1, 44, ruleId, binaryOpWarn(":", 0, null, 1, 1), true),
                LintError(1, 59, ruleId, binaryOpWarn(":", null, 0, 1, 1), true),
                LintError(2, 22, ruleId, binaryOpWarn("+", 0, null, 1, 1), true),
                LintError(3, 23, ruleId, binaryOpWarn("+", 0, 0, 1, 1), true),
                LintError(4, 24, ruleId, binaryOpWarn("+", null, 0, 1, 1), true),
                LintError(7, 21, ruleId, binaryOpWarn(".", 1, null, 0, 0), true),
                LintError(7, 31, ruleId, binaryOpWarn("::", 1, null, 0, 0), true),
                LintError(7, 38, ruleId, binaryOpWarn("?.", 1, null, 0, 0), true),
                LintError(7, 54, ruleId, binaryOpWarn("->", null, 0, 1, 1), true),
                LintError(7, 74, ruleId, binaryOpWarn("!!", 1, null, 0, 0), true),
                LintError(8, 21, ruleId, binaryOpWarn(".", 1, 1, 0, 0), true),
                LintError(8, 32, ruleId, binaryOpWarn("::", 1, 1, 0, 0), true),
                LintError(8, 40, ruleId, binaryOpWarn("?.", 1, 1, 0, 0), true),
                LintError(8, 56, ruleId, binaryOpWarn("->", 0, 0, 1, 1), true),
                LintError(8, 76, ruleId, binaryOpWarn("!!", 1, 1, 0, 0), true),
                LintError(8, 79, ruleId, binaryOpWarn(".", 1, null, 0, 0), true),
                LintError(9, 20, ruleId, binaryOpWarn(".", null, 1, 0, 0), true),
                LintError(9, 30, ruleId, binaryOpWarn("::", null, 1, 0, 0), true),
                LintError(9, 37, ruleId, binaryOpWarn("?.", null, 1, 0, 0), true),
                LintError(9, 53, ruleId, binaryOpWarn("->", 0, null, 1, 1), true),
                LintError(9, 75, ruleId, binaryOpWarn(".", null, 1, 0, 0), true)
        )
    }

    @Test
    fun `operators with single space after - positive example`() {
        lintMethod(WhiteSpaceRule(),
                """
                    |class Example<T> {
                    |    fun foo(t1: T, t2: T) {
                    |        println(); println()
                    |    }
                    |    
                    |    fun bar(t: T,
                    |            d: T) {
                    |        println();
                    |    }
                    |    
                    |    val x: Int
                    |}
                """.trimMargin()
        )
    }

    @Test
    fun `operators with single space after`() {
        lintMethod(WhiteSpaceRule(),
                """
                    |class Example<T> {${" "}
                    |    fun foo(t1 :T ,t2:T) {${" "} 
                    |        println();println()
                    |        println() ; println()
                    |    }
                    |    
                    |    val x : Int
                    |}
                """.trimMargin(),
                LintError(1, 19, ruleId, eolSpaceWarn, true),
                LintError(2, 16, ruleId, binaryOpWarn(":", 1, 0, 0, 1), true),
                LintError(2, 19, ruleId, binaryOpWarn(",", 1, 0, 0, 1), true),
                LintError(2, 22, ruleId, binaryOpWarn(":", null, 0, 0, 1), true),
                LintError(2, 27, ruleId, eolSpaceWarn, true),
                LintError(3, 18, ruleId, binaryOpWarn(";", null, 0, 0, 1), true),
                LintError(4, 19, ruleId, binaryOpWarn(";", 1, null, 0, 1), true),
                LintError(7, 11, ruleId, binaryOpWarn(":", 1, null, 0, 1), true)
        )
    }

    @Test
    fun `operators with single space after - exceptional cases - positive example`() {
        lintMethod(WhiteSpaceRule(),
                """
                    |abstract class Foo<out T : Any> : IFoo { }
                    |
                    |class FooImpl : Foo() {
                    |    constructor(x: String) : this(x) { /*...*/ }
                    |
                    |    val x = object : IFoo { /*...*/ }
                    |}
                """.trimMargin()
        )
    }

    @Test
    fun `operators with single space after - exceptional cases`() {
        lintMethod(WhiteSpaceRule(),
                """
                    |abstract class Foo<out T: Any>: IFoo { }
                    |
                    |class FooImpl: Foo() {
                    |    constructor(x: String): this(x) { /*...*/ }
                    |
                    |    val x = object: IFoo { /*...*/ }
                    |}
                """.trimMargin(),
                LintError(1, 25, ruleId, binaryOpWarn(":", 0, null, 1, 1), true),
                LintError(1, 31, ruleId, binaryOpWarn(":", 0, null, 1, 1), true),
                LintError(3, 14, ruleId, binaryOpWarn(":", 0, null, 1, 1), true),
                LintError(4, 27, ruleId, binaryOpWarn(":", 0, null, 1, 1), true),
                LintError(6, 19, ruleId, binaryOpWarn(":", 0, null, 1, 1), true)
        )
    }

    @Test
    fun `there should be no space before ? in nullable types`() {
        lintMethod(WhiteSpaceRule(),
                """
                    |class Example {
                    |    lateinit var x: Int?
                    |    lateinit var x: Int ?
                    |}
                """.trimMargin(),
                LintError(3, 25, ruleId, binaryOpWarn("?", 1, null, 0, null), true)
        )
    }
}
