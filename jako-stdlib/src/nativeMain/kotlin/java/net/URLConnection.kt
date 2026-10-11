package java.net

actual abstract class URLConnection protected actual constructor(protected val url: URL?) {
    abstract fun connect()
}
