package me.zly2006.swiftui.samples.liquidglass
import me.zly2006.swiftui.samples.host.runSample
fun main(args: Array<String>) { val state = GlassState(); if(args.any { it.startsWith("--snapshot=") }) { state.fixedHover = true; state.hovered = true }; runSample(args, "Liquid Glass · Kotlin Native", 1180.0, 760.0, true) { dark -> LiquidGlassMock(state, dark) } }
