// RUN_PIPELINE_TILL: FRONTEND
open class A {
    fun f() {}
}

class B : <!SUPERTYPE_NOT_INITIALIZED!>A<!> {
    fun g() {
        super<<!NOT_A_SUPERTYPE!>String<!>>.f()
        super<A>.f()
    }
}

/* GENERATED_FIR_TAGS: classDeclaration, functionDeclaration, superExpression */
