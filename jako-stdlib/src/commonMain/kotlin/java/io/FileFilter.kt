@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.io

expect fun interface FileFilter {
    fun accept(pathname: File): Boolean
}
