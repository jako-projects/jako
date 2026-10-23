package com.bennyhuo.jako.compiler

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor

const val JAKO_COMPILER_PLUGIN_ID: String = "com.bennyhuo.jako.compiler"

class JakoCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String = JAKO_COMPILER_PLUGIN_ID
    override val pluginOptions: Collection<AbstractCliOption> = emptyList()
}
