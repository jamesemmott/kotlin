
// IGNORE_INLINER: IR
// FILE: test.kt

fun box() {
    baz(foo())
    val a = foo()
    baz(a)
}
// FILE: test1.kt
// aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa
inline fun foo() = {
}
//FILE: test3.kt
fun baz(v:(() -> Unit)) {
    v()
}

// EXPECTATIONS JVM_IR
// test.kt:6 box
// test1.kt:12 box
// test1.kt:13 box
// test.kt:6 box
// test3.kt:16 baz
// test1.kt:13 invoke
// test3.kt:16 baz
// test3.kt:17 baz
// test.kt:7 box
// test1.kt:12 box
// test1.kt:13 box
// test.kt:7 box
// test.kt:8 box
// test3.kt:16 baz
// test1.kt:13 invoke
// test3.kt:16 baz
// test3.kt:17 baz
// test.kt:9 box

// EXPECTATIONS JS_IR
// test1.kt:12 box
// test.kt:6 box
// test3.kt:16 baz
// test1.kt:10 box$lambda
// test3.kt:17 baz
// test1.kt:12 box
// test.kt:8 box
// test3.kt:16 baz
// test1.kt:10 box$lambda
// test3.kt:17 baz
// test.kt:9 box

// EXPECTATIONS WASM
// test.kt:6 $box (8)
// test1.kt:13 $box (1)
// test.kt:6 $box (4)
// test3.kt:16 $baz (4)

// EXPECTATIONS ClassicFrontend WASM
// test1.kt:13 $box$lambda.invoke (0)

// EXPECTATIONS FIR WASM
// test1.kt:13 $box$lambda.invoke (1)

// EXPECTATIONS WASM
// test3.kt:16 $baz (4)
// test3.kt:17 $baz (1)
// test.kt:7 $box (12)
// test1.kt:13 $box (1)
// test.kt:8 $box (8, 4)
// test3.kt:16 $baz (4)

// EXPECTATIONS ClassicFrontend WASM
// test1.kt:13 $box$lambda.invoke (0)

// EXPECTATIONS FIR WASM
// test1.kt:13 $box$lambda.invoke (1)

// EXPECTATIONS WASM
// test3.kt:16 $baz (4)
// test3.kt:17 $baz (1)
// test.kt:9 $box (1)
