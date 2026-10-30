package com.bennyhuo.jako.gradle

import java.io.File
import javax.inject.Inject
import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty

/**
 * DSL extension for configuring `com.bennyhuo.jako` Gradle plugin:
 *
 * ```kotlin
 * jako {
 *     packageName.set("com.example.app")
 *     resDir.set(file("res"))
 *     webRuntimes.set(setOf("NODE_JS", "BROWSER"))
 *     webSupportSeverity.set("warning")
 * }
 * ```
 */
abstract class JakoExtension @Inject constructor(
    private val project: Project
) {
    /**
     * Target package name for generated `R.kt` (required when generating resources).
     */
    abstract val packageName: Property<String>

    /**
     * Root `res/` directory containing `values/` XML files and `raw/` files.
     * Defaults to `<projectDir>/res` (or `<projectDir>/src/commonMain/res` if present).
     */
    abstract val resDir: DirectoryProperty

    /**
     * Whether to enable the Jako Kotlin Compiler Plugin (e.g., cross-platform `@Synchronized`, `@Parcelize`, and `@WebSupport` checks).
     * Defaults to `true`.
     */
    abstract val compilerPluginEnabled: Property<Boolean>

    /**
     * Explicit override for target WebRuntime names (`NODE_JS`, `WASI`, `BROWSER`) to validate against `@WebSupport` on JS/Wasm targets.
     * When empty, runtimes are inferred automatically from the Kotlin target configuration (`nodejs()`, `browser()`, `wasmWasi()`).
     */
    abstract val webRuntimes: SetProperty<String>

    /**
     * Diagnostic severity for `@WebSupport` violations: `"warning"` (default), `"error"`, or `"none"`.
     */
    abstract val webSupportSeverity: Property<String>

    init {
        val defaultRes = project.file("res")
        val commonMainRes = project.file("src/commonMain/res")
        resDir.convention(
            project.layout.projectDirectory.dir(
                if (!defaultRes.exists() && commonMainRes.exists()) "src/commonMain/res" else "res"
            )
        )
        compilerPluginEnabled.convention(true)
        webRuntimes.convention(emptySet())
        webSupportSeverity.convention("warning")
    }

    fun packageName(value: String) {
        packageName.set(value)
    }

    fun resDir(path: File) {
        resDir.set(path)
    }

    fun resDir(path: String) {
        resDir.set(project.file(path))
    }

    fun compilerPluginEnabled(enabled: Boolean) {
        compilerPluginEnabled.set(enabled)
    }

    fun webRuntimes(vararg runtimes: String) {
        webRuntimes.set(runtimes.toSet())
    }

    fun webSupportSeverity(severity: String) {
        webSupportSeverity.set(severity)
    }
}
