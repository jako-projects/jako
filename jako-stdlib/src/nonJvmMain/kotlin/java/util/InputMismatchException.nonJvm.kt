package java.util

actual open class InputMismatchException : NoSuchElementException {
    actual constructor() : super()
    actual constructor(s: String?) : super(s)
}
