package test.paragraph4.null_checks

fun test() {
    val some: Int? = null
    some ?: run {
println("some")
bar()
}

    some?.let {
println("some")
bar()
}

    if (some == null && true) {
        print("asd")
    }

    some?.let {
print("qwe")
}
?: run {
print("asd")
}

    some?.let {
print("qweqwe")
}

    some?.let {
print("qqq")
}
?: run {
print("www")
}

    some?.let {
print("ttt")
}

    some?.let {
print("ttt")
}
?: run {
null
value
}
}

fun foo() {
    var result: Int? = 10
    while (result != 0 ) {
        result?.let {
goo()
}
?: run {
for(i in 1..10)
break
}
    }
    while (result != 0) {
        result = goo()
        if (result != null) {
            goo()
        } else {
            println(123)
            break
        }
    }
}

