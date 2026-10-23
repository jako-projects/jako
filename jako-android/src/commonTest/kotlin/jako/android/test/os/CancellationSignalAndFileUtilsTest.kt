package jako.android.test.os

import android.os.CancellationSignal
import android.os.FileUtils
import android.os.OperationCanceledException
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CancellationSignalAndFileUtilsTest {

    @Test
    fun testCancellationSignalLifecycle() {
        val signal = CancellationSignal()
        assertFalse(signal.isCanceled())
        assertFalse(signal.isCanceled)
        signal.throwIfCanceled()

        var callbackCount = 0
        signal.setOnCancelListener {
            callbackCount++
        }
        assertEquals(0, callbackCount)

        signal.cancel()
        assertTrue(signal.isCanceled())
        assertTrue(signal.isCanceled)
        assertEquals(1, callbackCount)

        // Calling cancel() again is a no-op
        signal.cancel()
        assertEquals(1, callbackCount)

        assertFailsWith<OperationCanceledException> {
            signal.throwIfCanceled()
        }

        // Setting a new listener after cancellation invokes it immediately
        var lateCallbackTriggered = false
        signal.setOnCancelListener {
            lateCallbackTriggered = true
        }
        assertTrue(lateCallbackTriggered)

        // Clearing listener with null is a no-op even when canceled
        signal.setOnCancelListener(null)

        val defaultEx = OperationCanceledException()
        assertEquals("The operation has been canceled.", defaultEx.message)
        val customEx = OperationCanceledException("Custom cancel reason")
        assertEquals("Custom cancel reason", customEx.message)
    }

    @Test
    fun testFileUtilsCopyAndCloseQuietly() {
        val payload = ByteArray(20_000) { (it % 251).toByte() }
        val input = ByteArrayInputStream(payload)
        val output = ByteArrayOutputStream()

        val copied = FileUtils.copy(input, output)
        assertEquals(payload.size.toLong(), copied)
        assertContentEquals(payload, output.toByteArray())

        var closed = false
        val closeable = AutoCloseable {
            closed = true
            throw IOException("ignored checked/IO exception")
        }
        FileUtils.closeQuietly(closeable)
        assertTrue(closed)
        FileUtils.closeQuietly(null)

        assertFailsWith<IllegalStateException> {
            FileUtils.closeQuietly(AutoCloseable { throw IllegalStateException("rethrown runtime exception") })
        }
    }
}
