// RUN_PIPELINE_TILL: BACKEND
// ISSUE: KT-56744
// SKIP_TXT

fun <T> T.myApply(block: T.() -> Unit): T = this

fun bar(): Int = 1

interface A : C
interface B : C
interface C {
    fun baz()
}

fun Any.foo() = myApply {
    when (this) {
        is A -> ::bar
        is B -> ::bar
        else -> throw RuntimeException()
    }

    <!UNRESOLVED_REFERENCE!>baz<!>() // Smart cast should work
}

/* GENERATED_FIR_TAGS: callableReference, funWithExtensionReceiver, functionDeclaration, functionalType, integerLiteral,
interfaceDeclaration, isExpression, lambdaLiteral, nullableType, smartcast, thisExpression, typeParameter,
typeWithExtension, whenExpression, whenWithSubject */
