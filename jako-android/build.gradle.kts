import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.native.tasks.KotlinNativeSimulatorTest
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.tasks.KotlinNativeCompile

plugins {
    kotlin("multiplatform")
    id("com.vanniktech.maven.publish")
}

kotlin {
    applyDefaultHierarchyTemplate()

    jvm {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
            freeCompilerArgs.add("-Xno-check-actual")
        }
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }
    }

    compilerOptions {
        freeCompilerArgs.add("-Xexpect-actual-classes")
        freeCompilerArgs.add("-Xno-check-actual")
    }

    // Apple / macOS / iOS / tvOS / watchOS Targets
    macosArm64()
    macosX64()
    iosArm64()
    iosX64()
    iosSimulatorArm64()

    tvosArm64()
    tvosX64()
    tvosSimulatorArm64()

    watchosArm32()
    watchosArm64()
    watchosDeviceArm64()
    watchosSimulatorArm64()
    watchosX64()

    // Linux Targets
    linuxX64()
    linuxArm64()

    // Windows Target
    mingwX64()

    // Web / Node.js Targets
    js(IR) {
        nodejs {
            testTask {
                useMocha {
                    timeout = "30s"
                }
            }
        }
    }
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        nodejs()
    }

    sourceSets {
        all {
            languageSettings.optIn("kotlinx.cinterop.ExperimentalForeignApi")
            languageSettings.optIn("kotlin.experimental.ExperimentalNativeApi")
            compilerOptions.freeCompilerArgs.add("-Xallow-kotlin-package")
            compilerOptions.freeCompilerArgs.add("-Xno-check-actual")
        }

        commonMain.dependencies {
            api(project(":jako-stdlib"))
        }

        jvmMain.dependencies {
            compileOnly("org.robolectric:android-all:10-robolectric-5803371")
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        jvmTest.dependencies {
            implementation(kotlin("test-junit5"))
            implementation("org.robolectric:android-all:10-robolectric-5803371")
        }

        val commonMain = getByName("commonMain")
        val nonJvmMain by creating {
            dependsOn(commonMain)
        }

        getByName("nativeMain").dependsOn(nonJvmMain)

        val webMain = getByName("webMain").apply {
            dependsOn(nonJvmMain)
        }

        getByName("wasmJsMain").apply {
            languageSettings.optIn("kotlin.js.ExperimentalWasmJsInterop")
        }
    }
}

@OptIn(org.jetbrains.kotlin.gradle.InternalKotlinGradlePluginApi::class)
tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xno-check-actual")
    }
    multiplatformStructure.fragments.set(emptyList())
    multiplatformStructure.refinesEdges.set(emptyList())
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
}

tasks.named<KotlinCompile>("compileKotlinJvm") {
    setSource(
        fileTree("src/commonMain/kotlin/com/bennyhuo/jako"),
        fileTree("src/commonMain/kotlin/kotlinx/parcelize"),
        fileTree("src/jvmMain/kotlin")
    )
}

tasks.withType<KotlinNativeCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xno-check-actual")
    }
}

// Disable simulator and non-host native test compilation/linking/execution on local/CI environments
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
val enabledTestTasks = setOf(
    "jvmTest",
    hostNativeTestTask,
    "jsTest",
    "jsNodeTest",
    "wasmJsTest",
    "wasmJsNodeTest"
)
tasks.withType<KotlinNativeSimulatorTest>().configureEach {
    enabled = false
}
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

// Disable intermediate KMP metadata tasks (like compileNativeMainKotlinMetadata) which fail on 32-bit vs 64-bit C-interop type width differences
tasks.matching { it.name.endsWith("KotlinMetadata") && !it.name.startsWith("compileCommonMain") }.configureEach {
    enabled = false
}

tasks.named("check") {
    dependsOn("compileCommonMainKotlinMetadata")
}

configurations.matching {
    it.name.startsWith("kotlinCompilerPluginClasspath") || it.name.endsWith("CompilerPluginClasspath")
}.configureEach {
    dependencies.add(project.dependencies.create(project(":jako-compiler-plugin")))
}

