// RUN_PIPELINE_TILL: BACKEND
// JVM_DEFAULT_MODE: no-compatibility

@JvmDefaultWithCompatibility
interface A<T> {
    fun test(p: T) {}
}

@JvmDefaultWithCompatibility
class B : A<String> {}

/* GENERATED_FIR_TAGS: classDeclaration, functionDeclaration, interfaceDeclaration, nullableType, typeParameter */
