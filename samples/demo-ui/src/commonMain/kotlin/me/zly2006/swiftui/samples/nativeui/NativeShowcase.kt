package me.zly2006.swiftui.samples.nativeui

import androidx.compose.runtime.*
import me.zly2006.swiftui.nativeui.*

class NativeShowcaseState {
    var updates by mutableStateOf(0)
    var checked by mutableStateOf(false)
    var text by mutableStateOf("原生输入")
    var value by mutableStateOf(0.35)
    var visible by mutableStateOf(true)
}

@Composable
fun NativeShowcase(state: NativeShowcaseState) {
    Column(spacing = 18.0, alignment = HorizontalAlignment.Leading,
        modifier = NativeModifier.padding(32.0).flexibleFrame(maxWidth = Double.POSITIVE_INFINITY, maxHeight = Double.POSITIVE_INFINITY, alignment = Alignment.TopLeading)) {
        Text("SwiftUI Kotlin Binding", NativeModifier.font(30.0, FontWeight.Bold))
        Text("Compose Runtime → 有类型绑定 → 官方原生 UI", NativeModifier.font(15.0).foreground(NativeColor.Secondary))
        Row(spacing = 12.0) {
            Button(onClick = { state.updates++ }) { Text("更新 Kotlin 状态") }
            Text("更新 ${state.updates} 次")
        }
        if (state.visible) {
            Toggle(checked = state.checked, onCheckedChanged = { state.checked = it }) { Text("原生 Toggle") }
            TextField(text = state.text, prompt = "输入文字", onTextChanged = { state.text = it }, modifier = NativeModifier.frame(width = 300.0))
            Slider(value = state.value, onValueChanged = { state.value = it }, modifier = NativeModifier.frame(width = 300.0))
            ProgressView(state.value, modifier = NativeModifier.frame(width = 300.0))
            Row(spacing = 24.0) {
                SystemImage("heart.fill", NativeModifier.font(42.0).foreground(NativeColor.Red))
                MaterialSurface(Material.Regular, 14.0, NativeModifier.frame(width = 160.0, height = 60.0))
            }
        }
        Text("整个界面使用一个原生宿主；没有 Skia 画布。", NativeModifier.font(13.0).foreground(NativeColor.Secondary))
    }
}
