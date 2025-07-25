// IGNORE_FIR_DIAGNOSTICS
// RUN_PIPELINE_TILL: FIR2IR
// LANGUAGE: -SupportEffectivelyFinalInExpectActualVisibilityCheck
// MODULE: m1-common
// FILE: common.kt

open class Base {
    internal open fun foo() {}
}
expect class Foo : Base {
    override fun foo()
}

// MODULE: m2-jvm()()(m1-common)
// FILE: jvm.kt

actual class Foo : Base() {
    <!ACTUAL_WITHOUT_EXPECT!>public<!> actual override fun foo() {
    }
}

/* GENERATED_FIR_TAGS: actual, classDeclaration, expect, functionDeclaration, override */
