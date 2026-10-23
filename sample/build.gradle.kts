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


