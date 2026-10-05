package me.zly2006.swiftui.samples.nativeui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import me.zly2006.swiftui.nativeui.Alignment
import me.zly2006.swiftui.nativeui.Button
import me.zly2006.swiftui.nativeui.Column
import me.zly2006.swiftui.nativeui.FontWeight
import me.zly2006.swiftui.nativeui.HorizontalAlignment
import me.zly2006.swiftui.nativeui.Material
import me.zly2006.swiftui.nativeui.MaterialSurface
import me.zly2006.swiftui.nativeui.NativeColor
import me.zly2006.swiftui.nativeui.ProgressView
import me.zly2006.swiftui.nativeui.Row
import me.zly2006.swiftui.nativeui.Slider
import me.zly2006.swiftui.nativeui.SystemImage
import me.zly2006.swiftui.nativeui.Text
import me.zly2006.swiftui.nativeui.TextField
import me.zly2006.swiftui.nativeui.Toggle
import me.zly2006.swiftui.nativeui.flexibleFrame
import me.zly2006.swiftui.nativeui.font
import me.zly2006.swiftui.nativeui.foreground
import me.zly2006.swiftui.nativeui.frame
import me.zly2006.swiftui.nativeui.padding

class NativeShowcaseState {
    var updates by mutableStateOf(0)
    var checked by mutableStateOf(false)
    var text by mutableStateOf("原生输入")
    var value by mutableStateOf(0.35)
    var visible by mutableStateOf(true)
}

@Composable
fun NativeShowcase(state: NativeShowcaseState) {
    Column(
        spacing = 18.0,
        alignment = HorizontalAlignment.Leading,
        modifier =
            Modifier
                .flexibleFrame(
                    maxWidth = Double.POSITIVE_INFINITY,
                    maxHeight = Double.POSITIVE_INFINITY,
                    alignment = Alignment.TopLeading,
                ).padding(
                    32.0,
                ),
    ) {
        Text("SwiftUI Kotlin Binding", Modifier.font(30.0, FontWeight.Bold))
        Text("Compose Runtime → 有类型绑定 → 官方原生 UI", Modifier.foreground(NativeColor.Secondary).font(15.0))
        Row(spacing = 12.0) {
            Button(onClick = { state.updates++ }) { Text("更新 Kotlin 状态") }
            Text("更新 ${state.updates} 次")
        }
        if (state.visible) {
            Toggle(checked = state.checked, onCheckedChanged = { state.checked = it }) { Text("原生 Toggle") }
            TextField(
                text = state.text,
                prompt = "输入文字",
                onTextChanged = { state.text = it },
                modifier = Modifier.frame(width = 300.0),
            )
            Slider(value = state.value, onValueChanged = { state.value = it }, modifier = Modifier.frame(width = 300.0))
            ProgressView(state.value, modifier = Modifier.frame(width = 300.0))
            Row(spacing = 24.0) {
                SystemImage("heart.fill", Modifier.foreground(NativeColor.Red).font(42.0))
                MaterialSurface(Material.Regular, 14.0, Modifier.frame(width = 160.0, height = 60.0))
            }
        }
        Text("整个界面使用一个原生宿主；没有 Skia 画布。", Modifier.foreground(NativeColor.Secondary).font(13.0))
    }
}
