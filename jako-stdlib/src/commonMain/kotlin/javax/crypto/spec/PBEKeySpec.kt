@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package javax.crypto.spec

import java.security.spec.KeySpec

/**
 * [Nullability Decision] PBEKeySpec accepts nullable CharArray password (treated as empty char array in JDK 8)
 * and returns nullable ByteArray? from getSalt() when constructed with password-only constructor.
 */
expect open class PBEKeySpec : KeySpec {
    constructor(password: CharArray?)
    constructor(password: CharArray?, salt: ByteArray, iterationCount: Int, keyLength: Int)
    constructor(password: CharArray?, salt: ByteArray, iterationCount: Int)

    fun clearPassword()
    fun getPassword(): CharArray
    val password: CharArray
    fun getSalt(): ByteArray?
    val salt: ByteArray?
    fun getIterationCount(): Int
    val iterationCount: Int
    fun getKeyLength(): Int
    val keyLength: Int
}
