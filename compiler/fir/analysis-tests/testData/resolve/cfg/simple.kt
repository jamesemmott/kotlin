// RUN_PIPELINE_TILL: BACKEND
// DUMP_CFG
fun foo() {}

fun test() {
    val x = 1
    val y = x + 1
    foo()
}

/* GENERATED_FIR_TAGS: additiveExpression, functionDeclaration, integerLiteral, localProperty, propertyDeclaration */
