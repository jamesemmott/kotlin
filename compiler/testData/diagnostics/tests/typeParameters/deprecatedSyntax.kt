// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
fun foo<!DEPRECATED_TYPE_PARAMETER_SYNTAX!><T><!>() {
    fun bar<!DEPRECATED_TYPE_PARAMETER_SYNTAX!><T><!>() {}
}

/* GENERATED_FIR_TAGS: functionDeclaration, localFunction, nullableType, typeParameter */
