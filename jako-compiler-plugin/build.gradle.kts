import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    kotlin("jvm")
    id("com.vanniktech.maven.publish")
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_1_8)
        optIn.add("org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi")
        optIn.add("org.jetbrains.kotlin.ir.symbols.UnsafeDuringIrConstructionAPI")
        optIn.add("org.jetbrains.kotlin.fir.symbols.SymbolInternals")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

val parcelizeCompiler: Configuration by configurations.creating

dependencies {
    compileOnly(kotlin("compiler-embeddable"))
    compileOnly(kotlin("parcelize-compiler"))
    parcelizeCompiler(kotlin("parcelize-compiler") as String) {
        isTransitive = false
    }
    testImplementation(kotlin("test"))
    testImplementation(kotlin("compiler-embeddable"))
    testImplementation(kotlin("parcelize-compiler"))
}

tasks.jar {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(parcelizeCompiler.map { if (it.isDirectory) it else zipTree(it) }) {
        exclude("META-INF/services/**")
        exclude("META-INF/*.kotlin_module")
        exclude("META-INF/MANIFEST.MF")
        exclude("kotlinx/parcelize/**")
    }
}
