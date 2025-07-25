// RUN_PIPELINE_TILL: BACKEND
// LANGUAGE: +ContractSyntaxV2
// WITH_STDLIB

import kotlin.contracts.*

inline fun <reified T> requreIsInstance(value: Any) contract [
    returns() implies (value is T)
] {
    if (value !is T) throw IllegalArgumentException()
}

fun test_1(s: Any) {
    requreIsInstance<String>(s)
    s.length
}

inline fun <reified T> requreIsInstanceOf(value: Any, requiredValue: T) contract [
    returns() implies (value is T)
] {
    if (value !is T) throw IllegalArgumentException()
}

fun test_2(x: Any, s: String) {
    requreIsInstanceOf(x, s)
    x.length
}

/* GENERATED_FIR_TAGS: contractConditionalEffect, contracts, functionDeclaration, ifExpression, inline, isExpression,
nullableType, reified, smartcast, typeParameter */
