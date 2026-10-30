package com.bennyhuo.jako.compiler

import org.jetbrains.kotlin.compiler.plugin.AbstractCliOption
import org.jetbrains.kotlin.compiler.plugin.CliOption
import org.jetbrains.kotlin.compiler.plugin.CommandLineProcessor
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.config.CompilerConfigurationKey

const val JAKO_COMPILER_PLUGIN_ID: String = "com.bennyhuo.jako.compiler"

val KEY_WEB_RUNTIMES = CompilerConfigurationKey<Set<String>>("webRuntimes")
val KEY_WEB_SUPPORT_SEVERITY = CompilerConfigurationKey<String>("webSupportSeverity")

class JakoCommandLineProcessor : CommandLineProcessor {
    override val pluginId: String = JAKO_COMPILER_PLUGIN_ID

    override val pluginOptions: Collection<AbstractCliOption> = listOf(
        CliOption(
            optionName = "webRuntimes",
            valueDescription = "<NODE_JS,WASI,BROWSER>",
            description = "Comma-separated target WebRuntime names enabled for the current compilation",
            required = false,
            allowMultipleOccurrences = true
        ),
        CliOption(
            optionName = "webSupportSeverity",
            valueDescription = "<warning|error|none>",
            description = "Diagnostic severity for @WebSupport violations (default: warning)",
            required = false,
            allowMultipleOccurrences = false
        )
    )

    override fun processOption(
        option: AbstractCliOption,
        value: String,
        configuration: CompilerConfiguration
    ) {
        when (option.optionName) {
            "webRuntimes" -> {
                val parsed = value.split(',')
                    .map { it.trim().uppercase() }
                    .filter { it.isNotEmpty() }
                    .toSet()
                val existing = configuration.get(KEY_WEB_RUNTIMES).orEmpty()
                configuration.put(KEY_WEB_RUNTIMES, existing + parsed)
            }
            "webSupportSeverity" -> {
                configuration.put(KEY_WEB_SUPPORT_SEVERITY, value.trim().lowercase())
            }
        }
    }
}
