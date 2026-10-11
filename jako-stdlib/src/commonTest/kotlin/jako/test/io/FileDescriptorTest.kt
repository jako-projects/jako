package jako.test.io

import java.io.FileDescriptor
import java.io.SyncFailedException
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class FileDescriptorTest {

    @Test
    fun testStandardDescriptors() {
        assertNotNull(FileDescriptor.`in`)
        assertNotNull(FileDescriptor.out)
        assertNotNull(FileDescriptor.err)

        val defaultFd = FileDescriptor()
        assertFalse(defaultFd.valid())
        assertFailsWith<SyncFailedException> {
            defaultFd.sync()
        }
    }

    @Test
    fun testFileInputStreamWithFileDescriptor() {
        val tempFile = java.io.File.createTempFile("fd_test", ".txt")
        try {
            java.io.FileOutputStream(tempFile).use { fos ->
                fos.write("Hello FD".encodeToByteArray())
            }

            val fis1 = java.io.FileInputStream(tempFile)
            val fd = fis1.fd
            assertTrue(fd.valid())

            val fis2 = java.io.FileInputStream(fd)
            val bytes = ByteArray(8)
            val read = fis2.read(bytes, 0, 8)
            kotlin.test.assertEquals(8, read)
            kotlin.test.assertEquals("Hello FD", bytes.decodeToString())

            fis2.close()
            val fis3 = java.io.FileInputStream(fd)
            assertFailsWith<java.io.IOException> {
                fis3.read()
            }
            fis3.close()
            fis1.close()
        } finally {
            tempFile.delete()
        }
    }
}
