// RUN_PIPELINE_TILL: FRONTEND
// FIR_IDENTICAL
// DIAGNOSTICS: -MISSING_CONSTRUCTOR_KEYWORD

class C(val a: String) {}

interface T1<!CONSTRUCTOR_IN_INTERFACE!>(val x: String)<!> {}

interface T2 <!CONSTRUCTOR_IN_INTERFACE!>constructor()<!> {}

interface T3 private <!CONSTRUCTOR_IN_INTERFACE!>constructor(a: Int)<!> {}

interface T4 {
    <!CONSTRUCTOR_IN_INTERFACE!>constructor(a: Int)<!> {
        val b: Int = 1
    }
}

interface T5 private <!CONSTRUCTOR_IN_INTERFACE!>()<!> : T4 {}
interface T6 <!CONSTRUCTOR_IN_INTERFACE!>private<!><!SYNTAX!><!> : T5 {}

/* GENERATED_FIR_TAGS: classDeclaration, integerLiteral, interfaceDeclaration, localProperty, primaryConstructor,
propertyDeclaration, secondaryConstructor */
