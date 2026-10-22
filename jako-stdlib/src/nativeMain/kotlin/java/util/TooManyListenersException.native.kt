package java.util

actual open class TooManyListenersException : Exception {
    actual constructor() : super()
    actual constructor(s: String?) : super(s)
}
