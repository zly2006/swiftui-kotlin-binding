package me.zly2006.swiftui.bridge

import platform.Foundation.NSThread

/** All SwiftUI bridge operations and ownership changes require Apple's main thread. */
fun requireNativeMainThread() {
    check(NSThread.isMainThread) { "Native UI requires the main thread" }
}
