// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
class Foo<T>

class Bar<T> {
    fun <S : T> takeFoo(foo: Foo<in S>) {}
}

class Inv<P>

interface A

fun <K : Inv<T>, T> main() where T: A, T: Number {
    val foo = Foo<K>()
    Bar<Inv<out Int>>().takeFoo(foo) // error in 1.3.72, no error in 1.4.31
}

/* GENERATED_FIR_TAGS: classDeclaration, functionDeclaration, inProjection, interfaceDeclaration, intersectionType,
localProperty, nullableType, outProjection, propertyDeclaration, typeConstraint, typeParameter */
