@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.nio.charset

import java.io.IOException

expect open class CharacterCodingException : IOException {
    constructor()
}
