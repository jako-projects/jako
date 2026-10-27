import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("multiplatform") version "2.4.21"
    id("com.bennyhuo.jako")
}

group = "com.bennyhuo.jako.sample"
version = "1.0.0"

jako {
    packageName.set("com.bennyhuo.jako.sample")
    resDir.set(file("res"))
}

kotlin {
    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        mainRun {
            mainClass.set("com.bennyhuo.jako.sample.MainKt")
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    val nativeTargets = listOf(
        macosArm64(),
        linuxX64(),
        linuxArm64(),
        mingwX64()
    )

    nativeTargets.forEach { target: KotlinNativeTarget ->
        target.binaries {
            executable {
                entryPoint = "com.bennyhuo.jako.sample.main"
            }
        }
    }

    js {
        nodejs()
        binaries.executable()
    }

    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        nodejs()
        binaries.executable()
    }

    sourceSets {
        commonMain.dependencies {
            implementation("com.bennyhuo.jako:jako-stdlib")
            implementation("com.bennyhuo.jako:jako-android")
        }

        jvmMain.dependencies {
            implementation("org.robolectric:android-all:10-robolectric-5803371")
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        jvmTest.dependencies {
            implementation(kotlin("test-junit5"))
            implementation("org.robolectric:android-all:10-robolectric-5803371")
        }
    }
}

@OptIn(InternalKotlinGradlePluginApi::class)
tasks.withType<KotlinCompile>().configureEach {
    multiplatformStructure.fragments.set(emptyList())
    multiplatformStructure.refinesEdges.set(emptyList())
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}

val hostOs = System.getProperty("os.name")?.lowercase() ?: ""
val hostArch = System.getProperty("os.arch")?.lowercase() ?: ""
val isArm64Host = hostArch.contains("aarch64") || hostArch.contains("arm64")
val hostNativeTarget = when {
    hostOs.contains("mac") || hostOs.contains("darwin") -> if (isArm64Host) "MacosArm64" else "MacosX64"
    hostOs.contains("linux") -> if (isArm64Host) "LinuxArm64" else "LinuxX64"
    hostOs.contains("win") -> "MingwX64"
    else -> ""
}
val hostNativeTestTask = hostNativeTarget.replaceFirstChar { it.lowercase() } + "Test"
val enabledTestTasks = setOf("jvmTest", hostNativeTestTask, "jsTest", "jsNodeTest", "wasmJsTest", "wasmJsNodeTest")
tasks.matching {
    when {
        it.name.startsWith("linkDebugTest") -> !it.name.endsWith(hostNativeTarget)
        it.name.startsWith("compileTestKotlin") -> !it.name.endsWith("Jvm") && !it.name.endsWith(hostNativeTarget) && !it.name.endsWith("Js") && !it.name.endsWith("WasmJs")
        it.name.endsWith("Test") && it.name !in enabledTestTasks -> true
        else -> false
    }
}.configureEach {
    enabled = false
}


