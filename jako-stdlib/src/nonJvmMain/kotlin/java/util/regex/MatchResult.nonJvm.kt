package java.util.regex

actual interface MatchResult {
    actual fun start(): Int
    actual fun start(group: Int): Int
    actual fun end(): Int
    actual fun end(group: Int): Int
    actual fun group(): String
    actual fun group(group: Int): String?
    actual fun groupCount(): Int
}
