// RUN_PIPELINE_TILL: FRONTEND
// DIAGNOSTICS: -UNUSED_PARAMETER

class A<T>

operator fun <T> T.invoke(a: A<T>) {}

fun foo(s: String, ai: A<Int>) {
    1(ai)

    s(<!TYPE_MISMATCH!>ai<!>)

    ""(<!TYPE_MISMATCH!>ai<!>)
}

/* GENERATED_FIR_TAGS: classDeclaration, funWithExtensionReceiver, functionDeclaration, integerLiteral, nullableType,
operator, stringLiteral, typeParameter */
