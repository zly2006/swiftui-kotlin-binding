@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class, kotlin.native.runtime.NativeRuntimeApi::class)
package me.zly2006.swiftui.samples.host

import androidx.compose.runtime.Composable
import kotlinx.cinterop.autoreleasepool
import me.zly2006.swiftui.nativeui.*
import platform.AppKit.*
import platform.Foundation.*
import platform.darwin.NSObject

private class SampleDelegate : NSObject(), NSWindowDelegateProtocol { override fun windowWillClose(notification: NSNotification) { NSApplication.sharedApplication().stop(null) } }
private var retainedDelegate: SampleDelegate? = null
private fun settle(seconds: Double) {
    val deadline = NSDate.dateWithTimeIntervalSinceNow(seconds)
    while (NSDate().compare(deadline) == NSOrderedAscending) NSRunLoop.mainRunLoop.runUntilDate(NSDate.dateWithTimeIntervalSinceNow(0.02))
}
private fun runSampleWindow(args: Array<String>, title: String, width: Double, height: Double, transparent: Boolean = false, content: @Composable (Boolean) -> Unit) {
    autoreleasepool {
        val output = args.firstOrNull { it.startsWith("--snapshot=") }?.substringAfter('=')
        val app = NSApplication.sharedApplication()
        app.setActivationPolicy(if (output == null) NSApplicationActivationPolicy.NSApplicationActivationPolicyRegular else NSApplicationActivationPolicy.NSApplicationActivationPolicyProhibited)
        app.finishLaunching()
        val dark = "--dark" in args
        val backend = MacosNativeUiBackend()
        val composition = NativeComposition(backend)
        composition.setContent { content(dark) }
        val host = backend.createHost(composition.rootElement)
        val window = NSWindow(NSMakeRect(0.0, 0.0, width, height), NSWindowStyleMaskTitled or NSWindowStyleMaskClosable or NSWindowStyleMaskResizable or if (transparent) NSWindowStyleMaskFullSizeContentView else 0uL, NSBackingStoreBuffered, false)
        window.releasedWhenClosed = false
        window.title = title
        window.appearance = NSAppearance.appearanceNamed(if (dark) NSAppearanceNameDarkAqua else NSAppearanceNameAqua)
        if (transparent) { window.opaque = false; window.backgroundColor = NSColor.clearColor; window.titlebarAppearsTransparent = true; window.titleVisibility = NSWindowTitleHidden }
        window.contentView = host.nsView
        val delegate = SampleDelegate(); retainedDelegate = delegate; window.delegate = delegate
        if (output == null) { window.makeKeyAndOrderFront(null); app.run() } else {
            window.orderBack(null); settle(2.0)
            check(nativeHostCount() == 1)
            val frames = composition.scheduledFrames; val bodies = nativeBodyEvaluations(); settle(1.0)
            check(frames == composition.scheduledFrames && bodies == nativeBodyEvaluations()) { "Static sample continued updating" }
            val capture = NSTask(); capture.launchPath = "/usr/sbin/screencapture"; capture.arguments = listOf("-x", "-o", "-l", window.windowNumber.toString(), output); capture.launch(); capture.waitUntilExit(); check(capture.terminationStatus == 0)
            println("Sample: $title; nodes=${backend.liveCount}; hosts=${nativeHostCount()}; idle frames=$frames, bodies=$bodies")
        }
        window.orderOut(null); window.contentView = null; host.close(); composition.close(); backend.close(); retainedDelegate = null; settle(0.2)
        check(backend.liveCount == 0)
    }
 }
fun runSample(args: Array<String>, title: String, width: Double, height: Double, transparent: Boolean = false, content: @Composable (Boolean) -> Unit) {
    runSampleWindow(args, title, width, height, transparent, content)
    kotlin.native.runtime.GC.collect(); settle(0.2)
    check(nativeNodeCount() == 0 && nativeHostCount() == 0) { "Retained native resources: nodes=${nativeNodeCount()}, hosts=${nativeHostCount()}" }
    println("Native resources released: nodes=0, hosts=0")
}
