// RUN_PIPELINE_TILL: BACKEND
// FILE: A.java
public class A<T extends A> {}

// FILE: 1.kt
<!FINITE_BOUNDS_VIOLATION_IN_JAVA!>class B<!><S: A<*>>

/* GENERATED_FIR_TAGS: classDeclaration, javaType, starProjection, typeConstraint, typeParameter */
