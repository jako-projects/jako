import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
    kotlin("multiplatform") version "2.4.21"
}

group = "com.bennyhuo.jako.sample"
version = "1.0.0"

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
        macosX64(),
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

    sourceSets {
        commonMain.dependencies {
            implementation("com.bennyhuo.jako:jako-stdlib")
        }

        commonTest.dependencies {
            implementation(kotlin("test"))
        }

        jvmTest.dependencies {
            implementation(kotlin("test-junit5"))
        }
    }
}
