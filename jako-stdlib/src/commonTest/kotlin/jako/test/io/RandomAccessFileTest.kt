package jako.test.io

import java.io.*
import kotlin.test.*

class RandomAccessFileTest {

    @Test
    fun testRandomAccessFileReadWriteSeek() {
        val tempFile = File("/tmp/raf_test_${System.currentTimeMillis()}.bin")
        if (tempFile.exists()) tempFile.delete()

        try {
            val raf = RandomAccessFile(tempFile, "rw")
            raf.write(65) // 'A'
            raf.write(66) // 'B'
            raf.write(byteArrayOf(67, 68, 69)) // 'C', 'D', 'E'

            assertEquals(5L, raf.length())
            assertEquals(5L, raf.getFilePointer())

            raf.seek(0L)
            assertEquals(0L, raf.getFilePointer())
            assertEquals(65, raf.read())

            raf.seek(3L)
            assertEquals(68, raf.read()) // 'D'

            raf.close()
        } finally {
            if (tempFile.exists()) tempFile.delete()
        }
    }
}
