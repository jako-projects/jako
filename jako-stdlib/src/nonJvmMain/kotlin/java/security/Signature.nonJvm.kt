package java.security

import java.io.ByteArrayOutputStream
import java.math.BigInteger
import java.nio.ByteBuffer
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.*

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
