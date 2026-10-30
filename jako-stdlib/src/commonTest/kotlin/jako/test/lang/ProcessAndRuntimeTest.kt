package jako.test.lang

import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.lang.ProcessBuilder
import java.lang.Runtime
import java.util.concurrent.TimeUnit
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class ProcessAndRuntimeTest {

    @Test
    fun testRuntimeStatsAndGc() {
        val rt = Runtime.getRuntime()
        assertTrue(rt.availableProcessors() >= 1)
        assertTrue(rt.totalMemory() > 0L)
        assertTrue(rt.freeMemory() > 0L)
        assertTrue(rt.maxMemory() >= rt.totalMemory())
        rt.gc()
    }

    @Test
    fun testProcessBuilderEchoAndEnvAndErrorStream() {
        val isWin = System.getProperty("file.separator") == "\\"
        if (isWin) return
        if (System.getProperty("os.name") == "WASI") {
            val ex = assertFailsWith<UnsupportedOperationException> {
                ProcessBuilder("/bin/sh", "-c", "echo ok").start()
            }
            assertTrue(ex.message!!.contains("is not supported on target 'WasmWasi'"))
            return
        }

        // 1. Standard stdout + custom environment variable + exit code
        val pb = ProcessBuilder("/bin/sh", "-c", "echo \"Hello \$JAKO_TEST_VAR\"; echo \"err_line\" >&2; exit 7")
        pb.environment()["JAKO_TEST_VAR"] = "JakoProcess"
        val proc = pb.start()

        val outText = BufferedReader(InputStreamReader(proc.inputStream)).readLine()
        val errText = BufferedReader(InputStreamReader(proc.errorStream)).readLine()
        val code = proc.waitFor()

        assertEquals("Hello JakoProcess", outText)
        assertEquals("err_line", errText)
        assertEquals(7, code)
        assertEquals(7, proc.exitValue())
        assertFalse(proc.isAlive)

        // 2. redirectErrorStream(true)
        val pbMerged = ProcessBuilder("/bin/sh", "-c", "echo out1; echo err2 >&2")
            .redirectErrorStream(true)
        val pMerged = pbMerged.start()
        val lines = mutableListOf<String>()
        val reader = BufferedReader(InputStreamReader(pMerged.inputStream))
        while (true) {
            val l = reader.readLine() ?: break
            lines.add(l)
        }
        assertEquals(0, pMerged.waitFor())
        assertEquals(listOf("out1", "err2"), lines)

        // 3. Stdin pipe via outputStream (cat)
        val catProc = ProcessBuilder("/bin/cat").start()
        catProc.outputStream.write("piped-stdin-content\n".encodeToByteArray())
        catProc.outputStream.close()
        val catOut = BufferedReader(InputStreamReader(catProc.inputStream)).readLine()
        assertEquals(0, catProc.waitFor())
        assertEquals("piped-stdin-content", catOut)

        // 4. Timeout and destroyForcibly on long-running command
        val sleepProc = ProcessBuilder("/bin/sleep", "10").start()
        assertTrue(sleepProc.isAlive)
        assertFalse(sleepProc.waitFor(40, TimeUnit.MILLISECONDS))
        sleepProc.destroyForcibly()
        assertTrue(sleepProc.waitFor(2, TimeUnit.SECONDS))
        assertFalse(sleepProc.isAlive)

        // 5. Nonexistent executable throws IOException
        assertFailsWith<IOException> {
            ProcessBuilder("/nonexistent_dir_xyz/nonexistent_bin_xyz").start()
        }

        // 6. ProcessBuilder when open file descriptors exceed single-digit range (fd >= 10, e.g. dash on Ubuntu)
        val tempFiles = mutableListOf<File>()
        val openStreams = mutableListOf<java.io.FileInputStream>()
        try {
            repeat(12) { idx ->
                val f = File.createTempFile("jako_fd_pad_${idx}_", ".tmp")
                tempFiles.add(f)
                openStreams.add(java.io.FileInputStream(f))
            }
            val highFdCat = ProcessBuilder("/bin/cat").start()
            highFdCat.outputStream.write("high-fd-stdin-ok\n".encodeToByteArray())
            highFdCat.outputStream.close()
            val highFdOut = BufferedReader(InputStreamReader(highFdCat.inputStream)).use { it.readLine() }
            assertEquals(0, highFdCat.waitFor())
            assertEquals("high-fd-stdin-ok", highFdOut)
        } finally {
            openStreams.forEach {
                try {
                    it.close()
                } catch (_: Throwable) {
                }
            }
            tempFiles.forEach { it.delete() }
        }
    }
}
