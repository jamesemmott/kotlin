// RUN_PIPELINE_TILL: FRONTEND
// DIAGNOSTICS: -UNUSED_VARIABLE
// MODULE: m1
// FILE: a.kt

package p

public class A
public class B {
    public val a: A = A()
}

// MODULE: m2(m1)
// FILE: b.kt

import p.*

class A

fun test() {
    val a: A = <!INITIALIZER_TYPE_MISMATCH!>B().a<!>
}

/* GENERATED_FIR_TAGS: classDeclaration, functionDeclaration, localProperty, propertyDeclaration */
