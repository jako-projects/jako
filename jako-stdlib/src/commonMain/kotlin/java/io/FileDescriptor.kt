@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect class FileDescriptor() {
    fun valid(): Boolean
    fun sync()

    companion object {
        val `in`: FileDescriptor
        val out: FileDescriptor
        val err: FileDescriptor
    }
}
