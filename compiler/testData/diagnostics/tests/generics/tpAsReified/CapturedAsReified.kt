// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
// DIAGNOSTICS: -UNUSED_PARAMETER
class A<F>

inline fun <reified T> foo(x: A<T>) {}

fun test(x: A<out CharSequence>) {
    <!REIFIED_TYPE_FORBIDDEN_SUBSTITUTION!>foo<!>(x)
}

/* GENERATED_FIR_TAGS: capturedType, classDeclaration, functionDeclaration, inline, nullableType, outProjection, reified,
typeParameter */
