// RUN_PIPELINE_TILL: BACKEND
// FILE: JavaClass.java
import org.jetbrains.annotations.Nullable;

public class JavaClass {
    private String myFoo = "";
    public String getFoo() { return myFoo; }
    public void setFoo(@Nullable String s) { myFoo = s; }
}

// FILE: main.kt
fun main(j: JavaClass) {
    j.foo += "OK"
}

/* GENERATED_FIR_TAGS: additiveExpression, assignment, flexibleType, functionDeclaration, javaProperty, javaType,
localProperty, propertyDeclaration, stringLiteral */
