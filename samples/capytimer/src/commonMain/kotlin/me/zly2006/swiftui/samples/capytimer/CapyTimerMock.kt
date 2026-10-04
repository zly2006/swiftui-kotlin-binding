package me.zly2006.swiftui.samples.capytimer

import androidx.compose.runtime.*
import me.zly2006.swiftui.nativeui.*

// UI adapted from CapyTimer by anvndev (@andev0x), MIT. See the sample LICENSE.
data class TodoItem(val id: Int, val title: String, val done: Boolean = false)
class CapyState {
    var remaining by mutableStateOf(1155)
    var running by mutableStateOf(false)
    var focusMinutes by mutableStateOf("25")
    var breakMinutes by mutableStateOf("5")
    var notes by mutableStateOf("Focus on one thing at a time.\n\nShip the native Kotlin UI mock.")
    var newTask by mutableStateOf("")
    var todos by mutableStateOf(listOf(TodoItem(1, "Review the native UI", true), TodoItem(2, "Ship the Kotlin mock")))
    var status by mutableStateOf("Up to Date")
    fun addTask() { if (newTask.isNotBlank()) { todos = todos + TodoItem((todos.maxOfOrNull { it.id } ?: 0) + 1, newTask.trim()); newTask = "" } }
    fun reset() { running = false; remaining = (focusMinutes.toIntOrNull() ?: 25).coerceAtLeast(1) * 60 }
}
private val border = NativeColor.Gray.opacity(0.25)
private fun cardModifier() = NativeModifier.padding(12.0).roundedBackground(NativeColor.WindowBackground, 12.0).roundedBorder(border, 12.0)
@Composable private fun Card(content: @Composable () -> Unit) { Column(spacing = 8.0, alignment = HorizontalAlignment.Leading, modifier = cardModifier(), content = content) }
@Composable private fun Heading(value: String) { Text(value, NativeModifier.semanticFont(TextStyle.Headline)) }
@Composable private fun TextButton(value: String, prominent: Boolean = false, click: () -> Unit) { StyledButton(prominent, click) { Text(value) } }
@Composable
fun CapyTimerMock(state: CapyState) {
    ScrollView {
        Column(spacing = 12.0, alignment = HorizontalAlignment.Leading, modifier = NativeModifier.defaultPadding().frame(width = 280.0)) {
            Card {
                Column(spacing = 10.0) {
                    Row(spacing = 12.0) {
                        Box(modifier = NativeModifier.frame(width = 54.0, height = 54.0)) {
                            CircleStroke(border, 8.0)
                            CircleStroke(NativeColor.AccentColor, 8.0, end = (1.0 - state.remaining / 1500.0).coerceIn(0.0, 1.0), roundCap = true, modifier = NativeModifier.rotation(-90.0))
                        }
                        Column(spacing = 2.0, alignment = HorizontalAlignment.Leading) {
                            Text("${(state.remaining / 60).toString().padStart(2,'0')}:${(state.remaining % 60).toString().padStart(2,'0')}", NativeModifier.font(28.0, FontWeight.Bold))
                            Text(if (state.running) "Running" else "Paused", NativeModifier.semanticFont(TextStyle.Caption).foreground(NativeColor.Secondary))
                        }
                        Spacer()
                    }
                    Row {
                        TextButton(if (state.running) "Pause" else "Start", true) { state.running = !state.running }
                        TextButton("Reset") { state.reset() }
                    }
                }
            }
            Card {
                Heading("Todo List")
                Column(spacing = 8.0, alignment = HorizontalAlignment.Leading, modifier = NativeModifier.padding(top = 2.0)) {
                    state.todos.forEach { todo -> key(todo.id) {
                        Row(spacing = 8.0, modifier = NativeModifier.padding(top = 4.0, bottom = 4.0)) {
                            Button(plain = true, onClick = { state.todos = state.todos.map { if (it.id == todo.id) it.copy(done = !it.done) else it } }) {
                                SystemImage(if (todo.done) "checkmark.circle.fill" else "circle", NativeModifier.foreground(if (todo.done) NativeColor.AccentColor else NativeColor.Secondary))
                            }
                            Text(todo.title, NativeModifier.strikeThrough(todo.done).foreground(if (todo.done) NativeColor.Secondary else NativeColor.Primary).lineLimit(1.0))
                            Spacer()
                            Button(plain = true, onClick = { state.todos = state.todos.filterNot { it.id == todo.id } }) { SystemImage("trash") }
                        }
                    } }
                    Row(spacing = 8.0) {
                        TextField(state.newTask, "New task...", { state.newTask = it }, NativeModifier.roundedTextField().frame(height = 26.0))
                        TextButton("Add") { state.addTask() }
                    }
                }
            }
            Card {
                Heading("Notes")
                Column(spacing = 8.0, alignment = HorizontalAlignment.Leading, modifier = NativeModifier.padding(top = 2.0)) {
                    TextEditor(state.notes, { state.notes = it }, NativeModifier.frame(height = 100.0).padding(6.0).roundedBackground(NativeColor.WindowBackground, 8.0).roundedBorder(border, 8.0))
                }
            }
            Card {
                Heading("Settings")
                Column(spacing = 8.0, alignment = HorizontalAlignment.Leading) {
                    Row { Text("Focus (min)", NativeModifier.frame(width = 90.0, alignment = Alignment.Leading)); TextField(state.focusMinutes, "25", { state.focusMinutes = it }, NativeModifier.frame(width = 60.0).roundedTextField().frame(height = 26.0)) }
                    Row { Text("Break (min)", NativeModifier.frame(width = 90.0, alignment = Alignment.Leading)); TextField(state.breakMinutes, "5", { state.breakMinutes = it }, NativeModifier.frame(width = 60.0).roundedTextField().frame(height = 26.0)) }
                    Row(spacing = 8.0, modifier = NativeModifier.padding(top = 4.0)) {
                        TextButton("Save", true) { state.reset() }
                        TextButton("Reset Defaults") { state.focusMinutes = "25"; state.breakMinutes = "5"; state.reset() }
                    }
                    Divider(NativeModifier.padding(top = 8.0, bottom = 8.0))
                    Row { Text("Update Settings", NativeModifier.semanticFont(TextStyle.Subheadline).weight(FontWeight.Medium)); Spacer(); TextButton("Configure") { state.status = "Mock settings" } }
                }
            }
            Card {
                Heading("Updates")
                Column(spacing = 12.0, alignment = HorizontalAlignment.Leading) {
                    Row {
                        SystemImage("arrow.clockwise.circle", NativeModifier.foreground(NativeColor.AccentColor).semanticFont(TextStyle.Title2))
                        Column(spacing = 2.0, alignment = HorizontalAlignment.Leading) { Heading("Updates"); Text(state.status, NativeModifier.semanticFont(TextStyle.Caption).foreground(NativeColor.Green)) }
                        Spacer(); TextButton("Check") { state.status = "Up to Date" }
                    }
                }
            }
        }
    }
}
