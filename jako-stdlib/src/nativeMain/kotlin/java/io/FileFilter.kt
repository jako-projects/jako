package java.io

actual fun interface FileFilter {
    actual fun accept(pathname: File): Boolean
}
