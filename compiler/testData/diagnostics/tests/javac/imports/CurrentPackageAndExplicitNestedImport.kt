// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// FILE: a/X.java
package a;

public class X {
    public void test() {}

    public static class Y {}

}

// FILE: b/Y.java
package b;

public class Y {}

// FILE: b/T.java
package b;

import a.X.Y;

public class T {

    public Y getY() { return null; }

}

// FILE: b/b.kt
package b

fun test() = T().getY()

/* GENERATED_FIR_TAGS: flexibleType, functionDeclaration, javaFunction, javaType */
