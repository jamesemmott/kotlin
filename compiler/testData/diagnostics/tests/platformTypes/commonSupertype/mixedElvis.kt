// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
// DIAGNOSTICS: -UNUSED_VARIABLE
// CHECK_TYPE

import java.util.ArrayList

fun foo(handlers: Array<MutableList<String>?>) {
    val v = handlers[0] ?: ArrayList<String>()
    handlers[0] = v
    val js: MutableList<String> = v
    // TODO: fix with dominance
//    v checkType { it : _<MutableList<String>>}
//    v checkType { it : _<MutableList<String?>>}
}

/* GENERATED_FIR_TAGS: assignment, classDeclaration, elvisExpression, flexibleType, funWithExtensionReceiver,
functionDeclaration, functionalType, infix, integerLiteral, javaFunction, localProperty, nullableType,
propertyDeclaration, typeParameter, typeWithExtension */
