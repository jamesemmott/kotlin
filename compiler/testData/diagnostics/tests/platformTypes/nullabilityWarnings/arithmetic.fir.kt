// RUN_PIPELINE_TILL: FRONTEND
// FILE: J.java

import org.jetbrains.annotations.*;

public class J {
    @NotNull
    public static Integer staticNN;
    @Nullable
    public static Integer staticN;
    public static Integer staticJ;
}

// FILE: k.kt

fun test() {
    // @NotNull platform type
    var platformNN = J.staticNN
    // @Nullable platform type
    var platformN = J.staticN
    // platform type with no annotation
    var platformJ = J.staticJ

    +platformNN
    <!UNSAFE_CALL!>+<!>platformN
    +platformJ

    ++platformNN
    <!UNSAFE_CALL!>++<!>platformN
    ++platformJ

    platformNN++
    platformN++
    platformJ++

    1 + platformNN
    1 + platformN
    1 + platformJ

    platformNN + 1
    platformN + 1
    platformJ + 1

    1 <!INFIX_MODIFIER_REQUIRED!>plus<!> platformNN
    1 <!INFIX_MODIFIER_REQUIRED!>plus<!> platformN
    1 <!INFIX_MODIFIER_REQUIRED!>plus<!> platformJ

    platformNN <!INFIX_MODIFIER_REQUIRED!>plus<!> 1
    platformN <!INFIX_MODIFIER_REQUIRED!>plus<!> 1
    platformJ <!INFIX_MODIFIER_REQUIRED!>plus<!> 1

    platformNN += 1
    platformN += 1
    platformJ += 1
}

/* GENERATED_FIR_TAGS: additiveExpression, assignment, flexibleType, functionDeclaration, incrementDecrementExpression,
integerLiteral, javaProperty, localProperty, nullableType, propertyDeclaration, smartcast, unaryExpression */
