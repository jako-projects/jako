package java.security

import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.nio.ByteBuffer
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

internal object P256Curve {
    val P = BigInteger("FFFFFFFF00000001000000000000000000000000FFFFFFFFFFFFFFFFFFFFFFFF", 16)
    val A = P.subtract(BigInteger.valueOf(3L))
    val B = BigInteger("5AC635D8AA3A93E7B3EBBD55769886BC651D06B0CC53B0F63BCE3C3E27D2604B", 16)
    val N = BigInteger("FFFFFFFF00000000FFFFFFFFFFFFFFFFBCE6FAADA7179E84F3B9CAC2FC632551", 16)
    val GX = BigInteger("6B17D1F2E12C4247F8BCE6E563A440F277037D812DEB33A0F4A13945D898C296", 16)
    val GY = BigInteger("4FE342E2FE1A7F9B8EE7EB4A7C0F9E162BCE33576B315ECECBB6406837BF51F5", 16)
    private val TWO = BigInteger.valueOf(2L)
    private val THREE = BigInteger.valueOf(3L)
    private val FOUR = BigInteger.valueOf(4L)
    private val EIGHT = BigInteger.valueOf(8L)

    data class AffinePoint(val x: BigInteger, val y: BigInteger, val infinity: Boolean = false) {
        companion object {
            val INFINITY = AffinePoint(BigInteger.ZERO, BigInteger.ZERO, true)
        }
    }

    private class JacobianPoint(val x: BigInteger, val y: BigInteger, val z: BigInteger) {
        val isInfinity: Boolean get() = z.signum() == 0

        companion object {
            val INFINITY = JacobianPoint(BigInteger.ONE, BigInteger.ONE, BigInteger.ZERO)
        }
    }

    private fun modP(v: BigInteger): BigInteger {
        val r = v.remainder(P)
        return if (r.signum() < 0) r.add(P) else r
    }

    private fun doubleJacobian(pt: JacobianPoint): JacobianPoint {
        if (pt.isInfinity || pt.y.signum() == 0) return JacobianPoint.INFINITY
        val x1 = pt.x
        val y1 = pt.y
        val z1 = pt.z

        val z1Sq = modP(z1.multiply(z1))
        // On P-256, a = -3, so 3*X1^2 + a*Z1^4 = 3*(X1 - Z1^2)*(X1 + Z1^2)
        val m = modP(THREE.multiply(modP(x1.subtract(z1Sq)).multiply(modP(x1.add(z1Sq)))))
        val y1Sq = modP(y1.multiply(y1))
        val s = modP(FOUR.multiply(x1).multiply(y1Sq))
        val x3 = modP(m.multiply(m).subtract(TWO.multiply(s)))
        val y1Fourth = modP(y1Sq.multiply(y1Sq))
        val y3 = modP(m.multiply(s.subtract(x3)).subtract(EIGHT.multiply(y1Fourth)))
        val z3 = modP(TWO.multiply(y1).multiply(z1))
        return JacobianPoint(x3, y3, z3)
    }

    private fun addJacobian(p1: JacobianPoint, p2: JacobianPoint): JacobianPoint {
        if (p1.isInfinity) return p2
        if (p2.isInfinity) return p1

        val z1z1 = modP(p1.z.multiply(p1.z))
        val z2z2 = modP(p2.z.multiply(p2.z))
        val u1 = modP(p1.x.multiply(z2z2))
        val u2 = modP(p2.x.multiply(z1z1))
        val s1 = modP(p1.y.multiply(p2.z).multiply(z2z2))
        val s2 = modP(p2.y.multiply(p1.z).multiply(z1z1))

        if (u1 == u2) {
            return if (s1 == s2) doubleJacobian(p1) else JacobianPoint.INFINITY
        }

        val h = modP(u2.subtract(u1))
        val r = modP(s2.subtract(s1))
        val hh = modP(h.multiply(h))
        val hhh = modP(h.multiply(hh))
        val u1hh = modP(u1.multiply(hh))

        val x3 = modP(r.multiply(r).subtract(hhh).subtract(TWO.multiply(u1hh)))
        val y3 = modP(r.multiply(u1hh.subtract(x3)).subtract(s1.multiply(hhh)))
        val z3 = modP(h.multiply(p1.z).multiply(p2.z))
        return JacobianPoint(x3, y3, z3)
    }

    private fun toAffine(pt: JacobianPoint): AffinePoint {
        if (pt.isInfinity) return AffinePoint.INFINITY
        val zInv = pt.z.modInverse(P)
        val zInv2 = modP(zInv.multiply(zInv))
        val zInv3 = modP(zInv2.multiply(zInv))
        return AffinePoint(modP(pt.x.multiply(zInv2)), modP(pt.y.multiply(zInv3)))
    }

    fun multiply(pt: AffinePoint, k: BigInteger): AffinePoint {
        if (pt.infinity || k.signum() == 0) return AffinePoint.INFINITY
        val scalar = k.mod(N)
        if (scalar.signum() == 0) return AffinePoint.INFINITY
        val baseJ = JacobianPoint(pt.x, pt.y, BigInteger.ONE)
        var acc = JacobianPoint.INFINITY
        val bytes = scalar.toByteArray()
        var started = false
        for (b in bytes) {
            val ub = b.toInt() and 0xFF
            for (bit in 7 downTo 0) {
                val isSet = ((ub ushr bit) and 1) != 0
                if (!started) {
                    if (isSet) {
                        started = true
                        acc = baseJ
                    }
                } else {
                    acc = doubleJacobian(acc)
                    if (isSet) {
                        acc = addJacobian(acc, baseJ)
                    }
                }
            }
        }
        return toAffine(acc)
    }

    fun add(p1: AffinePoint, p2: AffinePoint): AffinePoint {
        if (p1.infinity) return p2
        if (p2.infinity) return p1
        val j1 = JacobianPoint(p1.x, p1.y, BigInteger.ONE)
        val j2 = JacobianPoint(p2.x, p2.y, BigInteger.ONE)
        return toAffine(addJacobian(j1, j2))
    }

    val G = AffinePoint(GX, GY)
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

actual abstract class KeyPairGenerator internal constructor(
    private val alg: String
) {
    protected var keySize: Int = if (alg.equals("EC", ignoreCase = true)) 256 else 1024
    protected var random: SecureRandom = SecureRandom()

    actual open fun getAlgorithm(): String = alg
    actual open val algorithm: String get() = alg

    actual open fun initialize(keysize: Int) {
        initialize(keysize, SecureRandom())
    }

    actual open fun initialize(keysize: Int, random: SecureRandom) {
        if (alg.equals("RSA", ignoreCase = true)) {
            if (keysize < 512) throw InvalidParameterException("RSA keys must be at least 512 bits long")
        } else if (alg.equals("EC", ignoreCase = true)) {
            if (keysize != 256) throw InvalidParameterException("Supported EC keysize is 256 bits (secp256r1)")
        }
        this.keySize = keysize
        this.random = random
    }

    actual open fun initialize(params: AlgorithmParameterSpec) {
        initialize(params, SecureRandom())
    }

    actual open fun initialize(params: AlgorithmParameterSpec, random: SecureRandom) {
        if (alg.equals("EC", ignoreCase = true) && params is ECGenParameterSpec) {
            val curve = params.getName().lowercase()
            if (curve != "secp256r1" && curve != "prime256v1" && curve != "1.2.840.10045.3.1.7") {
                throw InvalidAlgorithmParameterException("Unsupported EC curve: ${params.getName()}")
            }
            this.keySize = 256
            this.random = random
        } else {
            throw InvalidAlgorithmParameterException("Unsupported AlgorithmParameterSpec")
        }
    }

    actual fun genKeyPair(): KeyPair = generateKeyPair()

    actual open fun generateKeyPair(): KeyPair {
        when (alg.uppercase()) {
            "RSA" -> {
                val e = BigInteger.valueOf(65537L)
                val pBits = (keySize + 1) / 2
                val qBits = keySize - pBits
                var p: BigInteger
                var q: BigInteger
                var n: BigInteger
                var phi: BigInteger
                while (true) {
                    p = BigInteger.probablePrime(pBits, random)
                    q = BigInteger.probablePrime(qBits, random)
                    if (p == q) continue
                    if (p < q) {
                        val tmp = p
                        p = q
                        q = tmp
                    }
                    n = p.multiply(q)
                    if (n.bitLength() != keySize) continue
                    val p1 = p.subtract(BigInteger.ONE)
                    val q1 = q.subtract(BigInteger.ONE)
                    phi = p1.multiply(q1)
                    if (e.gcd(phi) == BigInteger.ONE) {
                        val d = e.modInverse(phi)
                        val dP = d.mod(p1)
                        val dQ = d.mod(q1)
                        val qInv = q.modInverse(p)
                        return KeyPair(
                            NativeRSAPublicKey(n, e),
                            NativeRSAPrivateKey(n, d, e, p, q, dP, dQ, qInv)
                        )
                    }
                }
            }
            "EC" -> {
                val nMinusTwo = P256Curve.N.subtract(BigInteger.valueOf(2L))
                var d: BigInteger
                while (true) {
                    val candidate = BigInteger(256, random)
                    if (candidate >= BigInteger.ONE && candidate <= nMinusTwo) {
                        d = candidate
                        break
                    }
                }
                val pubPt = P256Curve.multiply(P256Curve.G, d)
                return KeyPair(NativeECPublicKey(pubPt.x, pubPt.y), NativeECPrivateKey(d))
            }
            else -> throw IllegalStateException("Unsupported algorithm: $alg")
        }
    }

    actual companion object {
        actual fun getInstance(algorithm: String): KeyPairGenerator {
            return when (algorithm.uppercase()) {
                "RSA", "EC" -> object : KeyPairGenerator(algorithm) {}
                else -> throw NoSuchAlgorithmException("Unsupported KeyPairGenerator algorithm: $algorithm")
            }
        }
    }
}

actual abstract class Signature internal constructor(
    private val alg: String,
    private val digestAlg: String?,
    private val cipherAlg: String
) {
    private var mode: Int = 0 // 1 = SIGN, 2 = VERIFY
    private var publicKey: PublicKey? = null
    private var privateKey: PrivateKey? = null
    private var random: SecureRandom = SecureRandom()
    private val buffer = ByteArrayOutputStream()

    actual fun getAlgorithm(): String = alg
    actual val algorithm: String get() = alg

    actual fun initVerify(publicKey: PublicKey) {
        if (cipherAlg == "RSA") {
            if (publicKey !is RSAPublicKey) {
                val parsed = KeyFactory.getInstance("RSA").generatePublic(
                    X509EncodedKeySpec(publicKey.getEncoded() ?: throw InvalidKeyException("Invalid RSA public key"))
                )
                this.publicKey = parsed
            } else {
                this.publicKey = publicKey
            }
        } else if (cipherAlg == "ECDSA") {
            if (publicKey !is NativeECPublicKey) {
                val parsed = KeyFactory.getInstance("EC").generatePublic(
                    X509EncodedKeySpec(publicKey.getEncoded() ?: throw InvalidKeyException("Invalid EC public key"))
                )
                this.publicKey = parsed
            } else {
                this.publicKey = publicKey
            }
        }
        this.privateKey = null
        this.mode = 2
        this.buffer.reset()
    }

    actual fun initSign(privateKey: PrivateKey) {
        initSign(privateKey, SecureRandom())
    }

    actual fun initSign(privateKey: PrivateKey, random: SecureRandom) {
        if (cipherAlg == "RSA") {
            if (privateKey !is RSAPrivateKey) {
                val parsed = KeyFactory.getInstance("RSA").generatePrivate(
                    PKCS8EncodedKeySpec(privateKey.getEncoded() ?: throw InvalidKeyException("Invalid RSA private key"))
                )
                this.privateKey = parsed
            } else {
                this.privateKey = privateKey
            }
        } else if (cipherAlg == "ECDSA") {
            if (privateKey !is NativeECPrivateKey) {
                val parsed = KeyFactory.getInstance("EC").generatePrivate(
                    PKCS8EncodedKeySpec(privateKey.getEncoded() ?: throw InvalidKeyException("Invalid EC private key"))
                )
                this.privateKey = parsed
            } else {
                this.privateKey = privateKey
            }
        }
        this.publicKey = null
        this.random = random
        this.mode = 1
        this.buffer.reset()
    }

    actual fun update(b: Byte) {
        if (mode == 0) throw SignatureException("Signature object not initialized")
        buffer.write(b.toInt() and 0xFF)
    }

    actual fun update(data: ByteArray) {
        update(data, 0, data.size)
    }

    actual fun update(data: ByteArray, off: Int, len: Int) {
        if (mode == 0) throw SignatureException("Signature object not initialized")
        if (off < 0 || len < 0 || off + len > data.size) throw IllegalArgumentException("Invalid offset/len")
        buffer.write(data, off, len)
    }

    actual fun update(data: ByteBuffer) {
        if (mode == 0) throw SignatureException("Signature object not initialized")
        val rem = data.remaining()
        if (rem > 0) {
            val tmp = ByteArray(rem)
            data.get(tmp)
            buffer.write(tmp, 0, rem)
        }
    }

    private fun computeDigest(): ByteArray {
        val raw = buffer.toByteArray()
        buffer.reset()
        return if (digestAlg == null) {
            raw
        } else {
            MessageDigest.getInstance(digestAlg).digest(raw)
        }
    }

    private fun buildRsaDigestInfo(hash: ByteArray): ByteArray {
        if (digestAlg == null) return hash
        val prefix = when (digestAlg) {
            "MD5" -> byteArrayOf(
                0x30, 0x20, 0x30, 0x0C, 0x06, 0x08, 0x2A, 0x86.toByte(), 0x48, 0x86.toByte(),
                0xF7.toByte(), 0x0D, 0x02, 0x05, 0x05, 0x00, 0x04, 0x10
            )
            "SHA-1" -> byteArrayOf(
                0x30, 0x21, 0x30, 0x09, 0x06, 0x05, 0x2B, 0x0E, 0x03, 0x02, 0x1A, 0x05, 0x00, 0x04, 0x14
            )
            "SHA-256" -> byteArrayOf(
                0x30, 0x31, 0x30, 0x0D, 0x06, 0x09, 0x60, 0x86.toByte(), 0x48, 0x01,
                0x65, 0x03, 0x04, 0x02, 0x01, 0x05, 0x00, 0x04, 0x20
            )
            "SHA-384" -> byteArrayOf(
                0x30, 0x41, 0x30, 0x0D, 0x06, 0x09, 0x60, 0x86.toByte(), 0x48, 0x01,
                0x65, 0x03, 0x04, 0x02, 0x02, 0x05, 0x00, 0x04, 0x30
            )
            "SHA-512" -> byteArrayOf(
                0x30, 0x51, 0x30, 0x0D, 0x06, 0x09, 0x60, 0x86.toByte(), 0x48, 0x01,
                0x65, 0x03, 0x04, 0x02, 0x03, 0x05, 0x00, 0x04, 0x40
            )
            else -> throw SignatureException("Unsupported RSA digest: $digestAlg")
        }
        val out = ByteArray(prefix.size + hash.size)
        prefix.copyInto(out, 0)
        hash.copyInto(out, prefix.size)
        return out
    }

    private fun buildPkcs1v15EncodedMessage(digestInfo: ByteArray, modulusBytes: Int): ByteArray {
        val psLen = modulusBytes - 3 - digestInfo.size
        if (psLen < 8) throw SignatureException("RSA key size too small for digest")
        val em = ByteArray(modulusBytes)
        em[0] = 0x00
        em[1] = 0x01
        for (i in 0 until psLen) {
            em[2 + i] = 0xFF.toByte()
        }
        em[2 + psLen] = 0x00
        digestInfo.copyInto(em, 3 + psLen)
        return em
    }

    actual fun sign(): ByteArray {
        if (mode != 1) throw SignatureException("Object not initialized for signing")
        val hash = computeDigest()
        if (cipherAlg == "RSA") {
            val priv = privateKey as? RSAPrivateKey ?: throw SignatureException("Missing RSA private key")
            val n = priv.getModulus()
            val d = priv.getPrivateExponent()
            val k = (n.bitLength() + 7) / 8
            val t = buildRsaDigestInfo(hash)
            val em = buildPkcs1v15EncodedMessage(t, k)
            val m = BigInteger(1, em)
            val s = m.modPow(d, n)
            return DerCodec.toUnsignedBytes(s, k)
        } else {
            val priv = privateKey as? NativeECPrivateKey ?: throw SignatureException("Missing EC private key")
            val n = P256Curve.N
            val eHash = if (hash.size > 32) hash.copyOfRange(0, 32) else hash
            val z = BigInteger(1, eHash)
            while (true) {
                val k = BigInteger(256, random).mod(n)
                if (k.signum() <= 0) continue
                val pt = P256Curve.multiply(P256Curve.G, k)
                if (pt.infinity) continue
                val r = pt.x.mod(n)
                if (r.signum() == 0) continue
                val kInv = k.modInverse(n)
                val s = kInv.multiply(z.add(r.multiply(priv.s))).mod(n)
                if (s.signum() == 0) continue
                return DerCodec.encodeSequence(DerCodec.encodeInteger(r), DerCodec.encodeInteger(s))
            }
        }
    }

    actual fun sign(outbuf: ByteArray, offset: Int, len: Int): Int {
        val sig = sign()
        if (offset < 0 || len < sig.size || offset + sig.size > outbuf.size) {
            throw SignatureException("Output buffer too small")
        }
        sig.copyInto(outbuf, offset)
        return sig.size
    }

    actual fun verify(signature: ByteArray): Boolean = verify(signature, 0, signature.size)

    actual fun verify(signature: ByteArray, offset: Int, length: Int): Boolean {
        if (mode != 2) throw SignatureException("Object not initialized for verification")
        if (offset < 0 || length < 0 || offset + length > signature.size) {
            throw IllegalArgumentException("Invalid offset/length")
        }
        val sigBytes = signature.copyOfRange(offset, offset + length)
        val hash = computeDigest()
        return try {
            if (cipherAlg == "RSA") {
                val pub = publicKey as? RSAPublicKey ?: throw SignatureException("Missing RSA public key")
                val n = pub.getModulus()
                val e = pub.getPublicExponent()
                val k = (n.bitLength() + 7) / 8
                if (sigBytes.size != k) return false
                val s = BigInteger(1, sigBytes)
                if (s >= n) return false
                val m = s.modPow(e, n)
                val actualEm = DerCodec.toUnsignedBytes(m, k)
                val expectedEm = buildPkcs1v15EncodedMessage(buildRsaDigestInfo(hash), k)
                MessageDigest.isEqual(actualEm, expectedEm)
            } else {
                val pub = publicKey as? NativeECPublicKey ?: throw SignatureException("Missing EC public key")
                val seq = DerCodec.parseSequence(sigBytes)
                if (seq.size != 2) return false
                val r = BigInteger(seq[0].content)
                val s = BigInteger(seq[1].content)
                val n = P256Curve.N
                if (r < BigInteger.ONE || r >= n || s < BigInteger.ONE || s >= n) return false
                val eHash = if (hash.size > 32) hash.copyOfRange(0, 32) else hash
                val z = BigInteger(1, eHash)
                val w = s.modInverse(n)
                val u1 = z.multiply(w).mod(n)
                val u2 = r.multiply(w).mod(n)
                val p1 = P256Curve.multiply(P256Curve.G, u1)
                val p2 = P256Curve.multiply(P256Curve.AffinePoint(pub.x, pub.y), u2)
                val pt = P256Curve.add(p1, p2)
                if (pt.infinity) return false
                pt.x.mod(n) == r
            }
        } catch (_: Exception) {
            false
        }
    }

    actual companion object {
        actual fun getInstance(algorithm: String): Signature {
            val upper = algorithm.uppercase()
            val (digestAlg, cipherAlg) = when (upper) {
                "NONEWITHRSA" -> Pair(null, "RSA")
                "MD5WITHRSA" -> Pair("MD5", "RSA")
                "SHA1WITHRSA", "SHA-1WITHRSA" -> Pair("SHA-1", "RSA")
                "SHA256WITHRSA", "SHA-256WITHRSA" -> Pair("SHA-256", "RSA")
                "SHA384WITHRSA", "SHA-384WITHRSA" -> Pair("SHA-384", "RSA")
                "SHA512WITHRSA", "SHA-512WITHRSA" -> Pair("SHA-512", "RSA")
                "NONEWITHECDSA" -> Pair(null, "ECDSA")
                "SHA1WITHECDSA", "SHA-1WITHECDSA" -> Pair("SHA-1", "ECDSA")
                "SHA256WITHECDSA", "SHA-256WITHECDSA" -> Pair("SHA-256", "ECDSA")
                "SHA384WITHECDSA", "SHA-384WITHECDSA" -> Pair("SHA-384", "ECDSA")
                "SHA512WITHECDSA", "SHA-512WITHECDSA" -> Pair("SHA-512", "ECDSA")
                else -> throw NoSuchAlgorithmException("Unsupported Signature algorithm: $algorithm")
            }
            return object : Signature(algorithm, digestAlg, cipherAlg) {}
        }
    }
}
