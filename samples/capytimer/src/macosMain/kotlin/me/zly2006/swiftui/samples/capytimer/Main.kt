package me.zly2006.swiftui.samples.capytimer
import me.zly2006.swiftui.samples.host.runSample

fun main(args: Array<String>) {
    val state = CapyState()
    runSample(args, "CapyTimer · Kotlin Native", 280.0, 850.0) { CapyTimerMock(state) }
}
