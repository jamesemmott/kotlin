// IGNORE_FIR_DIAGNOSTICS
// RUN_PIPELINE_TILL: BACKEND
// MODULE: m1-common
// FILE: common.kt

expect class A {
    fun foo(a: Int): Int
}

// MODULE: m2-jvm()()(m1-common)
// FILE: jvm.kt

interface FooProvider {
    fun foo(a: Int = 2): Int = 42
}

actual class <!EXPECT_ACTUAL_INCOMPATIBLE_CLASS_SCOPE!>A<!> : FooProvider

/* GENERATED_FIR_TAGS: actual, classDeclaration, expect, functionDeclaration, integerLiteral, interfaceDeclaration */
