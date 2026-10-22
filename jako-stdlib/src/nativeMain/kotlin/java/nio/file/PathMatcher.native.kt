package java.nio.file

actual fun interface PathMatcher {
    actual fun matches(path: Path): Boolean
}
