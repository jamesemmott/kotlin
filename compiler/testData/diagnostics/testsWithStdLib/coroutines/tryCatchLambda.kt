// RUN_PIPELINE_TILL: FRONTEND
// DIAGNOSTICS: -UNUSED_PARAMETER -ASSIGNED_BUT_NEVER_ACCESSED_VARIABLE -UNUSED_VALUE -UNUSED_VARIABLE

suspend fun <V> await(f: V): V = f

fun <T> genericBuilder(c: suspend () -> T): T = null!!

fun foo() {
    var result = ""
    genericBuilder<String> {
        <!TYPE_MISMATCH!>try {
            await("")
        } catch(e: Exception) {
            result = "fail"
        }<!>
    }
}

/* GENERATED_FIR_TAGS: assignment, checkNotNullCall, functionDeclaration, functionalType, lambdaLiteral, localProperty,
nullableType, propertyDeclaration, stringLiteral, suspend, tryExpression, typeParameter */
