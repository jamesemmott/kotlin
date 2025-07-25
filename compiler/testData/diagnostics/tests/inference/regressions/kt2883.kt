// RUN_PIPELINE_TILL: FRONTEND
//KT-2883 Type inference fails due to non-Unit value returned
package a

public fun doAction(action : () -> Unit){
}

class Y<TItem>(val itemToString: (TItem) -> String){
}

fun <TItem> bar(context : Y<TItem>) : TItem{
<!NO_RETURN_IN_FUNCTION_WITH_BLOCK_BODY!>}<!>

fun foo(){
    val stringToString : (String) -> String = { it }
    doAction({bar(Y<String>(stringToString))})
}

fun <T> bar(t: T): T = t

fun test() {

    doAction { bar(12) }

    val u: Unit =  <!TYPE_MISMATCH, TYPE_MISMATCH!>bar(11)<!>
}

fun testWithoutInference(col: MutableCollection<Int>) {

    doAction { col.add(2) }

    val u: Unit = <!TYPE_MISMATCH!>col.add(2)<!>
}

/* GENERATED_FIR_TAGS: classDeclaration, functionDeclaration, functionalType, integerLiteral, lambdaLiteral,
localProperty, nullableType, primaryConstructor, propertyDeclaration, typeParameter */
