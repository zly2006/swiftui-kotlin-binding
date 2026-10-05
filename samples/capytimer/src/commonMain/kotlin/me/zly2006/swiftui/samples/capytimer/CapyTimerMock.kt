package me.zly2006.swiftui.samples.capytimer

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import me.zly2006.swiftui.nativeui.Alignment
import me.zly2006.swiftui.nativeui.Button
import me.zly2006.swiftui.nativeui.CircleStroke
import me.zly2006.swiftui.nativeui.Color
import me.zly2006.swiftui.nativeui.Divider
import me.zly2006.swiftui.nativeui.Font
import me.zly2006.swiftui.nativeui.HStack
import me.zly2006.swiftui.nativeui.HorizontalAlignment
import me.zly2006.swiftui.nativeui.Image
import me.zly2006.swiftui.nativeui.ScrollView
import me.zly2006.swiftui.nativeui.Spacer
import me.zly2006.swiftui.nativeui.StyledButton
import me.zly2006.swiftui.nativeui.Text
import me.zly2006.swiftui.nativeui.TextEditor
import me.zly2006.swiftui.nativeui.TextField
import me.zly2006.swiftui.nativeui.VStack
import me.zly2006.swiftui.nativeui.ZStack
import me.zly2006.swiftui.nativeui.font
import me.zly2006.swiftui.nativeui.fontWeight
import me.zly2006.swiftui.nativeui.foregroundStyle
import me.zly2006.swiftui.nativeui.frame
import me.zly2006.swiftui.nativeui.lineLimit
import me.zly2006.swiftui.nativeui.opacity
import me.zly2006.swiftui.nativeui.padding
import me.zly2006.swiftui.nativeui.rotationEffect
import me.zly2006.swiftui.nativeui.roundedBackground
import me.zly2006.swiftui.nativeui.roundedBorder
import me.zly2006.swiftui.nativeui.strikeThrough
import me.zly2006.swiftui.nativeui.textFieldStyle

// UI adapted from CapyTimer by anvndev (@andev0x), MIT. See the sample LICENSE.
data class TodoItem(
    val id: Int,
    val title: String,
    val done: Boolean = false,
)

class CapyState {
    var remaining by mutableStateOf(1155)
    var running by mutableStateOf(false)
    var focusMinutes by mutableStateOf("25")
    var breakMinutes by mutableStateOf("5")
    var notes by mutableStateOf("Focus on one thing at a time.\n\nShip the native Kotlin UI mock.")
    var newTask by mutableStateOf("")
    var todos by mutableStateOf(listOf(TodoItem(1, "Review the native UI", true), TodoItem(2, "Ship the Kotlin mock")))
    var status by mutableStateOf("Up to Date")

    fun addTask() {
        if (newTask.isNotBlank()) {
            todos = todos + TodoItem((todos.maxOfOrNull { it.id } ?: 0) + 1, newTask.trim())
            newTask =
                ""
        }
    }

    fun reset() {
        running = false
        remaining = (focusMinutes.toIntOrNull() ?: 25).coerceAtLeast(1) * 60
    }
}

private val border = Color.gray.opacity(0.25)

private fun cardModifier() = Modifier.roundedBorder(border, 12.0).roundedBackground(Color.windowBackground, 12.0).padding(12.0)

@Composable private fun Card(content: @Composable () -> Unit) {
    VStack(spacing = 8.0, alignment = HorizontalAlignment.leading, modifier = cardModifier(), content = content)
}

@Composable private fun Heading(value: String) {
    Text(value, Modifier.font(Font.TextStyle.headline))
}

@Composable private fun TextButton(
    value: String,
    prominent: Boolean = false,
    click: () -> Unit,
) {
    StyledButton(prominent, click) { Text(value) }
}

@Composable
fun CapyTimerMock(state: CapyState) {
    ScrollView {
        VStack(spacing = 12.0, alignment = HorizontalAlignment.leading, modifier = Modifier.frame(width = 280.0).padding()) {
            Card {
                VStack(spacing = 10.0) {
                    HStack(spacing = 12.0) {
                        ZStack(modifier = Modifier.frame(width = 54.0, height = 54.0)) {
                            CircleStroke(border, 8.0)
                            CircleStroke(
                                Color.accentColor,
                                8.0,
                                end = (1.0 - state.remaining / 1500.0).coerceIn(0.0, 1.0),
                                roundCap = true,
                                modifier = Modifier.rotationEffect(-90.0),
                            )
                        }
                        VStack(spacing = 2.0, alignment = HorizontalAlignment.leading) {
                            Text(
                                "${(state.remaining / 60).toString().padStart(2,'0')}:${(state.remaining % 60).toString().padStart(2,'0')}",
                                Modifier.font(28.0, Font.Weight.bold),
                            )
                            Text(
                                if (state.running) "Running" else "Paused",
                                Modifier.foregroundStyle(Color.secondary).font(Font.TextStyle.caption),
                            )
                        }
                        Spacer()
                    }
                    HStack {
                        TextButton(if (state.running) "Pause" else "Start", true) { state.running = !state.running }
                        TextButton("Reset") { state.reset() }
                    }
                }
            }
            Card {
                Heading("Todo List")
                VStack(spacing = 8.0, alignment = HorizontalAlignment.leading, modifier = Modifier.padding(top = 2.0)) {
                    state.todos.forEach { todo ->
                        key(todo.id) {
                            HStack(spacing = 8.0, modifier = Modifier.padding(top = 4.0, bottom = 4.0)) {
                                Button(plain = true, onClick = {
                                    state.todos =
                                        state.todos.map { if (it.id == todo.id) it.copy(done = !it.done) else it }
                                }) {
                                    Image(
                                        systemName = if (todo.done) "checkmark.circle.fill" else "circle",
                                        modifier = Modifier.foregroundStyle(if (todo.done) Color.accentColor else Color.secondary),
                                    )
                                }
                                Text(
                                    todo.title,
                                    Modifier
                                        .lineLimit(
                                            1.0,
                                        ).foregroundStyle(if (todo.done) Color.secondary else Color.primary)
                                        .strikeThrough(
                                            todo.done,
                                        ),
                                )
                                Spacer()
                                Button(
                                    plain = true,
                                    onClick = { state.todos = state.todos.filterNot { it.id == todo.id } },
                                ) { Image(systemName = "trash") }
                            }
                        }
                    }
                    HStack(spacing = 8.0) {
                        TextField(
                            state.newTask,
                            "New task...",
                            { state.newTask = it },
                            Modifier.frame(height = 26.0).textFieldStyle(),
                        )
                        TextButton("Add") { state.addTask() }
                    }
                }
            }
            Card {
                Heading("Notes")
                VStack(spacing = 8.0, alignment = HorizontalAlignment.leading, modifier = Modifier.padding(top = 2.0)) {
                    TextEditor(
                        state.notes,
                        {
                            state.notes = it
                        },
                        Modifier.roundedBorder(border, 8.0).roundedBackground(Color.windowBackground, 8.0).padding(6.0).frame(
                            height = 100.0,
                        ),
                    )
                }
            }
            Card {
                Heading("Settings")
                VStack(spacing = 8.0, alignment = HorizontalAlignment.leading) {
                    HStack {
                        Text("Focus (min)", Modifier.frame(width = 90.0, alignment = Alignment.leading))
                        TextField(state.focusMinutes, "25", {
                            state.focusMinutes =
                                it
                        }, Modifier.frame(height = 26.0).textFieldStyle().frame(width = 60.0))
                    }
                    HStack {
                        Text("Break (min)", Modifier.frame(width = 90.0, alignment = Alignment.leading))
                        TextField(state.breakMinutes, "5", {
                            state.breakMinutes =
                                it
                        }, Modifier.frame(height = 26.0).textFieldStyle().frame(width = 60.0))
                    }
                    HStack(spacing = 8.0, modifier = Modifier.padding(top = 4.0)) {
                        TextButton("Save", true) { state.reset() }
                        TextButton("Reset Defaults") {
                            state.focusMinutes = "25"
                            state.breakMinutes = "5"
                            state.reset()
                        }
                    }
                    Divider(Modifier.padding(top = 8.0, bottom = 8.0))
                    HStack {
                        Text("Update Settings", Modifier.fontWeight(Font.Weight.medium).font(Font.TextStyle.subheadline))
                        Spacer()
                        TextButton("Configure") {
                            state.status =
                                "Mock settings"
                        }
                    }
                }
            }
            Card {
                Heading("Updates")
                VStack(spacing = 12.0, alignment = HorizontalAlignment.leading) {
                    HStack {
                        Image(
                            systemName = "arrow.clockwise.circle",
                            modifier = Modifier.font(Font.TextStyle.title2).foregroundStyle(Color.accentColor),
                        )
                        VStack(spacing = 2.0, alignment = HorizontalAlignment.leading) {
                            Heading("Updates")
                            Text(state.status, Modifier.foregroundStyle(Color.green).font(Font.TextStyle.caption))
                        }
                        Spacer()
                        TextButton("Check") { state.status = "Up to Date" }
                    }
                }
            }
        }
    }
}
