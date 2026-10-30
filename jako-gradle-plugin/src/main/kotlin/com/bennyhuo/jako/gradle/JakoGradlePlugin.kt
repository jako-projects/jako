package com.bennyhuo.jako.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinProjectExtension
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilation
import org.jetbrains.kotlin.gradle.plugin.KotlinCompilerPluginSupportPlugin
import org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType
import org.jetbrains.kotlin.gradle.plugin.SubpluginArtifact
import org.jetbrains.kotlin.gradle.plugin.SubpluginOption
import org.jetbrains.kotlin.gradle.targets.js.KotlinWasmTargetType
import org.jetbrains.kotlin.gradle.targets.js.ir.KotlinJsIrTarget

/**
 * Gradle plugin (`id("com.bennyhuo.jako")`) that:
 * 1. Configures the Jako Kotlin Compiler Plugin (`jako-compiler-plugin`) for cross-platform `@Synchronized`, `@Parcelize`, and `@WebSupport` support.
 * 2. Generates compile-time `R.kt` resource constants from `res/` directories (`res/values/` XML files and `res/raw/` files)
 *    and wires the generated source directory into `commonMain` (for Kotlin Multiplatform) or `main` (for Kotlin/JVM).
 */
class JakoGradlePlugin : KotlinCompilerPluginSupportPlugin {

    override fun apply(target: Project) {
        val extension = target.extensions.create("jako", JakoExtension::class.java, target)

        val generateTask = target.tasks.register(
            "generateJakoResources",
            GenerateAndroidResourcesTask::class.java
        ) { task ->
            task.group = "jako"
            task.description = "Generates compile-time R.kt resource IDs from Android res/ directory."
            task.packageName.set(extension.packageName)
            task.resDir.set(extension.resDir)
            task.outputDir.set(target.layout.buildDirectory.dir("generated/jako/commonMain/kotlin"))
        }

        target.plugins.withId("org.jetbrains.kotlin.multiplatform") {
            val kmp = target.extensions.getByType(KotlinMultiplatformExtension::class.java)
            kmp.sourceSets.named("commonMain").configure { sourceSet ->
                sourceSet.kotlin.srcDir(generateTask.flatMap { it.outputDir })
            }
        }

        target.plugins.withId("org.jetbrains.kotlin.jvm") {
            val kotlinExt = target.extensions.getByType(KotlinProjectExtension::class.java)
            kotlinExt.sourceSets.named("main").configure { sourceSet ->
                sourceSet.kotlin.srcDir(generateTask.flatMap { it.outputDir })
            }
        }
    }

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        val ext = kotlinCompilation.target.project.extensions.findByType(JakoExtension::class.java)
        return ext?.compilerPluginEnabled?.getOrElse(true) ?: true
    }

    override fun getCompilerPluginId(): String = JakoPluginBuildConfig.COMPILER_PLUGIN_ID

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = JakoPluginBuildConfig.GROUP_ID,
        artifactId = JakoPluginBuildConfig.COMPILER_PLUGIN_ARTIFACT_ID,
        version = JakoPluginBuildConfig.VERSION
    )

    override fun applyToCompilation(
        kotlinCompilation: KotlinCompilation<*>
    ): Provider<List<SubpluginOption>> {
        val project = kotlinCompilation.target.project
        val ext = project.extensions.findByType(JakoExtension::class.java)
        return project.provider {
            val options = mutableListOf<SubpluginOption>()
            val severity = ext?.webSupportSeverity?.orNull ?: "warning"
            options.add(SubpluginOption("webSupportSeverity", severity))

            val explicitRuntimes = ext?.webRuntimes?.orNull.orEmpty()
            val target = kotlinCompilation.target
            if (target.platformType == KotlinPlatformType.js || target.platformType == KotlinPlatformType.wasm) {
                val detectedRuntimes = if (explicitRuntimes.isNotEmpty()) {
                    explicitRuntimes
                } else if (target is KotlinJsIrTarget) {
                    if (target.wasmTargetType == KotlinWasmTargetType.WASI) {
                        setOf("WASI")
                    } else {
                        val set = linkedSetOf<String>()
                        if (target.isNodejsConfigured) set.add("NODE_JS")
                        if (target.isBrowserConfigured) set.add("BROWSER")
                        if (set.isEmpty()) set.add("NODE_JS")
                        set
                    }
                } else {
                    emptySet()
                }
                if (detectedRuntimes.isNotEmpty()) {
                    options.add(SubpluginOption("webRuntimes", detectedRuntimes.joinToString(",")))
                }
            }
            options
        }
    }
}
