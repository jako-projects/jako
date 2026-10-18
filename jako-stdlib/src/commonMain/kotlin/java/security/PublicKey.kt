@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.security

import java.io.Serializable

expect interface PublicKey : Key

expect interface PrivateKey : Key

/**
 * [Nullability Decision] KeyPair allows nullable PublicKey and PrivateKey in JDK 8 constructor.
 */
expect class KeyPair(publicKey: PublicKey?, privateKey: PrivateKey?) : Serializable {
    fun getPublic(): PublicKey?
    val `public`: PublicKey?
    fun getPrivate(): PrivateKey?
    val `private`: PrivateKey?
}
