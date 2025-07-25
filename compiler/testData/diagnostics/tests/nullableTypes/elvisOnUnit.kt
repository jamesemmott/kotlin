// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
class My {
    fun other() {}
}

fun another() {}

fun foo(x: My?) {
    // Both elvis parts should be alive
    // See also KT-7936, KT-8347
    x?.other() ?: another()
}

/* GENERATED_FIR_TAGS: classDeclaration, elvisExpression, functionDeclaration, nullableType, safeCall */
