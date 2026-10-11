package java.lang

/**
 * Marker interface matching JDK 8 java.lang.Cloneable.
 * On JVM, clone() is inherited from Object; on Native, we declare it here
 * so interfaces extending Cloneable can override it.
 */
public actual interface Cloneable {
    fun clone(): Any = throw CloneNotSupportedException()
}

actual open class CloneNotSupportedException : Exception {
    actual constructor() : super()
    actual constructor(message: String?) : super(message)
}
