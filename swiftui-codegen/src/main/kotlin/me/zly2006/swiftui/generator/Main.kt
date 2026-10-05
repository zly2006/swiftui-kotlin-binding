package me.zly2006.swiftui.generator

import java.io.File

fun main(args: Array<String>) {
    when (args.firstOrNull()) {
        "generate" -> generate(File(args[1]))
        "whitelist" -> printWhitelist()
        "coverage" -> printControlCoverage()
        else -> error("Usage: generate <output-directory> | whitelist | coverage")
    }
}
