@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang

import java.io.Serializable
import kotlin.Int

/**
 * [Nullability Decision] declaringClass and methodName are non-null (Java throws NPE if null);
 * fileName is nullable (null indicates Unknown Source).
 */
expect class StackTraceElement(
    declaringClass: String,
    methodName: String,
    fileName: String?,
    lineNumber: Int
) : Serializable {
    fun getClassName(): String
    fun getMethodName(): String
    fun getFileName(): String?
    fun getLineNumber(): Int
    fun isNativeMethod(): Boolean

    val className: String
    val methodName: String
    val fileName: String?
    val lineNumber: Int
    val isNativeMethod: Boolean

    override fun toString(): String
    override fun equals(other: Any?): Boolean
    override fun hashCode(): Int
}
