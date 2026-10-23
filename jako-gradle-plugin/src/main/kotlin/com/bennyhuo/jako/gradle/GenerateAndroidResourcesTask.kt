package com.bennyhuo.jako.gradle

import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

@DisableCachingByDefault(because = "Fast local R.kt source generation")
abstract class GenerateAndroidResourcesTask : DefaultTask() {

    @get:Input
    @get:Optional
    abstract val packageName: Property<String>

    @get:InputFiles
    @get:Optional
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val resDir: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDir: DirectoryProperty

    @TaskAction
    fun generate() {
        val outRoot = outputDir.get().asFile
        if (outRoot.exists()) {
            outRoot.deleteRecursively()
        }
        outRoot.mkdirs()

        val resRoot = resDir.orNull?.asFile
        val pkg = packageName.orNull?.trim().orEmpty()
        if (pkg.isEmpty()) {
            if (resRoot != null && resRoot.exists()) {
                throw GradleException(
                    "Jako Gradle Plugin detected resource directory `${resRoot.path}` and requires `packageName` to be configured in the `jako { ... }` DSL block, e.g.:\n" +
                        "jako {\n" +
                        "    packageName.set(\"com.example.app\")\n" +
                        "}"
                )
            }
            return
        }

        val generatedCode = RClassGenerator.generateRFileContent(pkg, resRoot)

        val pkgDir = File(outRoot, pkg.replace('.', '/'))
        pkgDir.mkdirs()
        File(pkgDir, "R.kt").writeText(generatedCode, Charsets.UTF_8)
    }
}
