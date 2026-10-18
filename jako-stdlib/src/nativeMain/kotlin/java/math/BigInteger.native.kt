@file:Suppress("OVERRIDE_DEPRECATION")

package java.math

import java.io.Serializable
import java.util.Random

actual class BigInteger : Number, Comparable<BigInteger>, Serializable {
    val signumVal: Int
    val digits: IntArray // Little-endian base 10^9 digits, digits[0] is least significant

    internal constructor(sign: Int, digits: IntArray) {
        var len = digits.size
        while (len > 0 && digits[len - 1] == 0) {
            len--
        }
        if (len == 0) {
            this.signumVal = 0
            this.digits = IntArray(0)
        } else {
            this.signumVal = if (sign == 0) 0 else if (sign > 0) 1 else -1
            if (len == digits.size) {
                this.digits = digits
            } else {
                this.digits = digits.copyOf(len)
            }
        }
    }

    actual constructor(valBytes: ByteArray) {
        if (valBytes.isEmpty()) throw NumberFormatException("Zero length BigInteger")
        if (valBytes[0] < 0) {
            val mag = ByteArray(valBytes.size)
            var carry = 1
            for (i in valBytes.indices.reversed()) {
                val inv = (valBytes[i].toInt().inv() and 0xFF) + carry
                mag[i] = inv.toByte()
                carry = inv ushr 8
            }
            val temp = BigInteger(-1, mag)
            this.signumVal = temp.signumVal
            this.digits = temp.digits
        } else {
            val temp = BigInteger(1, valBytes)
            this.signumVal = temp.signumVal
            this.digits = temp.digits
        }
    }

    actual constructor(valStr: String) : this(valStr, 10)

    actual constructor(valStr: String, radix: Int) {
        require(radix in 2..36) { "Radix out of range: $radix" }
        val s = valStr.trim()
        require(s.isNotEmpty()) { "Zero length BigInteger" }

        var sign = 1
        var start = 0
        if (s[0] == '-') {
            sign = -1
            start = 1
        } else if (s[0] == '+') {
            start = 1
        }
        require(start < s.length) { "Zero length BigInteger" }

        if (radix == 10) {
            // Fast base 10 chunking
            val numDigits = s.length - start
            val numChunks = (numDigits + 8) / 9
            val chunks = IntArray(numChunks)
            var end = s.length
            var idx = 0
            while (end > start) {
                val chunkStart = maxOf(start, end - 9)
                val chunkStr = s.substring(chunkStart, end)
                chunks[idx++] = chunkStr.toInt()
                end = chunkStart
            }
            var len = chunks.size
            while (len > 0 && chunks[len - 1] == 0) len--
            if (len == 0) {
                this.signumVal = 0
                this.digits = IntArray(0)
            } else {
                this.signumVal = sign
                this.digits = if (len == chunks.size) chunks else chunks.copyOf(len)
            }
        } else {
            var result = ZERO
            val rad = valueOf(radix.toLong())
            for (i in start until s.length) {
                val digit = s[i].digitToIntOrNull(radix) ?: throw NumberFormatException("Illegal digit: ${s[i]}")
                result = result.multiply(rad).add(valueOf(digit.toLong()))
            }
            if (result.signumVal == 0) {
                this.signumVal = 0
                this.digits = IntArray(0)
            } else {
                this.signumVal = sign
                this.digits = result.digits
            }
        }
    }

    actual constructor(signum: Int, magnitude: ByteArray) {
        require(signum in -1..1) { "Invalid signum value" }
        if (magnitude.isEmpty() || signum == 0) {
            this.signumVal = 0
            this.digits = IntArray(0)
            return
        }
        var result = ZERO
        val base256 = valueOf(256L)
        for (b in magnitude) {
            val ubyte = b.toInt() and 0xFF
            result = result.multiply(base256).add(valueOf(ubyte.toLong()))
        }
        if (result.signumVal == 0) {
            this.signumVal = 0
            this.digits = IntArray(0)
        } else {
            this.signumVal = signum
            this.digits = result.digits
        }
    }

    actual constructor(bitLength: Int, rnd: Random) {
        require(bitLength >= 0) { "bitLength must be non-negative" }
        if (bitLength == 0) {
            this.signumVal = 0
            this.digits = IntArray(0)
            return
        }
        val numBytes = (bitLength + 7) / 8
        val bytes = ByteArray(numBytes)
        rnd.nextBytes(bytes)
        val excessBits = (numBytes * 8) - bitLength
        if (excessBits > 0 && bytes.isNotEmpty()) {
            bytes[0] = ((bytes[0].toInt() and 0xFF) ushr excessBits).toByte()
        }
        val temp = BigInteger(1, bytes)
        this.signumVal = temp.signumVal
        this.digits = temp.digits
    }

    actual constructor(bitLength: Int, certainty: Int, rnd: Random) {
        if (bitLength < 2) throw ArithmeticException("bitLength < 2")
        val p = generateProbablePrime(bitLength, maxOf(1, certainty), rnd)
        this.signumVal = p.signumVal
        this.digits = p.digits
    }

    actual fun add(valOther: BigInteger): BigInteger {
        if (signumVal == 0) return valOther
        if (valOther.signumVal == 0) return this
        if (signumVal == valOther.signumVal) {
            return BigInteger(signumVal, addDigits(digits, valOther.digits))
        }
        val cmp = compareDigits(digits, valOther.digits)
        if (cmp == 0) return ZERO
        return if (cmp > 0) {
            BigInteger(signumVal, subtractDigits(digits, valOther.digits))
        } else {
            BigInteger(valOther.signumVal, subtractDigits(valOther.digits, digits))
        }
    }

    actual fun subtract(valOther: BigInteger): BigInteger = add(valOther.negate())

    actual fun multiply(valOther: BigInteger): BigInteger {
        if (signumVal == 0 || valOther.signumVal == 0) return ZERO
        val res = multiplyDigits(digits, valOther.digits)
        return BigInteger(signumVal * valOther.signumVal, res)
    }

    actual fun divide(valOther: BigInteger): BigInteger = divideAndRemainder(valOther)[0]

    actual fun remainder(valOther: BigInteger): BigInteger = divideAndRemainder(valOther)[1]

    actual fun divideAndRemainder(valOther: BigInteger): Array<BigInteger> {
        require(valOther.signumVal != 0) { "BigInteger divide by zero" }
        if (signumVal == 0) return arrayOf(ZERO, ZERO)
        val cmp = compareDigits(digits, valOther.digits)
        if (cmp < 0) return arrayOf(ZERO, this)
        if (cmp == 0) {
            return arrayOf(
                if (signumVal == valOther.signumVal) ONE else ONE.negate(),
                ZERO
            )
        }

        val (qDigits, rDigits) = divideDigits(digits, valOther.digits)
        val q = BigInteger(signumVal * valOther.signumVal, qDigits)
        val r = BigInteger(signumVal, rDigits)
        return arrayOf(q, r)
    }

    actual fun pow(exponent: Int): BigInteger {
        require(exponent >= 0) { "Negative exponent: $exponent" }
        if (exponent == 0) return ONE
        if (signumVal == 0) return ZERO
        var base = this
        var result = ONE
        var exp = exponent
        while (exp > 0) {
            if (exp and 1 == 1) {
                result = result.multiply(base)
            }
            if (exp > 1) {
                base = base.multiply(base)
            }
            exp = exp ushr 1
        }
        return result
    }

    actual fun gcd(valOther: BigInteger): BigInteger {
        if (valOther.signumVal == 0) return this.abs()
        if (this.signumVal == 0) return valOther.abs()
        var a = this.abs()
        var b = valOther.abs()
        while (b.signumVal != 0) {
            val r = a.remainder(b)
            a = b
            b = r
        }
        return a
    }

    actual fun abs(): BigInteger = if (signumVal < 0) negate() else this

    actual fun negate(): BigInteger = if (signumVal == 0) ZERO else BigInteger(-signumVal, digits)

    actual fun signum(): Int = signumVal

    actual fun mod(m: BigInteger): BigInteger {
        if (m.signumVal <= 0) throw ArithmeticException("BigInteger: modulus not positive")
        val result = remainder(m)
        return if (result.signumVal < 0) result.add(m) else result
    }

    actual fun modPow(exponent: BigInteger, m: BigInteger): BigInteger {
        if (m.signumVal <= 0) throw ArithmeticException("BigInteger: modulus not positive")
        if (exponent.signumVal == 0) return if (m == ONE) ZERO else ONE
        if (this == ZERO) return if (exponent.signumVal < 0) throw ArithmeticException("BigInteger not invertible.") else ZERO
        if (exponent.signumVal < 0) {
            return modInverse(m).modPow(exponent.negate(), m)
        }
        if (m == ONE) return ZERO
        var base = this.mod(m)
        var result = ONE
        val expBytes = exponent.toByteArray()
        var started = false
        for (b in expBytes) {
            val ub = b.toInt() and 0xFF
            for (bit in 7 downTo 0) {
                val isSet = ((ub ushr bit) and 1) != 0
                if (!started) {
                    if (isSet) {
                        started = true
                        result = base
                    }
                } else {
                    result = result.multiply(result).remainder(m)
                    if (isSet) {
                        result = result.multiply(base).remainder(m)
                    }
                }
            }
        }
        return result
    }

    actual fun modInverse(m: BigInteger): BigInteger {
        if (m.signumVal <= 0) throw ArithmeticException("BigInteger: modulus not positive")
        if (m == ONE) return ZERO
        var t = ZERO
        var newT = ONE
        var r = m
        var newR = this.mod(m)
        while (newR.signumVal != 0) {
            val qr = r.divideAndRemainder(newR)
            val q = qr[0]
            val rem = qr[1]
            val nextT = t.subtract(q.multiply(newT))
            t = newT
            newT = nextT
            r = newR
            newR = rem
        }
        if (r > ONE) throw ArithmeticException("BigInteger not invertible.")
        if (t.signumVal < 0) t = t.add(m)
        return t
    }

    actual fun shiftLeft(n: Int): BigInteger {
        if (signumVal == 0 || n == 0) return this
        if (n < 0) return shiftRight(-n)
        return multiply(valueOf(2L).pow(n))
    }

    actual fun shiftRight(n: Int): BigInteger {
        if (signumVal == 0 || n == 0) return this
        if (n < 0) return shiftLeft(-n)
        val divisor = valueOf(2L).pow(n)
        val (q, r) = divideAndRemainder(divisor)
        return if (signumVal < 0 && r.signumVal != 0) q.subtract(ONE) else q
    }

    actual fun and(valOther: BigInteger): BigInteger = bitwiseOp(valOther) { a, b -> a and b }

    actual fun or(valOther: BigInteger): BigInteger = bitwiseOp(valOther) { a, b -> a or b }

    actual fun xor(valOther: BigInteger): BigInteger = bitwiseOp(valOther) { a, b -> a xor b }

    actual fun not(): BigInteger = subtract(ONE).negate()

    private inline fun bitwiseOp(other: BigInteger, op: (Int, Int) -> Int): BigInteger {
        val bytes1 = this.toByteArray()
        val bytes2 = other.toByteArray()
        val maxLen = maxOf(bytes1.size, bytes2.size)
        val res = ByteArray(maxLen)
        for (i in 0 until maxLen) {
            val b1 = if (i < bytes1.size) bytes1[bytes1.size - 1 - i].toInt() else (if (this.signumVal < 0) -1 else 0)
            val b2 = if (i < bytes2.size) bytes2[bytes2.size - 1 - i].toInt() else (if (other.signumVal < 0) -1 else 0)
            res[maxLen - 1 - i] = op(b1, b2).toByte()
        }
        return BigInteger(1, res)
    }

    actual fun testBit(n: Int): Boolean {
        require(n >= 0) { "Negative bit address: $n" }
        val shifted = shiftRight(n)
        return (shifted.remainder(valueOf(2L)).signumVal != 0)
    }

    actual fun setBit(n: Int): BigInteger = or(ONE.shiftLeft(n))

    actual fun clearBit(n: Int): BigInteger = and(ONE.shiftLeft(n).not())

    actual fun flipBit(n: Int): BigInteger = xor(ONE.shiftLeft(n))

    actual fun bitLength(): Int {
        if (signumVal == 0) return 0
        val bytes = toByteArray()
        var len = bytes.size * 8
        var firstByte = bytes[0].toInt() and 0xFF
        if (firstByte == 0 && bytes.size > 1) {
            len -= 8
            firstByte = bytes[1].toInt() and 0xFF
        }
        var leadingZeros = 0
        for (i in 7 downTo 0) {
            if ((firstByte and (1 shl i)) == 0) leadingZeros++ else break
        }
        return maxOf(0, len - leadingZeros)
    }

    actual fun bitCount(): Int {
        val bytes = toByteArray()
        var count = 0
        for (b in bytes) {
            var v = b.toInt() and 0xFF
            while (v != 0) {
                count += (v and 1)
                v = v ushr 1
            }
        }
        return count
    }

    actual fun isProbablePrime(certainty: Int): Boolean {
        if (certainty <= 0) return true
        val w = this.abs()
        if (w == TWO) return true
        if (w.signumVal == 0 || w == ONE || (w.digits[0] and 1) == 0) return false
        for (p in SMALL_PRIMES) {
            val bp = valueOf(p.toLong())
            if (w == bp) return true
            if (w.remainder(bp).signumVal == 0) return false
        }
        val wMinusOne = w.subtract(ONE)
        var d = wMinusOne
        var s = 0
        while (d.signumVal > 0 && (d.digits[0] and 1) == 0) {
            d = d.shiftRight(1)
            s++
        }
        val rounds = minOf(6, maxOf(2, (certainty + 1) / 2))
        val bases = intArrayOf(2, 3, 5, 7, 11, 13, 17, 19)
        for (i in 0 until rounds) {
            val a = valueOf(bases[i].toLong())
            if (a >= wMinusOne) break
            var x = a.modPow(d, w)
            if (x == ONE || x == wMinusOne) continue
            var composite = true
            for (r in 1 until s) {
                x = x.multiply(x).remainder(w)
                if (x == ONE) return false
                if (x == wMinusOne) {
                    composite = false
                    break
                }
            }
            if (composite) return false
        }
        return true
    }

    actual fun nextProbablePrime(): BigInteger {
        if (this.signumVal < 0) throw ArithmeticException("start < 0: $this")
        if (this < TWO) return TWO
        var candidate = this.add(ONE)
        if ((candidate.digits[0] and 1) == 0) {
            candidate = candidate.add(ONE)
        }
        while (!candidate.isProbablePrime(20)) {
            candidate = candidate.add(TWO)
        }
        return candidate
    }

    actual fun toByteArray(): ByteArray {
        if (signumVal == 0) return byteArrayOf(0)
        var temp = this.abs()
        val byteList = mutableListOf<Byte>()
        val base256 = valueOf(256L)
        while (temp.signumVal > 0) {
            val (q, r) = temp.divideAndRemainder(base256)
            temp = q
            byteList.add(r.toInt().toByte())
        }
        val highByte = byteList.last().toInt() and 0xFF
        if (signumVal > 0 && (highByte and 0x80) != 0) {
            byteList.add(0)
        }
        val bytes = ByteArray(byteList.size)
        for (i in byteList.indices) {
            bytes[bytes.size - 1 - i] = byteList[i]
        }
        if (signumVal < 0) {
            // Two's complement negation
            var carry = 1
            for (i in bytes.indices.reversed()) {
                val inv = (bytes[i].toInt().inv() and 0xFF) + carry
                bytes[i] = inv.toByte()
                carry = inv ushr 8
            }
        }
        return bytes
    }

    actual fun toString(radix: Int): String {
        require(radix in 2..36) { "Radix out of range: $radix" }
        if (signumVal == 0) return "0"
        if (radix == 10) {
            val sb = StringBuilder()
            if (signumVal < 0) sb.append('-')
            sb.append(digits[digits.size - 1])
            for (i in digits.size - 2 downTo 0) {
                sb.append(digits[i].toString().padStart(9, '0'))
            }
            return sb.toString()
        }

        var temp = this.abs()
        val rad = valueOf(radix.toLong())
        val sb = StringBuilder()
        while (temp.signumVal > 0) {
            val (q, r) = temp.divideAndRemainder(rad)
            temp = q
            val rem = r.toInt()
            val c = if (rem < 10) ('0' + rem) else ('a' + (rem - 10))
            sb.append(c)
        }
        if (signumVal < 0) sb.append('-')
        return sb.reverse().toString()
    }

    actual override fun toString(): String = toString(10)

    actual override fun toByte(): Byte = toLong().toByte()
    actual override fun toShort(): Short = toLong().toShort()
    actual override fun toInt(): Int = toLong().toInt()
    actual override fun toLong(): Long {
        if (digits.isEmpty()) return 0L
        var res = 0L
        var mult = 1L
        for (i in 0 until minOf(3, digits.size)) {
            res += digits[i].toLong() * mult
            mult *= BASE
        }
        return res * signumVal
    }
    actual override fun toFloat(): Float = toDouble().toFloat()
    actual override fun toDouble(): Double = toString().toDouble()
    actual override fun toChar(): Char = toInt().toChar()

    actual override fun compareTo(other: BigInteger): Int {
        if (signumVal != other.signumVal) {
            return if (signumVal > other.signumVal) 1 else -1
        }
        if (signumVal == 0) return 0
        val cmp = compareDigits(digits, other.digits)
        return if (signumVal > 0) cmp else -cmp
    }

    actual override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is BigInteger) return false
        if (signumVal != other.signumVal) return false
        if (digits.size != other.digits.size) return false
        for (i in digits.indices) {
            if (digits[i] != other.digits[i]) return false
        }
        return true
    }

    actual override fun hashCode(): Int {
        var hash = signumVal
        for (d in digits) {
            hash = 31 * hash + d
        }
        return hash
    }

    actual companion object {
        const val BASE = 1_000_000_000L

        actual val ZERO: BigInteger = BigInteger(0, IntArray(0))
        actual val ONE: BigInteger = BigInteger(1, intArrayOf(1))
        private val TWO: BigInteger = BigInteger(1, intArrayOf(2))
        actual val TEN: BigInteger = BigInteger(1, intArrayOf(10))

        private val SMALL_PRIMES = intArrayOf(
            3, 5, 7, 11, 13, 17, 19, 23, 29, 31, 37, 41, 43, 47, 53, 59, 61, 67, 71, 73,
            79, 83, 89, 97, 101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157,
            163, 167, 173, 179, 181, 191, 193, 197, 199, 211, 223, 227, 229, 233, 239, 241, 251
        )

        actual fun probablePrime(bitLength: Int, rnd: Random): BigInteger {
            if (bitLength < 2) throw ArithmeticException("bitLength < 2")
            return generateProbablePrime(bitLength, 20, rnd)
        }

        private fun generateProbablePrime(bitLength: Int, certainty: Int, rnd: Random): BigInteger {
            val numBytes = (bitLength + 7) / 8
            val excessBits = (numBytes * 8) - bitLength
            val topBitMask = 1 shl (7 - excessBits)
            val topMask = 0xFF ushr excessBits
            val bytes = ByteArray(numBytes)
            while (true) {
                rnd.nextBytes(bytes)
                bytes[0] = ((bytes[0].toInt() and topMask) or topBitMask).toByte()
                bytes[numBytes - 1] = (bytes[numBytes - 1].toInt() or 1).toByte()
                val candidate = BigInteger(1, bytes)
                if (candidate.isProbablePrime(certainty)) {
                    return candidate
                }
            }
        }

        actual fun valueOf(valLong: Long): BigInteger {
            if (valLong == 0L) return ZERO
            val sign = if (valLong > 0) 1 else -1
            var absVal = if (valLong < 0) -valLong else valLong
            val d0 = (absVal % BASE).toInt()
            absVal /= BASE
            if (absVal == 0L) {
                return BigInteger(sign, intArrayOf(d0))
            }
            val d1 = (absVal % BASE).toInt()
            absVal /= BASE
            if (absVal == 0L) {
                return BigInteger(sign, intArrayOf(d0, d1))
            }
            val d2 = (absVal % BASE).toInt()
            return BigInteger(sign, intArrayOf(d0, d1, d2))
        }

        private fun compareDigits(a: IntArray, b: IntArray): Int {
            if (a.size != b.size) {
                return if (a.size > b.size) 1 else -1
            }
            for (i in a.size - 1 downTo 0) {
                if (a[i] != b[i]) {
                    return if (a[i] > b[i]) 1 else -1
                }
            }
            return 0
        }

        private fun addDigits(a: IntArray, b: IntArray): IntArray {
            val maxLen = maxOf(a.size, b.size)
            val res = IntArray(maxLen + 1)
            var carry = 0L
            for (i in 0 until maxLen) {
                val da = if (i < a.size) a[i].toLong() else 0L
                val db = if (i < b.size) b[i].toLong() else 0L
                val sum = da + db + carry
                res[i] = (sum % BASE).toInt()
                carry = sum / BASE
            }
            if (carry > 0) {
                res[maxLen] = carry.toInt()
            }
            return res
        }

        private fun subtractDigits(big: IntArray, small: IntArray): IntArray {
            val res = IntArray(big.size)
            var borrow = 0L
            for (i in big.indices) {
                val da = big[i].toLong()
                val db = if (i < small.size) small[i].toLong() else 0L
                val diff = da - db - borrow
                if (diff < 0) {
                    res[i] = (diff + BASE).toInt()
                    borrow = 1L
                } else {
                    res[i] = diff.toInt()
                    borrow = 0L
                }
            }
            return res
        }

        private fun multiplyDigits(a: IntArray, b: IntArray): IntArray {
            val res = LongArray(a.size + b.size)
            for (i in a.indices) {
                val da = a[i].toLong()
                var carry = 0L
                for (j in b.indices) {
                    val db = b[j].toLong()
                    val cur = res[i + j] + (da * db) + carry
                    res[i + j] = cur % BASE
                    carry = cur / BASE
                }
                var k = i + b.size
                while (carry > 0L && k < res.size) {
                    val cur = res[k] + carry
                    res[k] = cur % BASE
                    carry = cur / BASE
                    k++
                }
            }
            val intRes = IntArray(res.size)
            for (i in res.indices) {
                intRes[i] = res[i].toInt()
            }
            return intRes
        }

        private fun divideDigits(num: IntArray, den: IntArray): Pair<IntArray, IntArray> {
            if (den.size == 1) {
                val d = den[0].toLong()
                val q = IntArray(num.size)
                var rem = 0L
                for (i in num.size - 1 downTo 0) {
                    val cur = rem * BASE + num[i].toLong()
                    q[i] = (cur / d).toInt()
                    rem = cur % d
                }
                return Pair(q, if (rem == 0L) IntArray(0) else intArrayOf(rem.toInt()))
            }

            // Knuth Algorithm D for multi-digit division in base 10^9
            val n = den.size
            val m = num.size - n
            val scale = (BASE / (den[n - 1].toLong() + 1)).toInt()

            val u = multiplySingle(num, scale, extra = 1)
            val v = multiplySingle(den, scale, extra = 0)

            val q = IntArray(m + 1)
            val v1 = v[n - 1].toLong()
            val v2 = v[n - 2].toLong()

            for (j in m downTo 0) {
                val u2 = u[j + n].toLong()
                val u1 = u[j + n - 1].toLong()
                val u0 = if (j + n - 2 >= 0) u[j + n - 2].toLong() else 0L

                var qHat = (u2 * BASE + u1) / v1
                var rHat = (u2 * BASE + u1) % v1

                while (qHat >= BASE || qHat * v2 > rHat * BASE + u0) {
                    qHat--
                    rHat += v1
                    if (rHat >= BASE) break
                }

                // Multiply and subtract
                var borrow = 0L
                for (i in 0 until n) {
                    val p = qHat * v[i].toLong() + borrow
                    val sub = u[j + i].toLong() - (p % BASE)
                    if (sub < 0) {
                        u[j + i] = (sub + BASE).toInt()
                        borrow = (p / BASE) + 1
                    } else {
                        u[j + i] = sub.toInt()
                        borrow = p / BASE
                    }
                }
                val subTop = u[j + n].toLong() - borrow
                if (subTop < 0) {
                    u[j + n] = (subTop + BASE).toInt()
                    qHat--
                    // Add back v
                    var carry = 0L
                    for (i in 0 until n) {
                        val sum = u[j + i].toLong() + v[i].toLong() + carry
                        u[j + i] = (sum % BASE).toInt()
                        carry = sum / BASE
                    }
                    u[j + n] = (u[j + n].toLong() + carry).toInt()
                } else {
                    u[j + n] = subTop.toInt()
                }

                q[j] = qHat.toInt()
            }

            // Unscale remainder
            val rem = IntArray(n)
            var remCarry = 0L
            for (i in n - 1 downTo 0) {
                val cur = remCarry * BASE + u[i].toLong()
                rem[i] = (cur / scale).toInt()
                remCarry = cur % scale
            }

            return Pair(q, rem)
        }

        private fun multiplySingle(a: IntArray, m: Int, extra: Int): IntArray {
            val res = IntArray(a.size + extra + 1)
            var carry = 0L
            val mult = m.toLong()
            for (i in a.indices) {
                val cur = a[i].toLong() * mult + carry
                res[i] = (cur % BASE).toInt()
                carry = cur / BASE
            }
            if (carry > 0) {
                res[a.size] = carry.toInt()
            }
            return res
        }
    }
}
