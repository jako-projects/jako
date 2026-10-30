package kotlin.io.path

import com.bennyhuo.jako.annotations.WebRuntime
import com.bennyhuo.jako.annotations.WebSupport
import java.nio.charset.Charset
import java.nio.file.OpenOption
import java.nio.file.Path
import kotlin.io.readBytes
import kotlin.io.readText
import kotlin.io.writeBytes
import kotlin.io.writeText
import kotlin.text.Charsets

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
public fun Path.readText(charset: Charset = Charsets.UTF_8): String = toFile().readText(charset)

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
public fun Path.readText(charsetName: String): String = toFile().readText(charsetName)

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
public fun Path.writeText(text: CharSequence, charset: Charset = Charsets.UTF_8, vararg options: OpenOption) {
    toFile().writeText(text.toString(), charset)
}

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
public fun Path.writeText(text: CharSequence, charsetName: String, vararg options: OpenOption) {
    toFile().writeText(text.toString(), charsetName)
}

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
public fun Path.readBytes(): ByteArray = toFile().readBytes()

@WebSupport(WebRuntime.NODE_JS, WebRuntime.WASI, reason = "Browser sandbox does not allow synchronous local file system access")
public fun Path.writeBytes(array: ByteArray, vararg options: OpenOption) {
    toFile().writeBytes(array)
}
