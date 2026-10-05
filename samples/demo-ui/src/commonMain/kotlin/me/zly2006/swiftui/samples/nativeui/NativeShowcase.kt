package me.zly2006.swiftui.samples.nativeui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import me.zly2006.swiftui.nativeui.Alignment
import me.zly2006.swiftui.nativeui.Button
import me.zly2006.swiftui.nativeui.Color
import me.zly2006.swiftui.nativeui.Font
import me.zly2006.swiftui.nativeui.HStack
import me.zly2006.swiftui.nativeui.HorizontalAlignment
import me.zly2006.swiftui.nativeui.Image
import me.zly2006.swiftui.nativeui.Material
import me.zly2006.swiftui.nativeui.MaterialSurface
import me.zly2006.swiftui.nativeui.ProgressView
import me.zly2006.swiftui.nativeui.Slider
import me.zly2006.swiftui.nativeui.Text
import me.zly2006.swiftui.nativeui.TextField
import me.zly2006.swiftui.nativeui.Toggle
import me.zly2006.swiftui.nativeui.VStack
import me.zly2006.swiftui.nativeui.flexibleFrame
import me.zly2006.swiftui.nativeui.font
import me.zly2006.swiftui.nativeui.foregroundStyle
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
    VStack(
        spacing = 18.0,
        alignment = HorizontalAlignment.leading,
        modifier =
            Modifier
                .flexibleFrame(
                    maxWidth = Double.POSITIVE_INFINITY,
                    maxHeight = Double.POSITIVE_INFINITY,
                    alignment = Alignment.topLeading,
                ).padding(
                    32.0,
                ),
    ) {
        Text("SwiftUI Kotlin Binding", Modifier.font(30.0, Font.Weight.bold))
        Text("Compose Runtime → 有类型绑定 → 官方原生 UI", Modifier.foregroundStyle(Color.secondary).font(15.0))
        HStack(spacing = 12.0) {
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
            HStack(spacing = 24.0) {
                Image(systemName = "heart.fill", modifier = Modifier.foregroundStyle(Color.red).font(42.0))
                MaterialSurface(Material.regular, 14.0, Modifier.frame(width = 160.0, height = 60.0))
            }
        }
        Text("整个界面使用一个原生宿主；没有 Skia 画布。", Modifier.foregroundStyle(Color.secondary).font(13.0))
    }
}
