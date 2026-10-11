package jako.test.io

import java.io.*

import kotlin.test.*

class PrintStreamTest {

    @Test
    fun testPrintStream() {
        val baos = ByteArrayOutputStream()
        val ps = PrintStream(baos)

        ps.print("Number: ")
        ps.print(100)
        ps.println()
        ps.println("Second Line")
        ps.flush()

        val output = baos.toByteArray().decodeToString()
        assertEquals("Number: 100\nSecond Line\n", output.replace("\r\n", "\n"))
    }

    @Test
    fun testPrintWriter() {
        val sw = StringWriter()
        val pw = PrintWriter(sw)

        pw.print("Score: ")
        pw.print(95.5)
        pw.println()
        pw.println("Status: Passed")
        pw.flush()

        assertEquals("Score: 95.5\nStatus: Passed\n", sw.toString().replace("\r\n", "\n"))
    }
}
