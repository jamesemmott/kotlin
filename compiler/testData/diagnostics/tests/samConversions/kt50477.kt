// RUN_PIPELINE_TILL: BACKEND
// LANGUAGE: -SuspendOnlySamConversions

fun interface FI {
    suspend fun call() // suspending now(!!!)
}

fun accept(fi: FI): Int = TODO()

suspend fun foo() {}
fun foo2() {}

fun main() {
    val fi: suspend () -> Unit = {} // Lambda of a suspending(!!!) functional type
    accept(<!TYPE_MISMATCH!>fi<!>) // ERROR: Type mismatch. Required: FI Found: suspend () → Unit

    accept(::foo)
    val x1 = ::foo
    accept(<!TYPE_MISMATCH!>x1<!>) // ERROR: Type mismatch. Required: FI Found: suspend () → Unit

    accept(::foo2)
    val x2 = ::foo2
    accept(x2)
}

/* GENERATED_FIR_TAGS: callableReference, funInterface, functionDeclaration, functionalType, interfaceDeclaration,
lambdaLiteral, localProperty, propertyDeclaration, samConversion, suspend */
