// RUN_PIPELINE_TILL: BACKEND
 interface Base {
     fun check()
 }


 class My {
     lateinit var delegate: Base

     fun check() = delegate.check() // Should not resolve
 }

/* GENERATED_FIR_TAGS: classDeclaration, functionDeclaration, interfaceDeclaration, lateinit, propertyDeclaration */
