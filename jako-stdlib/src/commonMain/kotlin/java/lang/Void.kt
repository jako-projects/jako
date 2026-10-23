@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.lang

expect class Void {
    companion object {
        val TYPE: Class<Void>
    }
}
