// RUN_PIPELINE_TILL: BACKEND
// DIAGNOSTICS: +UNUSED_EXPRESSION
// WITH_EXTRA_CHECKERS

val flag = true

// type of lambda was checked by txt
val a = b@ { // () -> Unit
    if (flag) return@b
    else <!UNUSED_EXPRESSION!>54<!>
}

/* GENERATED_FIR_TAGS: ifExpression, integerLiteral, lambdaLiteral, propertyDeclaration */
