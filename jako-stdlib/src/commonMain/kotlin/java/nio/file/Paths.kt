@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.file

import java.net.URI

expect class Paths {
    companion object {
        fun get(first: String, vararg more: String): Path
        fun get(uri: URI): Path
    }
}
