// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
// DIAGNOSTICS: -UNNECESSARY_NOT_NULL_ASSERTION
// See KT-9126: Variable change does not affect data flow info for its fields

class My(val x: Int?)

fun foo() {
    var y: My? = My(42)
    if (y!!.x != null) {
        y = My(null)
        y!!.x<!UNSAFE_CALL!>.<!>hashCode()
    }
}

/* GENERATED_FIR_TAGS: assignment, checkNotNullCall, classDeclaration, equalityExpression, functionDeclaration,
ifExpression, integerLiteral, localProperty, nullableType, primaryConstructor, propertyDeclaration, smartcast */
