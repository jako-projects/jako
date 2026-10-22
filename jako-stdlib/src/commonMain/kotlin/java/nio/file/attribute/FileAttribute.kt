@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.file.attribute

expect interface FileAttribute<T> {
    fun name(): String
    fun value(): T
}
