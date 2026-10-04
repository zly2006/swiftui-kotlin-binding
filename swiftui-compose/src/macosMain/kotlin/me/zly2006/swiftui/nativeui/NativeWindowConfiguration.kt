@file:OptIn(kotlinx.cinterop.ExperimentalForeignApi::class)

package me.zly2006.swiftui.nativeui

import kotlinx.cinterop.useContents
import platform.AppKit.NSScrollView
import platform.AppKit.NSToolbarItem
import platform.AppKit.NSView
import platform.AppKit.NSWindow
import platform.Foundation.NSMakePoint

/** SwiftUI may reinstall the native item during layout; callers can apply this on window updates. */
fun NSWindow.hideSidebarToggle() {
    checkNativeUiMainThread()
    val current = toolbar ?: return
    current.items.indices.reversed().forEach { index ->
        val item = current.items[index] as? NSToolbarItem ?: return@forEach
        val identifier = item.itemIdentifier.orEmpty().lowercase()
        if ("sidebar" in identifier && "toggle" in identifier) current.removeItemAtIndex(index.toLong())
    }
}

/** Scroll an actual native vertical viewport; no wheel events or foreground activation. */
fun NSView.scrollVertically(offset: Double): Boolean {
    checkNativeUiMainThread()
    require(offset.isFinite() && offset >= 0)
    val candidates = mutableListOf<NSScrollView>()

    fun visit(view: NSView) {
        if (view is NSScrollView) {
            val documentHeight = view.documentView?.bounds?.useContents { size.height } ?: 0.0
            val viewportHeight = view.contentView().bounds.useContents { size.height }
            if (documentHeight > viewportHeight + 1.0) candidates += view
        }
        view.subviews.forEach { (it as? NSView)?.let(::visit) }
    }
    visit(this)
    val target = candidates.maxByOrNull { it.contentView().bounds.useContents { size.width * size.height } } ?: return false
    val documentHeight = target.documentView!!.bounds.useContents { size.height }
    val viewportHeight = target.contentView().bounds.useContents { size.height }
    target.contentView().scrollToPoint(NSMakePoint(0.0, offset.coerceAtMost((documentHeight - viewportHeight).coerceAtLeast(0.0))))
    target.reflectScrolledClipView(target.contentView())
    return true
}
