package kotlin

public typealias Volatile = kotlin.concurrent.Volatile

public actual val <T : Any> kotlin.reflect.KClass<T>.java: Class<T> get() = Class(this)
public actual val <T : Any> kotlin.reflect.KClass<T>.javaObjectType: Class<T> get() = Class(this)
public actual val <T : Any> kotlin.reflect.KClass<T>.javaPrimitiveType: Class<T>? get() = Class(this)
@Suppress("UNCHECKED_CAST")
public actual val <T : Any> Class<T>.kotlin: kotlin.reflect.KClass<T> get() = kClass as kotlin.reflect.KClass<T>
@Suppress("UNCHECKED_CAST")
public actual val <T : Any> T.javaClass: Class<T> get() = Class(this::class as kotlin.reflect.KClass<T>)

public actual fun String(codePoints: IntArray, offset: Int, count: Int): String {
    if (offset < 0 || count < 0 || offset > codePoints.size - count) {
        throw IndexOutOfBoundsException("offset=$offset, count=$count, size=${codePoints.size}")
    }
    val sb = StringBuilder(count)
    for (i in offset until offset + count) {
        val cp = codePoints[i]
        if (cp < 0 || cp > 0x10FFFF) {
            throw IllegalArgumentException("Invalid code point: $cp")
        }
        if (cp <= 0xFFFF) {
            sb.append(cp.toChar())
        } else {
            val hi = ((cp - 0x10000) ushr 10) + 0xD800
            val lo = ((cp - 0x10000) and 0x3FF) + 0xDC00
            sb.append(hi.toChar())
            sb.append(lo.toChar())
        }
    }
    return sb.toString()
}

public actual fun String(bytes: ByteArray, offset: Int, length: Int, charset: java.nio.charset.Charset): String {
    if (offset < 0 || length < 0 || offset > bytes.size - length) {
        throw IndexOutOfBoundsException("offset=$offset, length=$length, size=${bytes.size}")
    }
    val cs = charset.name().uppercase().replace("_", "-")
    if (cs == "UTF-8" || cs == "UTF8" || cs == "US-ASCII" || cs == "ASCII") {
        return bytes.decodeToString(offset, offset + length)
    }
    val bb = java.nio.ByteBuffer.wrap(bytes, offset, length)
    val decoder = charset.newDecoder()
        .onMalformedInput(java.nio.charset.CodingErrorAction.REPLACE)
        .onUnmappableCharacter(java.nio.charset.CodingErrorAction.REPLACE)
    val cb = decoder.decode(bb)
    return cb.toString()
}

public actual fun String(bytes: ByteArray, charset: java.nio.charset.Charset): String =
    String(bytes, 0, bytes.size, charset)


