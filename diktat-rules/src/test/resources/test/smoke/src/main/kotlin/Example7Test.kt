package org.cqfn.diktat

fun foo() {
    val prop: Int? = null

    if (prop == null) {
        println("prop is null")
        bar()
    }

    if (prop != null) {
        baz()
        gaz()
    }

    if (prop == null) {
        doSmth()
    } else {
        doAnotherSmth()
    }
}

