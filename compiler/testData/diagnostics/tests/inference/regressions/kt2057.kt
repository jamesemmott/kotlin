// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
// DIAGNOSTICS: -UNUSED_PARAMETER

import java.util.ArrayList

fun <T> foo(a : T, b : Collection<T>, c : Int) {
}

fun <T> arrayListOf(vararg values: T): ArrayList<T> = throw Exception("$values")

val bar = foo("", <!NO_VALUE_FOR_PARAMETER!>arrayListOf(), )<!>
val bar2 = foo<String>("", <!NO_VALUE_FOR_PARAMETER!>arrayListOf(), )<!>

/* GENERATED_FIR_TAGS: functionDeclaration, nullableType, outProjection, propertyDeclaration, stringLiteral,
typeParameter, vararg */
