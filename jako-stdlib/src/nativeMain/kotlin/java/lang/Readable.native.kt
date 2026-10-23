package java.lang

actual fun interface Readable {
    actual fun read(cb: java.nio.CharBuffer): Int
}
