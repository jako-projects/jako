package jako.test.lang

import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport
import com.bennyhuo.jako.withSimulatedWebRuntime
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.RandomAccessFile
import java.lang.ProcessBuilder
import java.lang.Runtime
import java.lang.System
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.nio.file.Paths
import java.security.SecureRandom
import kotlin.io.extension
import kotlin.io.invariantSeparatorsPath
import kotlin.io.nameWithoutExtension
import kotlin.io.path.div
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNull
import kotlin.test.assertTrue

class WebSupportRuntimeTest {

    @WebSupport(
        WebRuntime.NODE_JS,
        WebRuntime.WASI,
        reason = "Test annotation metadata",
        alternative = "Test alternative"
    )
    private fun annotatedHelper(): Int = 42

    @Test
    fun testWebRuntimeEnumAndWebSupportAnnotation() {
        val runtimes = WebRuntime.entries
        assertEquals(3, runtimes.size)
        assertTrue(runtimes.contains(WebRuntime.NODE_JS))
        assertTrue(runtimes.contains(WebRuntime.WASI))
        assertTrue(runtimes.contains(WebRuntime.BROWSER))

        val ann = WebSupport(
            WebRuntime.NODE_JS,
            reason = "Requires Node.js",
            alternative = "Fetch API"
        )
        assertEquals(1, ann.runtimes.size)
        assertEquals(WebRuntime.NODE_JS, ann.runtimes[0])
        assertEquals("Requires Node.js", ann.reason)
        assertEquals("Fetch API", ann.alternative)
        assertEquals(42, annotatedHelper())
    }

    @Test
    fun testPurePathAndClockAndSecureRandomWorkInAllRuntimes() {
        withSimulatedWebRuntime(WebRuntime.BROWSER) {
            // Pure path manipulation on File and Path must work even in Browser mode
            val f = File("/workspace/project/readme.md")
            assertEquals("readme.md", f.name)
            assertEquals("md", f.extension)
            assertEquals("readme", f.nameWithoutExtension)
            assertEquals("/workspace/project", f.parent?.replace('\\', '/'))
            assertEquals("/workspace/project/readme.md", f.invariantSeparatorsPath)

            val p = Paths.get("/workspace") / "sub" / "data.json"
            assertEquals("data.json", p.getFileName()?.toString())
            assertEquals("/workspace/sub/data.json", p.normalize().toString().replace('\\', '/'))

            // Time and SecureRandom must work in Browser mode
            assertTrue(System.currentTimeMillis() > 0L)
            assertTrue(System.nanoTime() != 0L)
            val randomBytes = ByteArray(16)
            SecureRandom().nextBytes(randomBytes)
            assertTrue(randomBytes.any { it.toInt() != 0 })
        }
    }

    @Test
    fun testSimulatedBrowserAndWasiRuntimeGuardsOnWebTarget() {
        // Detect if we are executing on a Web/Wasm target where System.web.kt is active
        val isWebTarget = withSimulatedWebRuntime(WebRuntime.BROWSER) {
            System.getProperty("os.name") == "Browser"
        }
        if (!isWebTarget) return

        // 1. Verify Browser mode blocks local FS, Sockets, and ProcessBuilder with standardized diagnostic
        withSimulatedWebRuntime(WebRuntime.BROWSER) {
            assertEquals("Browser", System.getProperty("os.name"))
            assertEquals("/", System.getProperty("file.separator"))
            assertEquals(":", System.getProperty("path.separator"))
            assertEquals("/", System.getProperty("user.dir"))
            assertNull(System.getenv("PATH"))

            val relFile = File("relative/file.txt")
            assertEquals("/relative/file.txt", relFile.absolutePath)
            assertTrue(File("/workspace/project/readme.md").isAbsolute)

            val fileEx = assertFailsWith<UnsupportedOperationException> {
                relFile.exists()
            }
            assertTrue(fileEx.message!!.contains("is not supported on target 'JS/Wasm Browser'"))

            val fisEx = assertFailsWith<UnsupportedOperationException> {
                FileInputStream("test.txt")
            }
            assertTrue(fisEx.message!!.contains("is not supported on target 'JS/Wasm Browser'"))

            val fosEx = assertFailsWith<UnsupportedOperationException> {
                FileOutputStream("test.txt")
            }
            assertTrue(fosEx.message!!.contains("is not supported on target 'JS/Wasm Browser'"))

            val rafEx = assertFailsWith<UnsupportedOperationException> {
                RandomAccessFile("test.txt", "r")
            }
            assertTrue(rafEx.message!!.contains("is not supported on target 'JS/Wasm Browser'"))

            val sockEx = assertFailsWith<UnsupportedOperationException> {
                Socket("127.0.0.1", 80)
            }
            assertTrue(sockEx.message!!.contains("is not supported on target 'JS/Wasm Browser'"))

            val procEx = assertFailsWith<UnsupportedOperationException> {
                ProcessBuilder("echo", "hi").start()
            }
            assertTrue(procEx.message!!.contains("is not supported on target 'JS/Wasm Browser'"))

            val exitEx = assertFailsWith<UnsupportedOperationException> {
                Runtime.getRuntime().exit(0)
            }
            assertTrue(exitEx.message!!.contains("is not supported on target 'JS/Wasm Browser'"))
        }

        // 2. Verify WASI mode allows File I/O but blocks Sockets, DNS, and ProcessBuilder
        withSimulatedWebRuntime(WebRuntime.WASI) {
            val serverEx = assertFailsWith<UnsupportedOperationException> {
                ServerSocket(0)
            }
            assertTrue(serverEx.message!!.contains("is not supported on target 'WasmWasi'"))

            val udpEx = assertFailsWith<UnsupportedOperationException> {
                DatagramSocket(0)
            }
            assertTrue(udpEx.message!!.contains("is not supported on target 'WasmWasi'"))

            val dnsEx = assertFailsWith<UnsupportedOperationException> {
                InetAddress.getLocalHost()
            }
            assertTrue(dnsEx.message!!.contains("is not supported on target 'WasmWasi'"))

            val procEx = assertFailsWith<UnsupportedOperationException> {
                ProcessBuilder("echo", "hi").start()
            }
            assertTrue(procEx.message!!.contains("is not supported on target 'WasmWasi'"))
        }
    }
}
