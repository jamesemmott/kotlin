// RUN_PIPELINE_TILL: FRONTEND
// LANGUAGE: +InlineClasses
// DIAGNOSTICS: -UNUSED_VARIABLE, -INLINE_CLASS_DEPRECATED
// FIR_IDENTICAL

inline class Foo(val x: Int)

<!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var a: Foo

fun foo() {
    <!INAPPLICABLE_LATEINIT_MODIFIER!>lateinit<!> var b: Foo
}

/* GENERATED_FIR_TAGS: classDeclaration, functionDeclaration, lateinit, localProperty, primaryConstructor,
propertyDeclaration */
