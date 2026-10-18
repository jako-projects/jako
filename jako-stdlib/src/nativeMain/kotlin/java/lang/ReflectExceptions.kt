package java.lang

import java.io.Serializable

actual open class NoSuchFieldException : Exception {
    actual constructor() : super()
    actual constructor(s: String?) : super(s)
}

actual open class SecurityException : RuntimeException {
    actual constructor() : super()
    actual constructor(s: String?) : super(s)
    actual constructor(message: String?, cause: Throwable?) : super(message, cause)
    actual constructor(cause: Throwable?) : super(cause)
}

actual open class IllegalMonitorStateException : RuntimeException {
    actual constructor() : super()
    actual constructor(s: String?) : super(s)
}

actual open class NegativeArraySizeException : RuntimeException {
    actual constructor() : super()
    actual constructor(s: String?) : super(s)
}

actual open class ArrayStoreException : RuntimeException {
    actual constructor() : super()
    actual constructor(s: String?) : super(s)
}

actual open class StringIndexOutOfBoundsException : IndexOutOfBoundsException {
    actual constructor() : super()
    actual constructor(s: String?) : super(s)
    actual constructor(index: Int) : super("String index out of range: $index")
}

actual class StackTraceElement actual constructor(
    declaringClass: String,
    actual val methodName: String,
    actual val fileName: String?,
    actual val lineNumber: Int
) : Serializable {
    private val declaringClassVal: String = declaringClass

    actual fun getClassName(): String = declaringClassVal
    actual fun getMethodName(): String = methodName
    actual fun getFileName(): String? = fileName
    actual fun getLineNumber(): Int = lineNumber
    actual fun isNativeMethod(): Boolean = lineNumber == -2

    actual val className: String get() = declaringClassVal
    actual val isNativeMethod: Boolean get() = isNativeMethod()

    actual override fun toString(): String {
        return when {
            isNativeMethod() -> "$declaringClassVal.$methodName(Native Method)"
            fileName != null && lineNumber >= 0 -> "$declaringClassVal.$methodName($fileName:$lineNumber)"
            fileName != null -> "$declaringClassVal.$methodName($fileName)"
            else -> "$declaringClassVal.$methodName(Unknown Source)"
        }
    }

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is StackTraceElement) return false
        return declaringClassVal == other.declaringClassVal &&
            lineNumber == other.lineNumber &&
            methodName == other.methodName &&
            fileName == other.fileName
    }

    actual override fun hashCode(): Int {
        var result = 31 * declaringClassVal.hashCode() + methodName.hashCode()
        result = 31 * result + (fileName?.hashCode() ?: 0)
        result = 31 * result + lineNumber
        return result
    }
}
