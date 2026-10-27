package java.security

import java.math.BigInteger
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.*

internal object DerCodec {
    val OID_RSA_ENCRYPTION = byteArrayOf(
        0x06, 0x09, 0x2A, 0x86.toByte(), 0x48, 0x86.toByte(), 0xF7.toByte(), 0x0D, 0x01, 0x01, 0x01
    )
    val OID_EC_PUBLIC_KEY = byteArrayOf(
        0x06, 0x07, 0x2A, 0x86.toByte(), 0x48, 0xCE.toByte(), 0x3D, 0x02, 0x01
    )
    val OID_SECP256R1 = byteArrayOf(
        0x06, 0x08, 0x2A, 0x86.toByte(), 0x48, 0xCE.toByte(), 0x3D, 0x03, 0x01, 0x07
    )
    val DER_NULL = byteArrayOf(0x05, 0x00)

    fun encodeLength(len: Int): ByteArray {
        if (len < 128) return byteArrayOf(len.toByte())
        val bytes = mutableListOf<Byte>()
        var v = len
        while (v > 0) {
            bytes.add(0, (v and 0xFF).toByte())
            v = v ushr 8
        }
        val out = ByteArray(1 + bytes.size)
        out[0] = (0x80 or bytes.size).toByte()
        for (i in bytes.indices) out[1 + i] = bytes[i]
        return out
    }

    fun encodeTlv(tag: Int, content: ByteArray): ByteArray {
        val lenBytes = encodeLength(content.size)
        val out = ByteArray(1 + lenBytes.size + content.size)
        out[0] = tag.toByte()
        lenBytes.copyInto(out, 1)
        content.copyInto(out, 1 + lenBytes.size)
        return out
    }

    fun encodeSequence(vararg elements: ByteArray): ByteArray {
        var total = 0
        for (e in elements) total += e.size
        val content = ByteArray(total)
        var pos = 0
        for (e in elements) {
            e.copyInto(content, pos)
            pos += e.size
        }
        return encodeTlv(0x30, content)
    }

    fun encodeInteger(v: BigInteger): ByteArray = encodeTlv(0x02, v.toByteArray())

    fun encodeOctetString(bytes: ByteArray): ByteArray = encodeTlv(0x04, bytes)

    fun encodeBitString(bytes: ByteArray): ByteArray {
        val content = ByteArray(1 + bytes.size)
        content[0] = 0
        bytes.copyInto(content, 1)
        return encodeTlv(0x03, content)
    }

    class DerNode(val tag: Int, val content: ByteArray)

    fun readTlv(data: ByteArray, offset: Int): Pair<DerNode, Int> {
        if (offset >= data.size) throw IllegalArgumentException("Unexpected EOF in DER")
        val tag = data[offset].toInt() and 0xFF
        var pos = offset + 1
        if (pos >= data.size) throw IllegalArgumentException("Unexpected EOF in DER length")
        val firstLen = data[pos++].toInt() and 0xFF
        val len: Int
        if ((firstLen and 0x80) == 0) {
            len = firstLen
        } else {
            val numBytes = firstLen and 0x7F
            if (numBytes == 0 || numBytes > 4 || pos + numBytes > data.size) {
                throw IllegalArgumentException("Invalid DER length encoding")
            }
            var acc = 0
            for (i in 0 until numBytes) {
                acc = (acc shl 8) or (data[pos++].toInt() and 0xFF)
            }
            len = acc
        }
        if (pos + len > data.size) throw IllegalArgumentException("DER content exceeds buffer")
        val content = data.copyOfRange(pos, pos + len)
        return Pair(DerNode(tag, content), pos + len)
    }

    fun parseSequence(data: ByteArray): List<DerNode> {
        val (root, end) = readTlv(data, 0)
        if (root.tag != 0x30) throw IllegalArgumentException("Expected DER SEQUENCE (0x30), got 0x${root.tag.toString(16)}")
        val list = mutableListOf<DerNode>()
        var pos = 0
        while (pos < root.content.size) {
            val (child, nextPos) = readTlv(root.content, pos)
            list.add(child)
            pos = nextPos
        }
        return list
    }

    fun toUnsignedBytes(v: BigInteger, length: Int): ByteArray {
        val raw = v.toByteArray()
        if (raw.size == length) return raw
        if (raw.size == length + 1 && raw[0] == 0.toByte()) {
            return raw.copyOfRange(1, raw.size)
        }
        if (raw.size < length) {
            val out = ByteArray(length)
            raw.copyInto(out, length - raw.size)
            return out
        }
        return raw.copyOfRange(raw.size - length, raw.size)
    }
}

internal class NativeRSAPublicKey(
    private val mod: BigInteger,
    private val pubExp: BigInteger
) : RSAPublicKey {
    override fun getAlgorithm(): String = "RSA"
    override val algorithm: String get() = "RSA"
    override fun getFormat(): String = "X.509"
    override val format: String get() = "X.509"

    override fun getModulus(): BigInteger = mod
    override val modulus: BigInteger get() = mod
    override fun getPublicExponent(): BigInteger = pubExp
    override val publicExponent: BigInteger get() = pubExp

    override fun getEncoded(): ByteArray {
        val rsaPubKeySeq = DerCodec.encodeSequence(
            DerCodec.encodeInteger(mod),
            DerCodec.encodeInteger(pubExp)
        )
        val algId = DerCodec.encodeSequence(DerCodec.OID_RSA_ENCRYPTION, DerCodec.DER_NULL)
        return DerCodec.encodeSequence(algId, DerCodec.encodeBitString(rsaPubKeySeq))
    }

    override val encoded: ByteArray get() = getEncoded()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RSAPublicKey) return false
        return mod == other.getModulus() && pubExp == other.getPublicExponent()
    }

    override fun hashCode(): Int = mod.hashCode() xor pubExp.hashCode()
}

internal class NativeRSAPrivateKey(
    private val mod: BigInteger,
    private val privExp: BigInteger,
    private val pubExp: BigInteger = BigInteger.valueOf(65537L),
    private val primeP: BigInteger = BigInteger.ZERO,
    private val primeQ: BigInteger = BigInteger.ZERO,
    private val expP: BigInteger = BigInteger.ZERO,
    private val expQ: BigInteger = BigInteger.ZERO,
    private val crtCoeff: BigInteger = BigInteger.ZERO
) : RSAPrivateKey {
    override fun getAlgorithm(): String = "RSA"
    override val algorithm: String get() = "RSA"
    override fun getFormat(): String = "PKCS#8"
    override val format: String get() = "PKCS#8"

    override fun getModulus(): BigInteger = mod
    override val modulus: BigInteger get() = mod
    override fun getPrivateExponent(): BigInteger = privExp
    override val privateExponent: BigInteger get() = privExp

    override fun getEncoded(): ByteArray {
        val rsaPrivSeq = DerCodec.encodeSequence(
            DerCodec.encodeInteger(BigInteger.ZERO),
            DerCodec.encodeInteger(mod),
            DerCodec.encodeInteger(pubExp),
            DerCodec.encodeInteger(privExp),
            DerCodec.encodeInteger(primeP),
            DerCodec.encodeInteger(primeQ),
            DerCodec.encodeInteger(expP),
            DerCodec.encodeInteger(expQ),
            DerCodec.encodeInteger(crtCoeff)
        )
        val algId = DerCodec.encodeSequence(DerCodec.OID_RSA_ENCRYPTION, DerCodec.DER_NULL)
        return DerCodec.encodeSequence(
            DerCodec.encodeInteger(BigInteger.ZERO),
            algId,
            DerCodec.encodeOctetString(rsaPrivSeq)
        )
    }

    override val encoded: ByteArray get() = getEncoded()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is RSAPrivateKey) return false
        return mod == other.getModulus() && privExp == other.getPrivateExponent()
    }

    override fun hashCode(): Int = mod.hashCode() xor privExp.hashCode()
}

internal class NativeECPublicKey(
    val x: BigInteger,
    val y: BigInteger
) : PublicKey {
    override fun getAlgorithm(): String = "EC"
    override val algorithm: String get() = "EC"
    override fun getFormat(): String = "X.509"
    override val format: String get() = "X.509"

    override fun getEncoded(): ByteArray {
        val pointBytes = ByteArray(65)
        pointBytes[0] = 0x04
        DerCodec.toUnsignedBytes(x, 32).copyInto(pointBytes, 1)
        DerCodec.toUnsignedBytes(y, 32).copyInto(pointBytes, 33)
        val algId = DerCodec.encodeSequence(DerCodec.OID_EC_PUBLIC_KEY, DerCodec.OID_SECP256R1)
        return DerCodec.encodeSequence(algId, DerCodec.encodeBitString(pointBytes))
    }

    override val encoded: ByteArray get() = getEncoded()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PublicKey) return false
        return getEncoded().contentEquals(other.getEncoded())
    }

    override fun hashCode(): Int = x.hashCode() xor y.hashCode()
}

internal class NativeECPrivateKey(
    val s: BigInteger
) : PrivateKey {
    override fun getAlgorithm(): String = "EC"
    override val algorithm: String get() = "EC"
    override fun getFormat(): String = "PKCS#8"
    override val format: String get() = "PKCS#8"

    override fun getEncoded(): ByteArray {
        val sBytes = DerCodec.toUnsignedBytes(s, 32)
        val ecPrivKeySeq = DerCodec.encodeSequence(
            DerCodec.encodeInteger(BigInteger.ONE),
            DerCodec.encodeOctetString(sBytes)
        )
        val algId = DerCodec.encodeSequence(DerCodec.OID_EC_PUBLIC_KEY, DerCodec.OID_SECP256R1)
        return DerCodec.encodeSequence(
            DerCodec.encodeInteger(BigInteger.ZERO),
            algId,
            DerCodec.encodeOctetString(ecPrivKeySeq)
        )
    }

    override val encoded: ByteArray get() = getEncoded()

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is PrivateKey) return false
        return getEncoded().contentEquals(other.getEncoded())
    }

    override fun hashCode(): Int = s.hashCode()
}

actual open class KeyFactory private constructor(
    private val alg: String
) {
    actual fun getAlgorithm(): String = alg
    actual val algorithm: String get() = alg

    actual fun generatePublic(keySpec: KeySpec): PublicKey {
        try {
            when (alg.uppercase()) {
                "RSA" -> {
                    when (keySpec) {
                        is RSAPublicKeySpec -> return NativeRSAPublicKey(keySpec.getModulus(), keySpec.getPublicExponent())
                        is X509EncodedKeySpec -> {
                            val top = DerCodec.parseSequence(keySpec.getEncoded())
                            if (top.size < 2 || top[1].tag != 0x03) throw InvalidKeySpecException("Invalid X.509 RSA public key")
                            val bitString = top[1].content
                            if (bitString.isEmpty()) throw InvalidKeySpecException("Empty BIT STRING")
                            val rsaSeq = DerCodec.parseSequence(bitString.copyOfRange(1, bitString.size))
                            if (rsaSeq.size < 2) throw InvalidKeySpecException("Invalid RSAPublicKey sequence")
                            val n = BigInteger(rsaSeq[0].content)
                            val e = BigInteger(rsaSeq[1].content)
                            return NativeRSAPublicKey(n, e)
                        }
                        else -> throw InvalidKeySpecException("Unsupported KeySpec for RSA public key")
                    }
                }
                "EC" -> {
                    if (keySpec is X509EncodedKeySpec) {
                        val top = DerCodec.parseSequence(keySpec.getEncoded())
                        if (top.size < 2 || top[1].tag != 0x03) throw InvalidKeySpecException("Invalid X.509 EC public key")
                        val bitString = top[1].content
                        if (bitString.size < 66 || bitString[1] != 0x04.toByte()) {
                            throw InvalidKeySpecException("Expected uncompressed EC point (65 bytes)")
                        }
                        val x = BigInteger(1, bitString.copyOfRange(2, 34))
                        val y = BigInteger(1, bitString.copyOfRange(34, 66))
                        return NativeECPublicKey(x, y)
                    }
                    throw InvalidKeySpecException("Unsupported KeySpec for EC public key")
                }
                else -> throw InvalidKeySpecException("Unsupported algorithm: $alg")
            }
        } catch (e: InvalidKeySpecException) {
            throw e
        } catch (e: Exception) {
            throw InvalidKeySpecException("Failed to parse public key: ${e.message}", e)
        }
    }

    actual fun generatePrivate(keySpec: KeySpec): PrivateKey {
        try {
            when (alg.uppercase()) {
                "RSA" -> {
                    when (keySpec) {
                        is RSAPrivateKeySpec -> return NativeRSAPrivateKey(keySpec.getModulus(), keySpec.getPrivateExponent())
                        is PKCS8EncodedKeySpec -> {
                            val top = DerCodec.parseSequence(keySpec.getEncoded())
                            if (top.size < 3 || top[2].tag != 0x04) throw InvalidKeySpecException("Invalid PKCS#8 RSA private key")
                            val rsaSeq = DerCodec.parseSequence(top[2].content)
                            if (rsaSeq.size < 4) throw InvalidKeySpecException("Invalid RSAPrivateKey sequence")
                            val n = BigInteger(rsaSeq[1].content)
                            val e = BigInteger(rsaSeq[2].content)
                            val d = BigInteger(rsaSeq[3].content)
                            val p = if (rsaSeq.size > 4) BigInteger(rsaSeq[4].content) else BigInteger.ZERO
                            val q = if (rsaSeq.size > 5) BigInteger(rsaSeq[5].content) else BigInteger.ZERO
                            val dP = if (rsaSeq.size > 6) BigInteger(rsaSeq[6].content) else BigInteger.ZERO
                            val dQ = if (rsaSeq.size > 7) BigInteger(rsaSeq[7].content) else BigInteger.ZERO
                            val qInv = if (rsaSeq.size > 8) BigInteger(rsaSeq[8].content) else BigInteger.ZERO
                            return NativeRSAPrivateKey(n, d, e, p, q, dP, dQ, qInv)
                        }
                        else -> throw InvalidKeySpecException("Unsupported KeySpec for RSA private key")
                    }
                }
                "EC" -> {
                    if (keySpec is PKCS8EncodedKeySpec) {
                        val top = DerCodec.parseSequence(keySpec.getEncoded())
                        if (top.size < 3 || top[2].tag != 0x04) throw InvalidKeySpecException("Invalid PKCS#8 EC private key")
                        val ecSeq = DerCodec.parseSequence(top[2].content)
                        if (ecSeq.size < 2 || ecSeq[1].tag != 0x04) throw InvalidKeySpecException("Invalid ECPrivateKey structure")
                        val s = BigInteger(1, ecSeq[1].content)
                        return NativeECPrivateKey(s)
                    }
                    throw InvalidKeySpecException("Unsupported KeySpec for EC private key")
                }
                else -> throw InvalidKeySpecException("Unsupported algorithm: $alg")
            }
        } catch (e: InvalidKeySpecException) {
            throw e
        } catch (e: Exception) {
            throw InvalidKeySpecException("Failed to parse private key: ${e.message}", e)
        }
    }

    @Suppress("UNCHECKED_CAST")
    actual fun <T : KeySpec> getKeySpec(key: Key, keySpec: Class<T>): T {
        val specName = keySpec.getName()
        if (specName.endsWith("X509EncodedKeySpec") && key is PublicKey) {
            return X509EncodedKeySpec(key.getEncoded() ?: throw InvalidKeySpecException("Null encoded key")) as T
        }
        if (specName.endsWith("PKCS8EncodedKeySpec") && key is PrivateKey) {
            return PKCS8EncodedKeySpec(key.getEncoded() ?: throw InvalidKeySpecException("Null encoded key")) as T
        }
        if (specName.endsWith("RSAPublicKeySpec") && key is RSAPublicKey) {
            return RSAPublicKeySpec(key.getModulus(), key.getPublicExponent()) as T
        }
        if (specName.endsWith("RSAPrivateKeySpec") && key is RSAPrivateKey) {
            return RSAPrivateKeySpec(key.getModulus(), key.getPrivateExponent()) as T
        }
        throw InvalidKeySpecException("Unsupported KeySpec: $specName")
    }

    actual fun translateKey(key: Key): Key {
        val enc = key.getEncoded() ?: throw InvalidKeyException("Key does not support encoding")
        return when (key) {
            is PublicKey -> generatePublic(X509EncodedKeySpec(enc))
            is PrivateKey -> generatePrivate(PKCS8EncodedKeySpec(enc))
            else -> throw InvalidKeyException("Unsupported key type")
        }
    }

    actual companion object {
        actual fun getInstance(algorithm: String): KeyFactory {
            return when (algorithm.uppercase()) {
                "RSA", "EC" -> KeyFactory(algorithm)
                else -> throw NoSuchAlgorithmException("Unsupported KeyFactory algorithm: $algorithm")
            }
        }
    }
}
