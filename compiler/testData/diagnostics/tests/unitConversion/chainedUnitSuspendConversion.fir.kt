// RUN_PIPELINE_TILL: FRONTEND
// LANGUAGE: +UnitConversionsOnArbitraryExpressions +SuspendConversion
// DIAGNOSTICS: -UNUSED_PARAMETER -UNUSED_EXPRESSION

fun foo(f: suspend () -> Unit) {}

fun bar(): String = ""

abstract class SubInt : () -> Int

fun test(g: () -> Double, s: SubInt) {
    foo(::bar)
    foo(<!ARGUMENT_TYPE_MISMATCH!>g<!>)
    foo(<!ARGUMENT_TYPE_MISMATCH!>s<!>)
}

/* GENERATED_FIR_TAGS: callableReference, classDeclaration, functionDeclaration, functionalType, stringLiteral, suspend */
