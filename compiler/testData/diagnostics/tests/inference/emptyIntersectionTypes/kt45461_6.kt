// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
class Foo<T>

class Bar<T> {
    fun <S : T> takeFoo(foo: Foo<in S>) {}
}

fun <K : L, L : N, N: Number> main() {
    val foo = Foo<K>()
    Bar<Int>().takeFoo(foo) // error in 1.3.72, no error in 1.4.31
}

/* GENERATED_FIR_TAGS: classDeclaration, functionDeclaration, inProjection, intersectionType, localProperty,
nullableType, propertyDeclaration, typeConstraint, typeParameter */
