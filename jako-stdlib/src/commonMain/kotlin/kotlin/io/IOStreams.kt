package kotlin.io

import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.nio.charset.Charset
import kotlin.text.Charsets

public fun InputStream.readBytes(): ByteArray {
    val out = ByteArrayOutputStream()
    val buffer = ByteArray(8192)
    var bytes = read(buffer)
    while (bytes >= 0) {
        out.write(buffer, 0, bytes)
        bytes = read(buffer)
    }
    return out.toByteArray()
}

public fun InputStream.copyTo(out: OutputStream, bufferSize: Int = 8192): Long {
    var bytesCopied: Long = 0
    val buffer = ByteArray(bufferSize)
    var bytes = read(buffer)
    while (bytes >= 0) {
        out.write(buffer, 0, bytes)
        bytesCopied += bytes
        bytes = read(buffer)
    }
    return bytesCopied
}

public fun InputStream.bufferedReader(charset: Charset = Charsets.UTF_8): BufferedReader =
    bufferedReader(charset.name())

public fun InputStream.bufferedReader(charsetName: String): BufferedReader =
    BufferedReader(InputStreamReader(this, charsetName))

public fun InputStream.reader(charset: Charset = Charsets.UTF_8): InputStreamReader =
    reader(charset.name())

public fun InputStream.reader(charsetName: String): InputStreamReader =
    InputStreamReader(this, charsetName)

public fun InputStream.buffered(bufferSize: Int = 8192): BufferedInputStream =
    if (this is BufferedInputStream) this else BufferedInputStream(this, bufferSize)

public fun OutputStream.bufferedWriter(charset: Charset = Charsets.UTF_8): BufferedWriter =
    bufferedWriter(charset.name())

public fun OutputStream.bufferedWriter(charsetName: String): BufferedWriter =
    BufferedWriter(OutputStreamWriter(this, charsetName))

public fun OutputStream.writer(charset: Charset = Charsets.UTF_8): OutputStreamWriter =
    writer(charset.name())

public fun OutputStream.writer(charsetName: String): OutputStreamWriter =
    OutputStreamWriter(this, charsetName)

public fun OutputStream.buffered(bufferSize: Int = 8192): BufferedOutputStream =
    if (this is BufferedOutputStream) this else BufferedOutputStream(this, bufferSize)
