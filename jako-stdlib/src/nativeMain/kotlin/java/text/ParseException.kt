package java.text

actual open class ParseException actual constructor(
    s: String?,
    actual val errorOffset: Int
) : Exception(s)
