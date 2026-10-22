package java.security

import java.math.BigInteger
import java.security.spec.*

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
