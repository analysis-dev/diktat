package test.paragraph3.newlines

fun foo(list: List<Bar>?) {
    list!!.filterNotNull().map { it.baz() }.firstOrNull {
        it.condition()
    }?.qux()
            ?:
            foobar
}

fun bar(x :Int,y:Int) :Int {
    return   x+ y }

fun goo() {
    x.map().gro()
            .gh()
    t.map().hg().hg()
    t
            .map()
            .filter()
            .takefirst()
    x
            .map()
            .filter().hre()
}