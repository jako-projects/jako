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

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        jvmTest.dependencies {
            implementation(kotlin("test-junit5"))
            implementation("org.robolectric:android-all:10-robolectric-5803371")
        }
    }
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xno-check-actual")
    }
}

tasks.named<KotlinCompile>("compileKotlinJvm") {
    setSource(emptyList<File>())
}

tasks.withType<KotlinNativeCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.add("-Xno-check-actual")
    }
}

// Disable simulator test execution task on local/CI environments where iOS simulator runtime is not installed
tasks.withType<KotlinNativeSimulatorTest>().configureEach {
    enabled = false
}

// Disable intermediate KMP metadata tasks (like compileNativeMainKotlinMetadata) which fail on 32-bit vs 64-bit C-interop type width differences
tasks.matching { it.name.endsWith("KotlinMetadata") && !it.name.startsWith("compileCommonMain") }.configureEach {
    enabled = false
}

tasks.named("check") {
    dependsOn("compileCommonMainKotlinMetadata")
}
