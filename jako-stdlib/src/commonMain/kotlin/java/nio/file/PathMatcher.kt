@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.file

expect fun interface PathMatcher {
    fun matches(path: Path): Boolean
}
