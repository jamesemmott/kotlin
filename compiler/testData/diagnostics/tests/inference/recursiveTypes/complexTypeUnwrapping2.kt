// RUN_PIPELINE_TILL: BACKEND
// FIR_IDENTICAL
// ISSUE: KT-63487
// FIR_DUMP

abstract class AbstractField<out Field : AbstractField<Field>>

abstract class AbstractElement<Element : AbstractElement<Element, Field>, Field : AbstractField<Field>>

interface ElementOrRef<Element : AbstractElement<Element, Field>, Field : AbstractField<Field>> {
    val element: Element
}

fun elvis(x: ElementOrRef<*, *>?, y: ElementOrRef<*, *>?) {
    val xElement = x?.element
    val yElement = y?.element
    val e = xElement ?: yElement
}

/* GENERATED_FIR_TAGS: capturedType, classDeclaration, elvisExpression, functionDeclaration, interfaceDeclaration,
localProperty, nullableType, out, outProjection, propertyDeclaration, safeCall, starProjection, typeConstraint,
typeParameter */
