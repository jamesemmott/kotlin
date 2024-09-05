// DUMP_IR

// MODULE: ui
// MODULE_KIND: LibraryBinary
// FILE: com/example/ui/Text.kt
package com.example.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Text(text: String, modifier: Modifier) {}

// MODULE: myModule
// FILE: com/example/myModule/OtherModule.kt
@file:JvmName("SpecialName")
package com.example.myModule

class OtherModule {
    class Inner {
        inline fun giveMeString(): String {
            return secret()
        }

        @PublishedApi
        internal fun secret(): String {
            return "what is up!!!!!!!"
        }
    }
}

// MODULE: main(myModule, ui)
// FILE: main.kt
package home

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.myModule.OtherModule
import com.example.ui.Text

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "$name!" + OtherModule.Inner().giveMeString(),
        modifier = modifier
    )
}
