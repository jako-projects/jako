package java.io

actual fun interface FilenameFilter {
    actual fun accept(dir: File, name: String): Boolean
}
