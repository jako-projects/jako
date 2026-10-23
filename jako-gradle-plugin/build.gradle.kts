import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `java-gradle-plugin`
    kotlin("jvm")
    id("com.vanniktech.maven.publish")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

gradlePlugin {
    plugins {
        create("jako") {
            id = "com.bennyhuo.jako"
            implementationClass = "com.bennyhuo.jako.gradle.JakoGradlePlugin"
            displayName = "Jako Gradle Plugin"
            description = "Configures the Jako Kotlin Compiler Plugin (cross-platform @Synchronized and @Parcelize) and generates compile-time Android R resource classes from res/ directories."
        }
    }
}

val generateBuildConfigDir = layout.buildDirectory.dir("generated/buildConfig/kotlin")

val generateBuildConfig = tasks.register("generateBuildConfig") {
    val groupVal = project.group.toString()
    val versionVal = project.version.toString()
    val outDir = generateBuildConfigDir
    inputs.property("group", groupVal)
    inputs.property("version", versionVal)
    outputs.dir(outDir)
    doLast {
        val pkgDir = outDir.get().asFile.resolve("com/bennyhuo/jako/gradle")
        pkgDir.mkdirs()
        pkgDir.resolve("JakoPluginBuildConfig.kt").writeText(
            """
            package com.bennyhuo.jako.gradle

            internal object JakoPluginBuildConfig {
                const val GROUP_ID: String = "$groupVal"
                const val COMPILER_PLUGIN_ARTIFACT_ID: String = "jako-compiler-plugin"
                const val COMPILER_PLUGIN_ID: String = "com.bennyhuo.jako.compiler"
                const val VERSION: String = "$versionVal"
            }
            """.trimIndent() + "\n"
        )
    }
}

kotlin.sourceSets.named("main") {
    kotlin.srcDir(generateBuildConfig.map { generateBuildConfigDir.get() })
}

dependencies {
    compileOnly(kotlin("gradle-plugin"))
    compileOnly(kotlin("gradle-plugin-api"))
    testImplementation(kotlin("gradle-plugin"))
    testImplementation(kotlin("gradle-plugin-api"))
    testImplementation(kotlin("test"))
    testImplementation(kotlin("test-junit5"))
}

tasks.test {
    useJUnitPlatform()
}
