// RUN_PIPELINE_TILL: FRONTEND
// FILE: X.java

public class X {
    int getFoo() {return 3;}
}

// FILE: Usage.kt

class A : X() {
    // TODO: DEBUG_INFO_MISSING_UNRESOLVED indicates a bug here
    override fun getFoo() = <!DEBUG_INFO_MISSING_UNRESOLVED, TYPECHECKER_HAS_RUN_INTO_RECURSIVE_PROBLEM_ERROR!>foo<!>
}

/* GENERATED_FIR_TAGS: classDeclaration, functionDeclaration, javaProperty, javaType, override */
