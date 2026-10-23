@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.channels

import java.io.InputStream
import java.io.OutputStream
import java.io.Reader
import java.io.Writer
import java.nio.charset.CharsetDecoder
import java.nio.charset.CharsetEncoder

/**
 * [Nullability Decision] Channels utility methods take non-null streams/channels and return non-null wrappers.
 */
expect class Channels {
    companion object {
        fun newInputStream(ch: ReadableByteChannel): InputStream
        fun newOutputStream(ch: WritableByteChannel): OutputStream
        fun newChannel(inStream: InputStream): ReadableByteChannel
        fun newChannel(out: OutputStream): WritableByteChannel
        fun newReader(ch: ReadableByteChannel, dec: CharsetDecoder, minBufferCap: Int): Reader
        fun newReader(ch: ReadableByteChannel, csName: String): Reader
        fun newWriter(ch: WritableByteChannel, enc: CharsetEncoder, minBufferCap: Int): Writer
        fun newWriter(ch: WritableByteChannel, csName: String): Writer
    }
}
