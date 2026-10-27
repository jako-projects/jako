package javax.crypto.spec

import java.security.spec.KeySpec

actual open class PBEKeySpec : KeySpec {
    private var passwd: CharArray?
    private val saltBytes: ByteArray?
    private val iterCount: Int
    private val keyLen: Int

    actual constructor(password: CharArray?) {
        this.passwd = password?.copyOf() ?: CharArray(0)
        this.saltBytes = null
        this.iterCount = 0
        this.keyLen = 0
    }

    actual constructor(password: CharArray?, salt: ByteArray, iterationCount: Int, keyLength: Int) {
        if (salt.isEmpty()) throw IllegalArgumentException("the salt parameter must not be empty")
        if (iterationCount <= 0) throw IllegalArgumentException("invalid iterationCount value")
        if (keyLength <= 0) throw IllegalArgumentException("invalid keyLength value")
        this.passwd = password?.copyOf() ?: CharArray(0)
        this.saltBytes = salt.copyOf()
        this.iterCount = iterationCount
        this.keyLen = keyLength
    }

    actual constructor(password: CharArray?, salt: ByteArray, iterationCount: Int) {
        if (salt.isEmpty()) throw IllegalArgumentException("the salt parameter must not be empty")
        if (iterationCount <= 0) throw IllegalArgumentException("invalid iterationCount value")
        this.passwd = password?.copyOf() ?: CharArray(0)
        this.saltBytes = salt.copyOf()
        this.iterCount = iterationCount
        this.keyLen = 0
    }

    actual fun clearPassword() {
        passwd?.fill(' ')
        passwd = null
    }

    actual fun getPassword(): CharArray {
        val p = passwd ?: throw IllegalStateException("password has been cleared")
        return p.copyOf()
    }

    actual val password: CharArray get() = getPassword()

    actual fun getSalt(): ByteArray? = saltBytes?.copyOf()
    actual val salt: ByteArray? get() = getSalt()

    actual fun getIterationCount(): Int = iterCount
    actual val iterationCount: Int get() = iterCount

    actual fun getKeyLength(): Int = keyLen
    actual val keyLength: Int get() = keyLen
}
