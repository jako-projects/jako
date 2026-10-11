package kotlin.io.path

import java.io.File
import java.nio.charset.Charset
import java.nio.file.LinkOption
import java.nio.file.OpenOption
import java.nio.file.Path
import kotlin.io.readBytes
import kotlin.io.readText
import kotlin.io.writeBytes
import kotlin.io.writeText
import kotlin.text.Charsets

public operator fun Path.div(other: String): Path = resolve(other)
public operator fun Path.div(other: Path): Path = resolve(other)

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

public fun Path.exists(vararg options: LinkOption): Boolean = toFile().exists()

public fun Path.notExists(vararg options: LinkOption): Boolean = !toFile().exists()

public fun Path.isDirectory(vararg options: LinkOption): Boolean = toFile().isDirectory()

public fun Path.isRegularFile(vararg options: LinkOption): Boolean = toFile().isFile()

public val Path.extension: String
    get() {
        val fileName = getFileName()?.toString() ?: return ""
        val idx = fileName.lastIndexOf('.')
        return if (idx == -1 || idx == fileName.length - 1) "" else fileName.substring(idx + 1)
    }

public val Path.nameWithoutExtension: String
    get() {
        val fileName = getFileName()?.toString() ?: return ""
        val idx = fileName.lastIndexOf('.')
        return if (idx == -1) fileName else fileName.substring(0, idx)
    }
