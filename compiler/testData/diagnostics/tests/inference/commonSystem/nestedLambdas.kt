// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
// DIAGNOSTICS: -UNUSED_PARAMETER
// CHECK_TYPE

fun <T, R> Collection<T>.map(transform : (T) -> R) : List<R> = throw Exception()

fun test(list: List<List<Int>>) {

    val list1 = list.map { it.map { "$it" } }
    list1 checkType { _<List<List<String>>>() }
}

/* GENERATED_FIR_TAGS: classDeclaration, funWithExtensionReceiver, functionDeclaration, functionalType, infix,
lambdaLiteral, localProperty, nullableType, propertyDeclaration, typeParameter, typeWithExtension */
