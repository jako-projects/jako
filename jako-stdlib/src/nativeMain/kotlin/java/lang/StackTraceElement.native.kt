package java.lang

import java.io.Serializable

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
