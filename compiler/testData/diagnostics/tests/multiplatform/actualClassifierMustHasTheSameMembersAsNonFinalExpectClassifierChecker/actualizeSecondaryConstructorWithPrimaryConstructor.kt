// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// MODULE: m1-common
// FILE: common.kt

expect open class Foo {
    fun existingFun()
    val existingParam: Int

    constructor()
}

// MODULE: m2-jvm()()(m1-common)
// FILE: jvm.kt

actual open class Foo actual constructor() {
    actual fun existingFun() {}
    actual val existingParam: Int = 904
}

/* GENERATED_FIR_TAGS: actual, classDeclaration, expect, functionDeclaration, integerLiteral, primaryConstructor,
propertyDeclaration, secondaryConstructor */
