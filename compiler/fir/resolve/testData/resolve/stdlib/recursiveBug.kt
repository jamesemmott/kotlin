class Foo(name: () -> String) {
    val result = run { name() }

    val name = result.length
}

class Bar(name: () -> String) {
    val name by lazy { name() }
}

fun bar(name: () -> String) {
    val result = run { name() }

    val name = result.length
}