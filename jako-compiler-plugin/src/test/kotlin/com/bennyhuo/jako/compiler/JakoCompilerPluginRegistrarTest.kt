package com.bennyhuo.jako.compiler

import java.io.ByteArrayOutputStream
import java.io.File
import java.io.PrintStream
import java.nio.file.Files
import java.util.ServiceLoader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import org.jetbrains.kotlin.cli.common.ExitCode
import org.jetbrains.kotlin.cli.jvm.K2JVMCompiler
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi

@OptIn(ExperimentalCompilerApi::class)
class JakoCompilerPluginRegistrarTest {

    @Test
    fun testServiceLoaderLoadsJakoCompilerPluginRegistrar() {
        val registrars = ServiceLoader.load(CompilerPluginRegistrar::class.java).toList()
        val jakoRegistrar = registrars.filterIsInstance<JakoCompilerPluginRegistrar>().singleOrNull()
        assertNotNull(jakoRegistrar, "ServiceLoader should load JakoCompilerPluginRegistrar")
        assertTrue(jakoRegistrar.supportsK2)
        assertEquals("com.bennyhuo.jako.compiler", jakoRegistrar.pluginId)
    }

    @Test
    fun testServiceFileContainsBundledParcelizeMarkerForIdeK2() {
        val serviceResource = JakoCompilerPluginRegistrar::class.java.classLoader
            .getResource("META-INF/services/org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar")
        assertNotNull(serviceResource, "CompilerPluginRegistrar service file must exist")

        val content = serviceResource.readText()
        // MainByRegistrarContentBundledFirCompilerPluginProvider in IntelliJ IDEA K2 checks the raw text
        // of META-INF/services/org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar for
        // "org.jetbrains.kotlin.parcelize.ParcelizeComponentRegistrar" to activate the IDE-bundled Parcelize plugin.
        assertTrue(
            content.contains("com.bennyhuo.jako.compiler.JakoCompilerPluginRegistrar"),
            "Service file must register JakoCompilerPluginRegistrar"
        )
        assertTrue(
            content.contains("org.jetbrains.kotlin.parcelize.ParcelizeComponentRegistrar"),
            "Service file must contain ParcelizeComponentRegistrar marker for IntelliJ IDEA K2 recognition"
        )
    }

    @Test
    fun testWebSupportFirCheckerBrowserWasiAndNodeJsDiagnostics() {
        val tempDir = Files.createTempDirectory("jako_websupport_checker_test").toFile()
        try {
            val annotationsSrc = File(tempDir, "WebSupport.kt").apply {
                writeText(
                    """
                    package com.bennyhuo.jako.annotations

                    enum class WebRuntime { NODE_JS, WASI, BROWSER }

                    @Target(
                        AnnotationTarget.CLASS,
                        AnnotationTarget.CONSTRUCTOR,
                        AnnotationTarget.FUNCTION,
                        AnnotationTarget.PROPERTY
                    )
                    @Retention(AnnotationRetention.BINARY)
                    annotation class WebSupport(
                        vararg val runtimes: WebRuntime,
                        val reason: String = "",
                        val alternative: String = ""
                    )
                    """.trimIndent()
                )
            }

            val apiSrc = File(tempDir, "DemoApis.kt").apply {
                writeText(
                    """
                    package com.example.demo

                    import com.bennyhuo.jako.annotations.WebRuntime
                    import com.bennyhuo.jako.annotations.WebSupport

                    class DemoFile(val path: String) {
                        fun getName(): String = path

                        @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser has no local FS")
                        fun exists(): Boolean = true
                    }

                    @WebSupport(WebRuntime.NODE_JS, reason = "Sockets require Node.js")
                    class DemoSocket(val host: String, val port: Int)

                    class DemoThread {
                        @WebSupport(reason = "Single-threaded runtime", alternative = "kotlinx.coroutines")
                        fun start() {}
                    }
                    """.trimIndent()
                )
            }

            // 1. In BROWSER mode: calling DemoFile.exists() should fail when severity=error, while getName() and propagated/suppressed calls succeed
            val browserConsumerSrc = File(tempDir, "BrowserConsumer.kt").apply {
                writeText(
                    """
                    package com.example.consumer

                    import com.bennyhuo.jako.annotations.WebRuntime
                    import com.bennyhuo.jako.annotations.WebSupport
                    import com.example.demo.DemoFile
                    import com.example.demo.DemoSocket

                    fun safeInBrowser(f: DemoFile): String = f.getName()

                    @WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI)
                    fun propagatedFsHelper(f: DemoFile): Boolean = f.exists()

                    @Suppress("UNSUPPORTED_WEB_API_ERROR")
                    fun suppressedSocketHelper(): DemoSocket = DemoSocket("127.0.0.1", 8080)

                    fun invalidInBrowser(f: DemoFile): Boolean = f.exists()
                    """.trimIndent()
                )
            }

            val (browserExit, browserOutput) = compileWithPlugin(
                sources = listOf(annotationsSrc, apiSrc, browserConsumerSrc),
                outDir = File(tempDir, "out_browser"),
                webRuntimes = "BROWSER",
                severity = "error"
            )
            assertEquals(ExitCode.COMPILATION_ERROR, browserExit, "Expected compilation error in BROWSER mode, output:\n$browserOutput")
            assertTrue(
                browserOutput.contains("API 'com.example.demo.DemoFile.exists' is not supported on WebRuntime BROWSER"),
                "Expected diagnostic for DemoFile.exists in BROWSER mode:\n$browserOutput"
            )

            // 2. In WASI mode: calling DemoFile.exists() succeeds, but calling DemoSocket fails
            val wasiConsumerSrc = File(tempDir, "WasiConsumer.kt").apply {
                writeText(
                    """
                    package com.example.consumer

                    import com.example.demo.DemoFile
                    import com.example.demo.DemoSocket

                    fun validInWasi(f: DemoFile): Boolean = f.exists()
                    fun invalidInWasi(): DemoSocket = DemoSocket("127.0.0.1", 80)
                    """.trimIndent()
                )
            }

            val (wasiExit, wasiOutput) = compileWithPlugin(
                sources = listOf(annotationsSrc, apiSrc, wasiConsumerSrc),
                outDir = File(tempDir, "out_wasi"),
                webRuntimes = "WASI",
                severity = "error"
            )
            assertEquals(ExitCode.COMPILATION_ERROR, wasiExit, "Expected compilation error for DemoSocket in WASI mode:\n$wasiOutput")
            assertTrue(
                wasiOutput.contains("API 'com.example.demo.DemoSocket' is not supported on WebRuntime WASI"),
                "Expected diagnostic for DemoSocket in WASI mode:\n$wasiOutput"
            )

            // 3. In NODE_JS mode: DemoFile.exists() and DemoSocket succeed, but DemoThread.start() (@WebSupport()) fails
            val nodeConsumerSrc = File(tempDir, "NodeConsumer.kt").apply {
                writeText(
                    """
                    package com.example.consumer

                    import com.example.demo.DemoFile
                    import com.example.demo.DemoSocket
                    import com.example.demo.DemoThread

                    fun validInNode(f: DemoFile): Pair<Boolean, DemoSocket> = f.exists() to DemoSocket("127.0.0.1", 80)
                    fun invalidInNode(t: DemoThread) = t.start()
                    """.trimIndent()
                )
            }

            val (nodeExit, nodeOutput) = compileWithPlugin(
                sources = listOf(annotationsSrc, apiSrc, nodeConsumerSrc),
                outDir = File(tempDir, "out_node"),
                webRuntimes = "NODE_JS",
                severity = "error"
            )
            assertEquals(ExitCode.COMPILATION_ERROR, nodeExit, "Expected compilation error for DemoThread.start in NODE_JS mode:\n$nodeOutput")
            assertTrue(
                nodeOutput.contains("API 'com.example.demo.DemoThread.start' is not supported on WebRuntime NODE_JS"),
                "Expected diagnostic for DemoThread.start in NODE_JS mode:\n$nodeOutput"
            )
            assertTrue(
                nodeOutput.contains("Recommended alternative: kotlinx.coroutines"),
                "Expected alternative suggestion in diagnostic:\n$nodeOutput"
            )
        } finally {
            tempDir.deleteRecursively()
        }
    }

    private fun compileWithPlugin(
        sources: List<File>,
        outDir: File,
        webRuntimes: String,
        severity: String
    ): Pair<ExitCode, String> {
        outDir.mkdirs()
        val classpath = System.getProperty("java.class.path")
        val pluginJar = buildPluginJar(File(outDir.parentFile, "plugin.jar"))
        val baos = ByteArrayOutputStream()
        val ps = PrintStream(baos, true, "UTF-8")
        val args = mutableListOf(
            "-d", outDir.absolutePath,
            "-classpath", classpath,
            "-no-stdlib",
            "-Xplugin=${pluginJar.absolutePath}",
            "-P", "plugin:$JAKO_COMPILER_PLUGIN_ID:webRuntimes=$webRuntimes",
            "-P", "plugin:$JAKO_COMPILER_PLUGIN_ID:webSupportSeverity=$severity"
        )
        args.addAll(sources.map { it.absolutePath })
        val exitCode = K2JVMCompiler().exec(ps, *args.toTypedArray())
        return exitCode to baos.toString("UTF-8")
    }

    private fun buildPluginJar(targetJar: File): File {
        if (targetJar.isFile) return targetJar
        val cpEntries = System.getProperty("java.class.path")
            .split(File.pathSeparator)
            .map { File(it) }
            .filter {
                it.invariantSeparatorsPath.contains("jako-compiler-plugin/build/classes") ||
                    it.invariantSeparatorsPath.contains("jako-compiler-plugin/build/resources")
            }
        java.util.jar.JarOutputStream(targetJar.outputStream()).use { jos ->
            val added = HashSet<String>()
            for (dir in cpEntries) {
                if (!dir.isDirectory) continue
                dir.walkTopDown().filter { it.isFile }.forEach { f ->
                    val entryName = f.relativeTo(dir).invariantSeparatorsPath
                    if (added.add(entryName)) {
                        jos.putNextEntry(java.util.jar.JarEntry(entryName))
                        f.inputStream().use { it.copyTo(jos) }
                        jos.closeEntry()
                    }
                }
            }
        }
        return targetJar
    }
}
