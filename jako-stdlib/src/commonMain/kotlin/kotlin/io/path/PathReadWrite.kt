package kotlin.io.path

import java.nio.charset.Charset
import java.nio.file.OpenOption
import java.nio.file.Path
import kotlin.io.readBytes
import kotlin.io.readText
import kotlin.io.writeBytes
import kotlin.io.writeText
import kotlin.text.Charsets

public fun Path.readText(charset: Charset = Charsets.UTF_8): String = toFile().readText(charset)

public fun Path.readText(charsetName: String): String = toFile().readText(charsetName)

public fun Path.writeText(text: CharSequence, charset: Charset = Charsets.UTF_8, vararg options: OpenOption) {
    toFile().writeText(text.toString(), charset)
}

public fun Path.writeText(text: CharSequence, charsetName: String, vararg options: OpenOption) {
    toFile().writeText(text.toString(), charsetName)
}

public fun Path.readBytes(): ByteArray = toFile().readBytes()

public fun Path.writeBytes(array: ByteArray, vararg options: OpenOption) {
    toFile().writeBytes(array)
}
