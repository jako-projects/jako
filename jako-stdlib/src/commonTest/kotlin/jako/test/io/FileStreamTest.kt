package jako.test.io

import java.io.*

import kotlin.test.*

class FileStreamTest {

    @Test
    fun testFileStreamsAndFilters() {
        val tempDir = File("/tmp/jako_test_dir_${RandomId()}")
        tempDir.mkdirs()

        val testFile = File(tempDir, "sample.txt")
        val fos = FileOutputStream(testFile)
        val text = "Hello Jako File Streams!"
        fos.write(text.encodeToByteArray())
        fos.close()

        assertTrue(testFile.exists())
        assertEquals(text.length.toLong(), testFile.length())

        val fis = FileInputStream(testFile)
        val buf = ByteArray(100)
        val n = fis.read(buf)
        fis.close()

        assertEquals(text, buf.decodeToString(0, n))

        val txtFilter = FileFilter { f -> f.getName().endsWith(".txt") }
        assertTrue(txtFilter.accept(testFile))

        val nameFilter = FilenameFilter { _, name -> name == "sample.txt" }
        assertTrue(nameFilter.accept(tempDir, "sample.txt"))

        testFile.delete()
        tempDir.delete()
    }

    @Test
    fun testFileReaderAndWriter() {
        val tempDir = File("/tmp/jako_test_rw_dir_${RandomId()}")
        tempDir.mkdirs()
        val tempFile = File(tempDir, "sample.txt")
        val writer = FileWriter(tempFile)
        writer.write("Testing FileReader and FileWriter: 12345")
        writer.close()

        val reader = FileReader(tempFile)
        val br = BufferedReader(reader)
        val line = br.readLine()
        br.close()

        assertEquals("Testing FileReader and FileWriter: 12345", line)
        tempFile.delete()
        tempDir.delete()
    }

    private fun RandomId(): String = (1000..9999).random().toString()
}
