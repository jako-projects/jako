@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.file

expect class FileSystems {
    companion object {
        fun getDefault(): FileSystem
    }
}
