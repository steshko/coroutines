package dev.steshko.playground.compose.html

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import org.jetbrains.compose.web.dom.Button
import org.jetbrains.compose.web.dom.Div
import org.jetbrains.compose.web.dom.Text
import org.jetbrains.compose.web.renderComposable

fun main() {
    renderComposable(rootElementId = "root") {
        Div {
            Text("Hello, Compose Web!")
        }
        Counter()
    }
}

@Composable
fun Counter() {
    var count by remember { mutableStateOf(0) }
    Button(
        attrs = {
            onClick { count++ }
        }
    ){
        Text("Count: $count")
    }
}
