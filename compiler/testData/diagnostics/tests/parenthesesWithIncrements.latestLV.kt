// RUN_PIPELINE_TILL: BACKEND
// ISSUE: KT-70507
// WITH_STDLIB
// LATEST_LV_DIFFERENCE

object O {
    operator fun inc() = this

    operator fun get(i: Int) = this
    operator fun set(i: Int, o: O) {}
}

fun main() {
    var b = O
    <!WRAPPED_LHS_IN_ASSIGNMENT_ERROR!>(b)<!>++

    <!WRAPPED_LHS_IN_ASSIGNMENT_ERROR!>(O[1])<!>++
    (O)[0]++
}

/* GENERATED_FIR_TAGS: assignment, functionDeclaration, incrementDecrementExpression, localProperty, objectDeclaration,
operator, propertyDeclaration, thisExpression */
