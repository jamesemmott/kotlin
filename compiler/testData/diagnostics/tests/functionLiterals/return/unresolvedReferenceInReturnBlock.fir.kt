// RUN_PIPELINE_TILL: FRONTEND
val a = l@ {
    return@l <!UNRESOLVED_REFERENCE!>r<!>
}

val b = l@ {
    if ("" == "OK") return@l

    return@l <!UNRESOLVED_REFERENCE!>r<!>
}

/* GENERATED_FIR_TAGS: equalityExpression, ifExpression, lambdaLiteral, propertyDeclaration, stringLiteral */
