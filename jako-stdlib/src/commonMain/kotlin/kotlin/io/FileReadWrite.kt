package kotlin.io

import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.Reader
import java.io.Writer
import java.nio.charset.Charset
import kotlin.text.Charsets

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
public fun File.readText(charset: Charset = Charsets.UTF_8): String = readText(charset.name())

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
public fun File.readText(charsetName: String): String {
    val r: Reader = FileInputStream(this).reader(charsetName)
    try {
        return r.readText()
    } finally {
        r.close()
    }
}

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
public fun File.writeText(text: String, charset: Charset = Charsets.UTF_8) {
    writeText(text, charset.name())
}

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
public fun File.writeText(text: String, charsetName: String) {
    val w: Writer = FileOutputStream(this).writer(charsetName)
    try {
        w.write(text)
    } finally {
        w.close()
    }
}

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
public fun File.appendText(text: String, charset: Charset = Charsets.UTF_8) {
    appendText(text, charset.name())
}

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
public fun File.appendText(text: String, charsetName: String) {
    val w: Writer = FileOutputStream(this, true).writer(charsetName)
    try {
        w.write(text)
    } finally {
        w.close()
    }
}

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
public fun File.readBytes(): ByteArray {
    val s: InputStream = FileInputStream(this)
    try {
        return s.readBytes()
    } finally {
        s.close()
    }
}

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
public fun File.writeBytes(array: ByteArray) {
    val s: OutputStream = FileOutputStream(this)
    try {
        s.write(array)
    } finally {
        s.close()
    }
}

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
public fun File.appendBytes(array: ByteArray) {
    val s: OutputStream = FileOutputStream(this, true)
    try {
        s.write(array)
    } finally {
        s.close()
    }
}

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
public fun File.forEachLine(charset: Charset = Charsets.UTF_8, action: (String) -> Unit) {
    forEachLine(charset.name(), action)
}

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
public fun File.forEachLine(charsetName: String, action: (String) -> Unit) {
    val r: Reader = FileInputStream(this).reader(charsetName)
    try {
        r.forEachLine(action)
    } finally {
        r.close()
    }
}

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
public fun File.readLines(charset: Charset = Charsets.UTF_8): List<String> = readLines(charset.name())

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
public fun File.readLines(charsetName: String): List<String> {
    val r: Reader = FileInputStream(this).reader(charsetName)
    try {
        return r.readLines()
    } finally {
        r.close()
    }
}

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
public fun <T> File.useLines(charset: Charset = Charsets.UTF_8, block: (Sequence<String>) -> T): T =
    useLines(charset.name(), block)

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
public fun <T> File.useLines(charsetName: String, block: (Sequence<String>) -> T): T {
    val r: Reader = FileInputStream(this).reader(charsetName)
    try {
        return r.useLines(block)
    } finally {
        r.close()
    }
}

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
public fun File.bufferedReader(charset: Charset = Charsets.UTF_8, bufferSize: Int = 8192): BufferedReader =
    reader(charset).buffered(bufferSize)

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
public fun File.bufferedWriter(charset: Charset = Charsets.UTF_8, bufferSize: Int = 8192): BufferedWriter =
    writer(charset).buffered(bufferSize)

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
public fun File.reader(charset: Charset = Charsets.UTF_8): InputStreamReader =
    FileInputStream(this).reader(charset)

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
public fun File.writer(charset: Charset = Charsets.UTF_8): OutputStreamWriter =
    FileOutputStream(this).writer(charset)
