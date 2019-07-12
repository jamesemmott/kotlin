class Foo(name: () -> String) {
    val name = run { name() }.length
}