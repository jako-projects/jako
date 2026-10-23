@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect fun interface FilenameFilter {
    fun accept(dir: File, name: String): Boolean
}
