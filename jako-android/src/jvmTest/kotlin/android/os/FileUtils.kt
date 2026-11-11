package android.os

import java.io.InputStream
import java.io.OutputStream

object FileUtils {
    @JvmStatic
    fun copy(`in`: InputStream, out: OutputStream): Long {
        val buffer = ByteArray(8192)
        var total = 0L
        while (true) {
            val read = `in`.read(buffer)
            if (read == -1) break
            out.write(buffer, 0, read)
            total += read
        }
        return total
    }

    @JvmStatic
    fun closeQuietly(closeable: AutoCloseable?) {
        if (closeable != null) {
            try {
                closeable.close()
            } catch (rethrown: RuntimeException) {
                throw rethrown
            } catch (_: Exception) {
            }
        }
    }
}
