@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.charset

expect class CodingErrorAction {
    override fun toString(): String

    companion object {
        val IGNORE: CodingErrorAction
        val REPLACE: CodingErrorAction
        val REPORT: CodingErrorAction
    }
}
