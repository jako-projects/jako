package java.lang

actual open class StringIndexOutOfBoundsException : IndexOutOfBoundsException {
    actual constructor() : super()
    actual constructor(s: String?) : super(s)
    actual constructor(index: Int) : super("String index out of range: $index")
}
