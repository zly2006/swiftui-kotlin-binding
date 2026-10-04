@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlin.native.runtime.NativeRuntimeApi::class)
package me.zly2006.swiftui.samples.nativeapp

import kotlinx.cinterop.autoreleasepool
import me.zly2006.swiftui.nativeui.*
import me.zly2006.swiftui.samples.nativeui.*
import platform.AppKit.*
import platform.Foundation.*
import platform.darwin.NSObject

private class WindowDelegate : NSObject(), NSWindowDelegateProtocol {
    override fun windowWillClose(notification: NSNotification) { NSApplication.sharedApplication().stop(null) }
}
private var activeDelegate: WindowDelegate? = null
private fun settle(seconds: Double = 1.5) {
    val deadline = NSDate.dateWithTimeIntervalSinceNow(seconds)
    while (NSDate().compare(deadline) == NSOrderedAscending) NSRunLoop.mainRunLoop.runUntilDate(NSDate.dateWithTimeIntervalSinceNow(0.02))
}

private fun runWindow(args: Array<String>) = autoreleasepool {
    val output = args.firstOrNull { it.startsWith("--snapshot=") }?.substringAfter('=')
    val app = NSApplication.sharedApplication()
    app.setActivationPolicy(if (output == null) NSApplicationActivationPolicy.NSApplicationActivationPolicyRegular else NSApplicationActivationPolicy.NSApplicationActivationPolicyProhibited)
    app.finishLaunching()
    val state = NativeShowcaseState()
    val backend = MacosNativeUiBackend()
    val composition = NativeComposition(backend)
    composition.setContent { NativeShowcase(state) }
    val host = backend.createHost(composition.rootElement)
    val window = NSWindow(NSMakeRect(0.0, 0.0, 850.0, 530.0), NSWindowStyleMaskTitled or NSWindowStyleMaskClosable or NSWindowStyleMaskMiniaturizable or NSWindowStyleMaskResizable, NSBackingStoreBuffered, false)
    window.releasedWhenClosed = false
    window.title = "swiftui-kotlin-binding · Native"
    window.appearance = NSAppearance.appearanceNamed(if ("--dark" in args) NSAppearanceNameDarkAqua else NSAppearanceNameAqua)
    window.contentView = host.nsView
    val delegate = WindowDelegate()
    activeDelegate = delegate; window.delegate = delegate
    window.orderBack(null)
    if (output == null) app.run() else {
        settle()
        check(nativeHostCount() == 1) { "Expected one native root host" }
        val created = backend.createdCount
        if ("--updated" in args) {
            backend.debugAction(backend.debugElements("Button").single())
            backend.debugBoolean(backend.debugElements("Toggle").single(), true)
            backend.debugString(backend.debugElements("TextField").single(), "Kotlin 受控回调")
            backend.debugDouble(backend.debugElements("Slider").single(), 0.65)
            settle()
            check(state.updates == 1 && state.checked && state.text == "Kotlin 受控回调" && state.value == 0.65)
            check(backend.createdCount == created) { "Property updates recreated native nodes" }
        }
        if ("--unmounted" in args) { state.visible = false; settle(); check(backend.debugElements("Toggle").isEmpty()) }
        val frames = composition.scheduledFrames
        val updates = nativePropertyUpdates()
        val bodies = nativeBodyEvaluations()
        settle(2.0)
        check(frames == composition.scheduledFrames && updates == nativePropertyUpdates() && bodies == nativeBodyEvaluations()) { "Static native UI kept scheduling updates" }
        println("native nodes: created=${backend.createdCount}, live=${backend.liveCount}, released=${backend.releasedCount}; hosts=${nativeHostCount()}")
        println("idle: Compose frames=$frames, native property updates=$updates, native body evaluations=$bodies; unchanged during 2 seconds")
        val capture = NSTask()
        capture.launchPath = "/usr/sbin/screencapture"
        capture.arguments = listOf("-x", "-o", "-l", window.windowNumber.toString(), output)
        capture.launch(); capture.waitUntilExit()
        check(capture.terminationStatus == 0)
        println("snapshot: $output")
    }
    window.orderOut(null); window.contentView = null
    host.close(); composition.close(); backend.close()
    activeDelegate = null
    settle(0.5)
    println("after disposal: Kotlin live=${backend.liveCount}, released=${backend.releasedCount}; native nodes=${nativeNodeCount()}, hosts=${nativeHostCount()}")
    check(backend.liveCount == 0 && backend.createdCount == backend.releasedCount)
}

fun main(args: Array<String>) {
    runWindow(args)
    // Objective-C wrappers held by Kotlin/Native are collected after the window scope ends.
    kotlin.native.runtime.GC.collect()
    settle(0.2)
    check(nativeNodeCount() == 0 && nativeHostCount() == 0) { "Native resources retained after scope and interop collection: nodes=${nativeNodeCount()}, hosts=${nativeHostCount()}" }
    println("native resources after scope/interop collection: nodes=${nativeNodeCount()}, hosts=${nativeHostCount()}")
}
