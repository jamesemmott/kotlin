// RUN_PIPELINE_TILL: BACKEND
// LANGUAGE: +AllowContractsForCustomFunctions +UseReturnsEffect
// OPT_IN: kotlin.contracts.ExperimentalContracts
// DIAGNOSTICS: -INVISIBLE_REFERENCE -INVISIBLE_MEMBER

import kotlin.contracts.*

fun myAssert(condition: Boolean, message: String = "") {
    contract {
        returns() implies (condition)
    }
    if (!condition) throw kotlin.IllegalArgumentException(message)
}

fun test(x: Any?) {
    myAssert(x is String)
    <!DEBUG_INFO_SMARTCAST!>x<!>.length
}

/* GENERATED_FIR_TAGS: contractConditionalEffect, contracts, functionDeclaration, ifExpression, isExpression,
lambdaLiteral, nullableType, smartcast, stringLiteral */
