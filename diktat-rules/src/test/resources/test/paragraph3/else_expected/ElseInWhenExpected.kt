package test.paragraph3.else_expected

enum class TestEnum {
    ONE, TWO
}

fun testWhenExpression() {
    val directoryType = TestEnum.ONE

    when (directoryType) {
        TestEnum.ONE -> "d"
        TestEnum.TWO -> "-"
 else -> {
// this is a generated else block
}}

    val noElse = when (directoryType) {
        TestEnum.ONE -> "d"
        TestEnum.TWO -> "a"
    }

    val inLambda = {x: Int -> when(x) {
        1 -> print(5)
    }
    }
}

sealed class Expr {
    class Num(val value: Int) : Expr()
    class Sum(val left: Expr, val right: Expr) : Expr()
}
fun eval(e: Expr): Int =
    when (e) {
        is Expr.Num -> e.value
        is Expr.Sum -> eval(e.right) + eval(e.left)
    }

