package java.security

import java.nio.ByteBuffer

actual abstract class MessageDigest protected actual constructor(
    actual val algorithm: String
) {
    actual open fun update(input: Byte) {
        update(byteArrayOf(input), 0, 1)
    }

    actual open fun update(input: ByteArray) {
        update(input, 0, input.size)
    }

    actual abstract fun update(input: ByteArray, offset: Int, len: Int)

    actual open fun update(input: ByteBuffer) {
        val rem = input.remaining()
        if (rem <= 0) return
        val temp = ByteArray(rem)
        input.get(temp)
        update(temp, 0, rem)
    }

    actual abstract fun digest(): ByteArray

    actual open fun digest(buf: ByteArray, offset: Int, len: Int): Int {
        if (offset < 0 || len < 0 || buf.size - offset < len) {
            throw IllegalArgumentException("Output buffer too small for specified offset and length")
        }
        val d = digest()
        if (len < d.size) {
            throw DigestException("partial digests not returned")
        }
        d.copyInto(buf, destinationOffset = offset, startIndex = 0, endIndex = d.size)
        return d.size
    }

    actual open fun digest(input: ByteArray): ByteArray {
        update(input)
        return digest()
    }

    actual abstract fun reset()

    actual fun getAlgorithm(): String = algorithm

    actual open fun getDigestLength(): Int = 0

    actual val digestLength: Int get() = getDigestLength()

    actual companion object {
        actual fun getInstance(algorithm: String): MessageDigest {
            return when (algorithm.uppercase()) {
                "SHA-512", "SHA512" -> Sha512MessageDigest()
                "SHA-384", "SHA384" -> Sha384MessageDigest()
                "SHA-256", "SHA256" -> Sha256MessageDigest()
                "SHA-1", "SHA1" -> Sha1MessageDigest()
                "MD5" -> Md5MessageDigest()
                else -> throw NoSuchAlgorithmException("Unsupported algorithm: $algorithm")
            }
        }

        actual fun isEqual(digesta: ByteArray?, digestb: ByteArray?): Boolean {
            if (digesta == null || digestb == null) return digesta === digestb
            if (digesta.size != digestb.size) return false
            var result = 0
            for (i in digesta.indices) {
                result = result or (digesta[i].toInt() xor digestb[i].toInt())
            }
            return result == 0
        }
    }
}


internal class Sha256MessageDigest : MessageDigest("SHA-256") {
    private var state = intArrayOf(
        0x6a09e667, 0xbb67ae85.toInt(), 0x3c6ef372, 0xa54ff53a.toInt(),
        0x510e527f, 0x9b05688c.toInt(), 0x1f83d9ab, 0x5be0cd19
    )
    private var count = 0L
    private val buffer = ByteArray(64)
    private var bufferPos = 0

    override fun getDigestLength(): Int = 32

    override fun update(input: ByteArray, offset: Int, len: Int) {
        var i = offset
        val end = offset + len
        while (i < end) {
            buffer[bufferPos++] = input[i++]
            count += 8L
            if (bufferPos == 64) {
                processBlock(buffer, 0)
                bufferPos = 0
            }
        }
    }

    override fun digest(): ByteArray {
        val totalBits = count
        buffer[bufferPos++] = 0x80.toByte()
        if (bufferPos > 56) {
            while (bufferPos < 64) {
                buffer[bufferPos++] = 0
            }
            processBlock(buffer, 0)
            bufferPos = 0
        }
        while (bufferPos < 56) {
            buffer[bufferPos++] = 0
        }
        for (i in 7 downTo 0) {
            buffer[56 + (7 - i)] = ((totalBits ushr (i * 8)).toInt() and 0xFF).toByte()
        }
        processBlock(buffer, 0)

        val out = ByteArray(32)
        for (i in 0 until 8) {
            out[i * 4] = (state[i] ushr 24).toByte()
            out[i * 4 + 1] = (state[i] ushr 16).toByte()
            out[i * 4 + 2] = (state[i] ushr 8).toByte()
            out[i * 4 + 3] = state[i].toByte()
        }
        reset()
        return out
    }

    override fun reset() {
        state = intArrayOf(
            0x6a09e667, 0xbb67ae85.toInt(), 0x3c6ef372, 0xa54ff53a.toInt(),
            0x510e527f, 0x9b05688c.toInt(), 0x1f83d9ab, 0x5be0cd19
        )
        count = 0L
        bufferPos = 0
    }

    private fun processBlock(block: ByteArray, offset: Int) {
        val w = IntArray(64)
        for (i in 0 until 16) {
            w[i] = ((block[offset + i * 4].toInt() and 0xFF) shl 24) or
                    ((block[offset + i * 4 + 1].toInt() and 0xFF) shl 16) or
                    ((block[offset + i * 4 + 2].toInt() and 0xFF) shl 8) or
                    (block[offset + i * 4 + 3].toInt() and 0xFF)
        }
        for (i in 16 until 64) {
            val s0 = w[i - 15].rotateRight(7) xor w[i - 15].rotateRight(18) xor (w[i - 15] ushr 3)
            val s1 = w[i - 2].rotateRight(17) xor w[i - 2].rotateRight(19) xor (w[i - 2] ushr 10)
            w[i] = w[i - 16] + s0 + w[i - 7] + s1
        }

        var a = state[0]
        var b = state[1]
        var c = state[2]
        var d = state[3]
        var e = state[4]
        var f = state[5]
        var g = state[6]
        var h = state[7]

        val k = K_SHA256

        for (i in 0 until 64) {
            val S1 = e.rotateRight(6) xor e.rotateRight(11) xor e.rotateRight(25)
            val ch = (e and f) xor (e.inv() and g)
            val temp1 = h + S1 + ch + k[i] + w[i]
            val S0 = a.rotateRight(2) xor a.rotateRight(13) xor a.rotateRight(22)
            val maj = (a and b) xor (a and c) xor (b and c)
            val temp2 = S0 + maj

            h = g
            g = f
            f = e
            e = d + temp1
            d = c
            c = b
            b = a
            a = temp1 + temp2
        }

        state[0] += a
        state[1] += b
        state[2] += c
        state[3] += d
        state[4] += e
        state[5] += f
        state[6] += g
        state[7] += h
    }
}

private val K_SHA256 = intArrayOf(
    0x428a2f98, 0x71374491, 0xb5c0fbcf.toInt(), 0xe9b5dba5.toInt(), 0x3956c25b, 0x59f111f1, 0x923f82a4.toInt(), 0xab1c5ed5.toInt(),
    0xd807aa98.toInt(), 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe.toInt(), 0x9bdc06a7.toInt(), 0xc19bf174.toInt(),
    0xe49b69c1.toInt(), 0xefbe4786.toInt(), 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
    0x983e5152.toInt(), 0xa831c66d.toInt(), 0xb00327c8.toInt(), 0xbf597fc7.toInt(), 0xc6e00bf3.toInt(), 0xd5a79147.toInt(), 0x06ca6351, 0x14292967,
    0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e.toInt(), 0x92722c85.toInt(),
    0xa2bfe8a1.toInt(), 0xa81a664b.toInt(), 0xc24b8b70.toInt(), 0xc76c51a3.toInt(), 0xd192e819.toInt(), 0xd6990624.toInt(), 0xf40e3585.toInt(), 0x106aa070,
    0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
    0x748f82ee, 0x78a5636f, 0x84c87814.toInt(), 0x8cc70208.toInt(), 0x90befffa.toInt(), 0xa4506ceb.toInt(), 0xbef9a3f7.toInt(), 0xc67178f2.toInt()
)

internal class Sha1MessageDigest : MessageDigest("SHA-1") {
    private var state = intArrayOf(
        0x67452301, 0xefcdab89.toInt(), 0x98badcfe.toInt(), 0x10325476, 0xc3d2e1f0.toInt()
    )
    private var count = 0L
    private val buffer = ByteArray(64)
    private var bufferPos = 0

    override fun getDigestLength(): Int = 20

    override fun update(input: ByteArray, offset: Int, len: Int) {
        var i = offset
        val end = offset + len
        while (i < end) {
            buffer[bufferPos++] = input[i++]
            count += 8L
            if (bufferPos == 64) {
                processBlock(buffer, 0)
                bufferPos = 0
            }
        }
    }

    override fun digest(): ByteArray {
        val totalBits = count
        buffer[bufferPos++] = 0x80.toByte()
        if (bufferPos > 56) {
            while (bufferPos < 64) {
                buffer[bufferPos++] = 0
            }
            processBlock(buffer, 0)
            bufferPos = 0
        }
        while (bufferPos < 56) {
            buffer[bufferPos++] = 0
        }
        for (i in 7 downTo 0) {
            buffer[56 + (7 - i)] = ((totalBits ushr (i * 8)).toInt() and 0xFF).toByte()
        }
        processBlock(buffer, 0)

        val out = ByteArray(20)
        for (i in 0 until 5) {
            out[i * 4] = (state[i] ushr 24).toByte()
            out[i * 4 + 1] = (state[i] ushr 16).toByte()
            out[i * 4 + 2] = (state[i] ushr 8).toByte()
            out[i * 4 + 3] = state[i].toByte()
        }
        reset()
        return out
    }

    override fun reset() {
        state = intArrayOf(
            0x67452301, 0xefcdab89.toInt(), 0x98badcfe.toInt(), 0x10325476, 0xc3d2e1f0.toInt()
        )
        count = 0L
        bufferPos = 0
    }

    private fun processBlock(block: ByteArray, offset: Int) {
        val w = IntArray(80)
        for (i in 0 until 16) {
            w[i] = ((block[offset + i * 4].toInt() and 0xFF) shl 24) or
                    ((block[offset + i * 4 + 1].toInt() and 0xFF) shl 16) or
                    ((block[offset + i * 4 + 2].toInt() and 0xFF) shl 8) or
                    (block[offset + i * 4 + 3].toInt() and 0xFF)
        }
        for (i in 16 until 80) {
            w[i] = (w[i - 3] xor w[i - 8] xor w[i - 14] xor w[i - 16]).rotateLeft(1)
        }

        var a = state[0]
        var b = state[1]
        var c = state[2]
        var d = state[3]
        var e = state[4]

        for (i in 0 until 80) {
            val (f, k) = when (i) {
                in 0..19 -> Pair((b and c) or (b.inv() and d), 0x5A827999)
                in 20..39 -> Pair(b xor c xor d, 0x6ED9EBA1)
                in 40..59 -> Pair((b and c) or (b and d) or (c and d), 0x8F1BBCDC.toInt())
                else -> Pair(b xor c xor d, 0xCA62C1D6.toInt())
            }
            val temp = a.rotateLeft(5) + f + e + k + w[i]
            e = d
            d = c
            c = b.rotateLeft(30)
            b = a
            a = temp
        }

        state[0] += a
        state[1] += b
        state[2] += c
        state[3] += d
        state[4] += e
    }
}

internal class Md5MessageDigest : MessageDigest("MD5") {
    private var state = intArrayOf(
        0x67452301, 0xefcdab89.toInt(), 0x98badcfe.toInt(), 0x10325476
    )
    private var count = 0L
    private val buffer = ByteArray(64)
    private var bufferPos = 0

    override fun getDigestLength(): Int = 16

    override fun update(input: ByteArray, offset: Int, len: Int) {
        var i = offset
        val end = offset + len
        while (i < end) {
            buffer[bufferPos++] = input[i++]
            count += 8L
            if (bufferPos == 64) {
                processBlock(buffer, 0)
                bufferPos = 0
            }
        }
    }

    override fun digest(): ByteArray {
        val totalBits = count
        buffer[bufferPos++] = 0x80.toByte()
        if (bufferPos > 56) {
            while (bufferPos < 64) {
                buffer[bufferPos++] = 0
            }
            processBlock(buffer, 0)
            bufferPos = 0
        }
        while (bufferPos < 56) {
            buffer[bufferPos++] = 0
        }
        for (i in 0 until 8) {
            buffer[56 + i] = ((totalBits ushr (i * 8)).toInt() and 0xFF).toByte()
        }
        processBlock(buffer, 0)

        val out = ByteArray(16)
        for (i in 0 until 4) {
            out[i * 4] = state[i].toByte()
            out[i * 4 + 1] = (state[i] ushr 8).toByte()
            out[i * 4 + 2] = (state[i] ushr 16).toByte()
            out[i * 4 + 3] = (state[i] ushr 24).toByte()
        }
        reset()
        return out
    }

    override fun reset() {
        state = intArrayOf(
            0x67452301, 0xefcdab89.toInt(), 0x98badcfe.toInt(), 0x10325476
        )
        count = 0L
        bufferPos = 0
    }

    private fun processBlock(block: ByteArray, offset: Int) {
        val x = IntArray(16)
        for (i in 0 until 16) {
            x[i] = (block[offset + i * 4].toInt() and 0xFF) or
                    ((block[offset + i * 4 + 1].toInt() and 0xFF) shl 8) or
                    ((block[offset + i * 4 + 2].toInt() and 0xFF) shl 16) or
                    ((block[offset + i * 4 + 3].toInt() and 0xFF) shl 24)
        }

        var a = state[0]
        var b = state[1]
        var c = state[2]
        var d = state[3]

        fun ff(a: Int, b: Int, c: Int, d: Int, x: Int, s: Int, ac: Int): Int =
            b + (a + ((b and c) or (b.inv() and d)) + x + ac).rotateLeft(s)

        fun gg(a: Int, b: Int, c: Int, d: Int, x: Int, s: Int, ac: Int): Int =
            b + (a + ((b and d) or (c and d.inv())) + x + ac).rotateLeft(s)

        fun hh(a: Int, b: Int, c: Int, d: Int, x: Int, s: Int, ac: Int): Int =
            b + (a + (b xor c xor d) + x + ac).rotateLeft(s)

        fun ii(a: Int, b: Int, c: Int, d: Int, x: Int, s: Int, ac: Int): Int =
            b + (a + (c xor (b or d.inv())) + x + ac).rotateLeft(s)

        // Round 1
        a = ff(a, b, c, d, x[0], 7, 0xd76aa478.toInt())
        d = ff(d, a, b, c, x[1], 12, 0xe8c7b756.toInt())
        c = ff(c, d, a, b, x[2], 17, 0x242070db)
        b = ff(b, c, d, a, x[3], 22, 0xc1bdceee.toInt())
        a = ff(a, b, c, d, x[4], 7, 0xf57c0faf.toInt())
        d = ff(d, a, b, c, x[5], 12, 0x4787c62a)
        c = ff(c, d, a, b, x[6], 17, 0xa8304613.toInt())
        b = ff(b, c, d, a, x[7], 22, 0xfd469501.toInt())
        a = ff(a, b, c, d, x[8], 7, 0x698098d8)
        d = ff(d, a, b, c, x[9], 12, 0x8b44f7af.toInt())
        c = ff(c, d, a, b, x[10], 17, 0xffff5bb1.toInt())
        b = ff(b, c, d, a, x[11], 22, 0x895cd7be.toInt())
        a = ff(a, b, c, d, x[12], 7, 0x6b901122)
        d = ff(d, a, b, c, x[13], 12, 0xfd987193.toInt())
        c = ff(c, d, a, b, x[14], 17, 0xa679438e.toInt())
        b = ff(b, c, d, a, x[15], 22, 0x49b40821)

        // Round 2
        a = gg(a, b, c, d, x[1], 5, 0xf61e2562.toInt())
        d = gg(d, a, b, c, x[6], 9, 0xc040b340.toInt())
        c = gg(c, d, a, b, x[11], 14, 0x265e5a51)
        b = gg(b, c, d, a, x[0], 20, 0xe9b6c7aa.toInt())
        a = gg(a, b, c, d, x[5], 5, 0xd62f105d.toInt())
        d = gg(d, a, b, c, x[10], 9, 0x02441453)
        c = gg(c, d, a, b, x[15], 14, 0xd8a1e681.toInt())
        b = gg(b, c, d, a, x[4], 20, 0xe7d3fbc8.toInt())
        a = gg(a, b, c, d, x[9], 5, 0x21e1cde6)
        d = gg(d, a, b, c, x[14], 9, 0xc33707d6.toInt())
        c = gg(c, d, a, b, x[3], 14, 0xf4d50d87.toInt())
        b = gg(b, c, d, a, x[8], 20, 0x455a14ed)
        a = gg(a, b, c, d, x[13], 5, 0xa9e3e905.toInt())
        d = gg(d, a, b, c, x[2], 9, 0xfcefa3f8.toInt())
        c = gg(c, d, a, b, x[7], 14, 0x676f02d9)
        b = gg(b, c, d, a, x[12], 20, 0x8d2a4c8a.toInt())

        // Round 3
        a = hh(a, b, c, d, x[5], 4, 0xfffa3942.toInt())
        d = hh(d, a, b, c, x[8], 11, 0x8771f681.toInt())
        c = hh(c, d, a, b, x[11], 16, 0x6d9d6122)
        b = hh(b, c, d, a, x[14], 23, 0xfde5380c.toInt())
        a = hh(a, b, c, d, x[1], 4, 0xa4beea44.toInt())
        d = hh(d, a, b, c, x[4], 11, 0x4bdecfa9)
        c = hh(c, d, a, b, x[7], 16, 0xf6bb4b60.toInt())
        b = hh(b, c, d, a, x[10], 23, 0xbebfbc70.toInt())
        a = hh(a, b, c, d, x[13], 4, 0x289b7ec6)
        d = hh(d, a, b, c, x[0], 11, 0xeaa127fa.toInt())
        c = hh(c, d, a, b, x[3], 16, 0xd4ef3085.toInt())
        b = hh(b, c, d, a, x[6], 23, 0x04881d05)
        a = hh(a, b, c, d, x[9], 4, 0xd9d4d039.toInt())
        d = hh(d, a, b, c, x[12], 11, 0xe6db99e5.toInt())
        c = hh(c, d, a, b, x[15], 16, 0x1fa27cf8)
        b = hh(b, c, d, a, x[2], 23, 0xc4ac5665.toInt())

        // Round 4
        a = ii(a, b, c, d, x[0], 6, 0xf4292244.toInt())
        d = ii(d, a, b, c, x[7], 10, 0x432aff97)
        c = ii(c, d, a, b, x[14], 15, 0xab9423a7.toInt())
        b = ii(b, c, d, a, x[5], 21, 0xfc93a039.toInt())
        a = ii(a, b, c, d, x[12], 6, 0x655b59c3)
        d = ii(d, a, b, c, x[3], 10, 0x8f0ccc92.toInt())
        c = ii(c, d, a, b, x[10], 15, 0xffeff47d.toInt())
        b = ii(b, c, d, a, x[1], 21, 0x85845dd1.toInt())
        a = ii(a, b, c, d, x[8], 6, 0x6fa87e4f)
        d = ii(d, a, b, c, x[15], 10, 0xfe2ce6e0.toInt())
        c = ii(c, d, a, b, x[6], 15, 0xa3014314.toInt())
        b = ii(b, c, d, a, x[13], 21, 0x4e0811a1)
        a = ii(a, b, c, d, x[4], 6, 0xf7537e82.toInt())
        d = ii(d, a, b, c, x[11], 10, 0xbd3af235.toInt())
        c = ii(c, d, a, b, x[2], 15, 0x2ad7d2bb)
        b = ii(b, c, d, a, x[9], 21, 0xeb86d391.toInt())

        state[0] += a
        state[1] += b
        state[2] += c
        state[3] += d
    }
}

internal abstract class Sha512BaseMessageDigest(
    algorithm: String,
    private val initialState: LongArray,
    private val outputWords: Int
) : MessageDigest(algorithm) {
    private var state = initialState.copyOf()
    private var countLow = 0L
    private var countHigh = 0L
    private val buffer = ByteArray(128)
    private var bufferPos = 0

    override fun getDigestLength(): Int = outputWords * 8

    override fun update(input: ByteArray, offset: Int, len: Int) {
        var i = offset
        val end = offset + len
        while (i < end) {
            buffer[bufferPos++] = input[i++]
            val prev = countLow.toULong()
            val next = prev + 8uL
            countLow = next.toLong()
            if (next < prev) {
                countHigh++
            }
            if (bufferPos == 128) {
                processBlock(buffer, 0)
                bufferPos = 0
            }
        }
    }

    override fun digest(): ByteArray {
        val lowBits = countLow
        val highBits = countHigh
        buffer[bufferPos++] = 0x80.toByte()
        if (bufferPos > 112) {
            while (bufferPos < 128) {
                buffer[bufferPos++] = 0
            }
            processBlock(buffer, 0)
            bufferPos = 0
        }
        while (bufferPos < 112) {
            buffer[bufferPos++] = 0
        }
        for (i in 7 downTo 0) {
            buffer[112 + (7 - i)] = ((highBits ushr (i * 8)).toInt() and 0xFF).toByte()
            buffer[120 + (7 - i)] = ((lowBits ushr (i * 8)).toInt() and 0xFF).toByte()
        }
        processBlock(buffer, 0)

        val out = ByteArray(outputWords * 8)
        for (i in 0 until outputWords) {
            val v = state[i]
            for (b in 0 until 8) {
                out[i * 8 + b] = ((v ushr ((7 - b) * 8)).toInt() and 0xFF).toByte()
            }
        }
        reset()
        return out
    }

    override fun reset() {
        state = initialState.copyOf()
        countLow = 0L
        countHigh = 0L
        bufferPos = 0
    }

    private fun processBlock(block: ByteArray, offset: Int) {
        val w = LongArray(80)
        for (i in 0 until 16) {
            var v = 0L
            for (b in 0 until 8) {
                v = (v shl 8) or (block[offset + i * 8 + b].toLong() and 0xFFL)
            }
            w[i] = v
        }
        for (i in 16 until 80) {
            val w15 = w[i - 15]
            val s0 = w15.rotateRight(1) xor w15.rotateRight(8) xor (w15 ushr 7)
            val w2 = w[i - 2]
            val s1 = w2.rotateRight(19) xor w2.rotateRight(61) xor (w2 ushr 6)
            w[i] = w[i - 16] + s0 + w[i - 7] + s1
        }

        var a = state[0]
        var b = state[1]
        var c = state[2]
        var d = state[3]
        var e = state[4]
        var f = state[5]
        var g = state[6]
        var h = state[7]

        val k = K_SHA512
        for (i in 0 until 80) {
            val s1 = e.rotateRight(14) xor e.rotateRight(18) xor e.rotateRight(41)
            val ch = (e and f) xor (e.inv() and g)
            val temp1 = h + s1 + ch + k[i] + w[i]
            val s0 = a.rotateRight(28) xor a.rotateRight(34) xor a.rotateRight(39)
            val maj = (a and b) xor (a and c) xor (b and c)
            val temp2 = s0 + maj

            h = g
            g = f
            f = e
            e = d + temp1
            d = c
            c = b
            b = a
            a = temp1 + temp2
        }

        state[0] += a
        state[1] += b
        state[2] += c
        state[3] += d
        state[4] += e
        state[5] += f
        state[6] += g
        state[7] += h
    }
}

internal class Sha512MessageDigest : Sha512BaseMessageDigest(
    "SHA-512",
    longArrayOf(
        0x6a09e667f3bcc908UL.toLong(), 0xbb67ae8584caa73bUL.toLong(),
        0x3c6ef372fe94f82bUL.toLong(), 0xa54ff53a5f1d36f1UL.toLong(),
        0x510e527fade682d1UL.toLong(), 0x9b05688c2b3e6c1fUL.toLong(),
        0x1f83d9abfb41bd6bUL.toLong(), 0x5be0cd19137e2179UL.toLong()
    ),
    8
)

internal class Sha384MessageDigest : Sha512BaseMessageDigest(
    "SHA-384",
    longArrayOf(
        0xcbbb9d5dc1059ed8UL.toLong(), 0x629a292a367cd507UL.toLong(),
        0x9159015a3070dd17UL.toLong(), 0x152fecd8f70e5939UL.toLong(),
        0x67332667ffc00b31UL.toLong(), 0x8eb44a8768581511UL.toLong(),
        0xdb0c2e0d64f98fa7UL.toLong(), 0x47b5481dbefa4fa4UL.toLong()
    ),
    6
)

private val K_SHA512 = longArrayOf(
    0x428a2f98d728ae22UL.toLong(), 0x7137449123ef65cdUL.toLong(), 0xb5c0fbcfec4d3b2fUL.toLong(), 0xe9b5dba58189dbbcUL.toLong(),
    0x3956c25bf348b538UL.toLong(), 0x59f111f1b605d019UL.toLong(), 0x923f82a4af194f9bUL.toLong(), 0xab1c5ed5da6d8118UL.toLong(),
    0xd807aa98a3030242UL.toLong(), 0x12835b0145706fbeUL.toLong(), 0x243185be4ee4b28cUL.toLong(), 0x550c7dc3d5ffb4e2UL.toLong(),
    0x72be5d74f27b896fUL.toLong(), 0x80deb1fe3b1696b1UL.toLong(), 0x9bdc06a725c71235UL.toLong(), 0xc19bf174cf692694UL.toLong(),
    0xe49b69c19ef14ad2UL.toLong(), 0xefbe4786384f25e3UL.toLong(), 0x0fc19dc68b8cd5b5UL.toLong(), 0x240ca1cc77ac9c65UL.toLong(),
    0x2de92c6f592b0275UL.toLong(), 0x4a7484aa6ea6e483UL.toLong(), 0x5cb0a9dcbd41fbd4UL.toLong(), 0x76f988da831153b5UL.toLong(),
    0x983e5152ee66dfabUL.toLong(), 0xa831c66d2db43210UL.toLong(), 0xb00327c898fb213fUL.toLong(), 0xbf597fc7beef0ee4UL.toLong(),
    0xc6e00bf33da88fc2UL.toLong(), 0xd5a79147930aa725UL.toLong(), 0x06ca6351e003826fUL.toLong(), 0x142929670a0e6e70UL.toLong(),
    0x27b70a8546d22ffcUL.toLong(), 0x2e1b21385c26c926UL.toLong(), 0x4d2c6dfc5ac42aedUL.toLong(), 0x53380d139d95b3dfUL.toLong(),
    0x650a73548baf63deUL.toLong(), 0x766a0abb3c77b2a8UL.toLong(), 0x81c2c92e47edaee6UL.toLong(), 0x92722c851482353bUL.toLong(),
    0xa2bfe8a14cf10364UL.toLong(), 0xa81a664bbc423001UL.toLong(), 0xc24b8b70d0f89791UL.toLong(), 0xc76c51a30654be30UL.toLong(),
    0xd192e819d6ef5218UL.toLong(), 0xd69906245565a910UL.toLong(), 0xf40e35855771202aUL.toLong(), 0x106aa07032bbd1b8UL.toLong(),
    0x19a4c116b8d2d0c8UL.toLong(), 0x1e376c085141ab53UL.toLong(), 0x2748774cdf8eeb99UL.toLong(), 0x34b0bcb5e19b48a8UL.toLong(),
    0x391c0cb3c5c95a63UL.toLong(), 0x4ed8aa4ae3418acbUL.toLong(), 0x5b9cca4f7763e373UL.toLong(), 0x682e6ff3d6b2b8a3UL.toLong(),
    0x748f82ee5defb2fcUL.toLong(), 0x78a5636f43172f60UL.toLong(), 0x84c87814a1f0ab72UL.toLong(), 0x8cc702081a6439ecUL.toLong(),
    0x90befffa23631e28UL.toLong(), 0xa4506cebde82bde9UL.toLong(), 0xbef9a3f7b2c67915UL.toLong(), 0xc67178f2e372532bUL.toLong(),
    0xca273eceea26619cUL.toLong(), 0xd186b8c721c0c207UL.toLong(), 0xeada7dd6cde0eb1eUL.toLong(), 0xf57d4f7fee6ed178UL.toLong(),
    0x06f067aa72176fbaUL.toLong(), 0x0a637dc5a2c898a6UL.toLong(), 0x113f9804bef90daeUL.toLong(), 0x1b710b35131c471bUL.toLong(),
    0x28db77f523047d84UL.toLong(), 0x32caab7b40c72493UL.toLong(), 0x3c9ebe0a15c9bebcUL.toLong(), 0x431d67c49c100d4cUL.toLong(),
    0x4cc5d4becb3e42b6UL.toLong(), 0x597f299cfc657e2aUL.toLong(), 0x5fcb6fab3ad6faecUL.toLong(), 0x6c44198c4a475817UL.toLong()
)
