package javax.crypto

import java.io.ByteArrayOutputStream
import java.lang.Cloneable
import java.math.BigInteger
import java.nio.ByteBuffer
import java.security.*
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.*
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec

actual interface SecretKey : Key

actual open class ShortBufferException : GeneralSecurityException {
    actual constructor() : super()
    actual constructor(msg: String?) : super(msg)
}

actual open class NoSuchPaddingException : GeneralSecurityException {
    actual constructor() : super()
    actual constructor(msg: String?) : super(msg)
}

actual open class IllegalBlockSizeException : GeneralSecurityException {
    actual constructor() : super()
    actual constructor(msg: String?) : super(msg)
}

actual open class BadPaddingException : GeneralSecurityException {
    actual constructor() : super()
    actual constructor(msg: String?) : super(msg)
}

actual open class AEADBadTagException : BadPaddingException {
    actual constructor() : super()
    actual constructor(msg: String?) : super(msg)
}

actual open class Mac private constructor(
    private val algName: String,
    private val digestAlg: String,
    private val blockSize: Int,
    private val macLen: Int
) : Cloneable {
    private var initialized = false
    private var ipad = ByteArray(0)
    private var opad = ByteArray(0)
    private var buffer = ByteArrayOutputStream()

    actual fun getAlgorithm(): String = algName
    actual val algorithm: String get() = algName

    actual fun getMacLength(): Int = macLen
    actual val macLength: Int get() = macLen

    actual fun init(key: Key) {
        init(key, null)
    }

    actual fun init(key: Key, params: AlgorithmParameterSpec?) {
        if (params != null) {
            throw InvalidAlgorithmParameterException("HMAC does not use parameters")
        }
        val keyBytes = key.getEncoded() ?: throw InvalidKeyException("Missing key data")
        val k0 = if (keyBytes.size > blockSize) {
            MessageDigest.getInstance(digestAlg).digest(keyBytes)
        } else {
            keyBytes
        }
        ipad = ByteArray(blockSize) { i ->
            val b = if (i < k0.size) k0[i].toInt() and 0xFF else 0
            (b xor 0x36).toByte()
        }
        opad = ByteArray(blockSize) { i ->
            val b = if (i < k0.size) k0[i].toInt() and 0xFF else 0
            (b xor 0x5c).toByte()
        }
        buffer.reset()
        initialized = true
    }

    private fun checkInitialized() {
        if (!initialized) {
            throw IllegalStateException("MAC not initialized")
        }
    }

    actual fun update(input: Byte) {
        checkInitialized()
        buffer.write(input.toInt() and 0xFF)
    }

    actual fun update(input: ByteArray?) {
        checkInitialized()
        if (input != null && input.isNotEmpty()) {
            buffer.write(input, 0, input.size)
        }
    }

    actual fun update(input: ByteArray?, offset: Int, len: Int) {
        checkInitialized()
        if (input == null) {
            throw IllegalArgumentException("Input buffer must not be null")
        }
        if (offset < 0 || len < 0 || offset > input.size - len) {
            throw IllegalArgumentException("Bad arguments")
        }
        if (len > 0) {
            buffer.write(input, offset, len)
        }
    }

    actual fun update(input: ByteBuffer) {
        checkInitialized()
        val rem = input.remaining()
        if (rem > 0) {
            val tmp = ByteArray(rem)
            input.get(tmp)
            buffer.write(tmp, 0, rem)
        }
    }

    actual fun doFinal(): ByteArray {
        checkInitialized()
        val data = buffer.toByteArray()
        buffer.reset()

        val innerMd = MessageDigest.getInstance(digestAlg)
        innerMd.update(ipad)
        innerMd.update(data)
        val innerHash = innerMd.digest()

        val outerMd = MessageDigest.getInstance(digestAlg)
        outerMd.update(opad)
        outerMd.update(innerHash)
        return outerMd.digest()
    }

    actual fun doFinal(output: ByteArray, outOffset: Int) {
        checkInitialized()
        if (outOffset < 0 || output.size - outOffset < macLen) {
            throw ShortBufferException("Cannot store MAC in output buffer")
        }
        val mac = doFinal()
        mac.copyInto(output, destinationOffset = outOffset)
    }

    actual fun doFinal(input: ByteArray?): ByteArray {
        checkInitialized()
        update(input)
        return doFinal()
    }

    actual fun reset() {
        buffer.reset()
    }

    open fun clone(): Any {
        val copy = Mac(algName, digestAlg, blockSize, macLen)
        copy.initialized = this.initialized
        copy.ipad = this.ipad.copyOf()
        copy.opad = this.opad.copyOf()
        val currentBytes = this.buffer.toByteArray()
        copy.buffer.write(currentBytes, 0, currentBytes.size)
        return copy
    }

    actual companion object {
        actual fun getInstance(algorithm: String): Mac {
            val normalized = algorithm.uppercase().replace("-", "")
            return when (normalized) {
                "HMACMD5" -> Mac(algorithm, "MD5", 64, 16)
                "HMACSHA1" -> Mac(algorithm, "SHA-1", 64, 20)
                "HMACSHA256" -> Mac(algorithm, "SHA-256", 64, 32)
                "HMACSHA384" -> Mac(algorithm, "SHA-384", 128, 48)
                "HMACSHA512" -> Mac(algorithm, "SHA-512", 128, 64)
                else -> throw NoSuchAlgorithmException("Algorithm $algorithm not available")
            }
        }
    }
}

actual open class Cipher private constructor(
    private val transformation: String,
    private val cipherFamily: CipherFamily,
    private val mode: Mode,
    private val padding: Padding
) {
    private enum class CipherFamily { AES, RSA }
    private enum class Mode { ECB, CBC, CTR, GCM }
    private enum class Padding { NO_PADDING, PKCS5_PADDING, PKCS1_PADDING }

    private var initialized = false
    private var opmode = ENCRYPT_MODE
    private var roundKeys = IntArray(0)
    private var numRounds = 0
    private var initialIv: ByteArray? = null
    private var gcmTagLenBits: Int = 128
    private var currentVector = ByteArray(16)
    private var aadBuffer = ByteArrayOutputStream()
    private var streamBuffer = ByteArrayOutputStream()
    private var updateCalled = false
    private var rsaModulus: BigInteger = BigInteger.ZERO
    private var rsaExponent: BigInteger = BigInteger.ZERO
    private var rsaIsPublicKey: Boolean = true
    private var cipherRandom: SecureRandom = SecureRandom()

    actual fun getAlgorithm(): String = transformation
    actual val algorithm: String get() = transformation

    actual fun getBlockSize(): Int = if (cipherFamily == CipherFamily.RSA) 0 else 16
    actual val blockSize: Int get() = getBlockSize()

    actual fun getOutputSize(inputLen: Int): Int {
        checkInitialized()
        if (cipherFamily == CipherFamily.RSA) {
            return (rsaModulus.bitLength() + 7) / 8
        }
        val total = streamBuffer.size() + maxOf(0, inputLen)
        return when {
            mode == Mode.GCM -> {
                val tagBytes = gcmTagLenBits / 8
                if (opmode == ENCRYPT_MODE) total + tagBytes else maxOf(0, total - tagBytes)
            }
            mode == Mode.CTR -> total
            padding == Padding.NO_PADDING -> total
            opmode == ENCRYPT_MODE -> ((total / 16) + 1) * 16
            else -> total
        }
    }

    actual fun getIV(): ByteArray? = initialIv?.copyOf()
    actual val iv: ByteArray? get() = getIV()

    actual fun init(opmode: Int, key: Key) {
        init(opmode, key, null as AlgorithmParameterSpec?, null)
    }

    actual fun init(opmode: Int, key: Key, random: SecureRandom?) {
        init(opmode, key, null as AlgorithmParameterSpec?, random)
    }

    actual fun init(opmode: Int, key: Key, params: AlgorithmParameterSpec?) {
        init(opmode, key, params, null)
    }

    actual fun init(opmode: Int, key: Key, params: AlgorithmParameterSpec?, random: SecureRandom?) {
        if (opmode != ENCRYPT_MODE && opmode != DECRYPT_MODE && opmode != WRAP_MODE && opmode != UNWRAP_MODE) {
            throw IllegalArgumentException("Invalid opmode: $opmode")
        }
        this.cipherRandom = random ?: SecureRandom()

        if (cipherFamily == CipherFamily.RSA) {
            if (params != null) {
                throw InvalidAlgorithmParameterException("RSA cipher does not take parameters")
            }
            when (key) {
                is RSAPublicKey -> {
                    rsaModulus = key.getModulus()
                    rsaExponent = key.getPublicExponent()
                    rsaIsPublicKey = true
                }
                is RSAPrivateKey -> {
                    rsaModulus = key.getModulus()
                    rsaExponent = key.getPrivateExponent()
                    rsaIsPublicKey = false
                }
                is PublicKey -> {
                    val pub = KeyFactory.getInstance("RSA").generatePublic(
                        X509EncodedKeySpec(key.getEncoded() ?: throw InvalidKeyException("Null key encoding"))
                    ) as RSAPublicKey
                    rsaModulus = pub.getModulus()
                    rsaExponent = pub.getPublicExponent()
                    rsaIsPublicKey = true
                }
                is PrivateKey -> {
                    val priv = KeyFactory.getInstance("RSA").generatePrivate(
                        PKCS8EncodedKeySpec(key.getEncoded() ?: throw InvalidKeyException("Null key encoding"))
                    ) as RSAPrivateKey
                    rsaModulus = priv.getModulus()
                    rsaExponent = priv.getPrivateExponent()
                    rsaIsPublicKey = false
                }
                else -> throw InvalidKeyException("Unsupported key for RSA Cipher")
            }
            this.opmode = opmode
            this.initialIv = null
            resetState()
            this.initialized = true
            return
        }

        val keyBytes = key.getEncoded() ?: throw InvalidKeyException("Key encoding is null")
        if (keyBytes.size != 16 && keyBytes.size != 24 && keyBytes.size != 32) {
            throw InvalidKeyException("Invalid AES key length: ${keyBytes.size} bytes")
        }

        when (mode) {
            Mode.ECB -> {
                if (params != null) {
                    throw InvalidAlgorithmParameterException("ECB mode cannot use IV")
                }
                initialIv = null
            }
            Mode.CBC, Mode.CTR -> {
                if (params == null) {
                    if (opmode == ENCRYPT_MODE || opmode == WRAP_MODE) {
                        val ivBytes = ByteArray(16)
                        cipherRandom.nextBytes(ivBytes)
                        initialIv = ivBytes
                    } else {
                        throw InvalidAlgorithmParameterException("Parameters missing")
                    }
                } else if (params is IvParameterSpec) {
                    val ivBytes = params.getIV()
                    if (ivBytes.size != 16) {
                        throw InvalidAlgorithmParameterException("Wrong IV length: must be 16 bytes long")
                    }
                    initialIv = ivBytes
                } else {
                    throw InvalidAlgorithmParameterException("Unsupported parameter specification: $params")
                }
            }
            Mode.GCM -> {
                when (params) {
                    null -> {
                        if (opmode == ENCRYPT_MODE || opmode == WRAP_MODE) {
                            val ivBytes = ByteArray(12)
                            cipherRandom.nextBytes(ivBytes)
                            initialIv = ivBytes
                            gcmTagLenBits = 128
                        } else {
                            throw InvalidAlgorithmParameterException("Parameters missing for GCM decryption")
                        }
                    }
                    is GCMParameterSpec -> {
                        val tLen = params.getTLen()
                        if (tLen !in intArrayOf(128, 120, 112, 104, 96)) {
                            throw InvalidAlgorithmParameterException("Unsupported GCM tag length: $tLen bits")
                        }
                        val ivBytes = params.getIV()
                        if (ivBytes.isEmpty()) {
                            throw InvalidAlgorithmParameterException("GCM IV must not be empty")
                        }
                        initialIv = ivBytes
                        gcmTagLenBits = tLen
                    }
                    is IvParameterSpec -> {
                        val ivBytes = params.getIV()
                        if (ivBytes.isEmpty()) {
                            throw InvalidAlgorithmParameterException("GCM IV must not be empty")
                        }
                        initialIv = ivBytes
                        gcmTagLenBits = 128
                    }
                    else -> throw InvalidAlgorithmParameterException("Unsupported parameter specification for GCM: $params")
                }
            }
        }

        this.opmode = opmode
        this.numRounds = when (keyBytes.size) {
            16 -> 10
            24 -> 12
            else -> 14
        }
        this.roundKeys = AesEngine.expandKey(keyBytes, numRounds)
        resetState()
        this.initialized = true
    }

    private fun resetState() {
        aadBuffer.reset()
        streamBuffer.reset()
        updateCalled = false
        val iv = initialIv
        if (iv != null && iv.size == 16) {
            iv.copyInto(currentVector)
        } else {
            currentVector.fill(0)
        }
    }

    private fun checkInitialized() {
        if (!initialized) {
            throw IllegalStateException("Cipher not initialized")
        }
    }

    actual fun updateAAD(src: ByteArray) {
        updateAAD(src, 0, src.size)
    }

    actual fun updateAAD(src: ByteArray, offset: Int, len: Int) {
        checkInitialized()
        if (mode != Mode.GCM) {
            throw IllegalStateException("Cipher is not in AEAD mode")
        }
        if (updateCalled) {
            throw IllegalStateException("Cannot call updateAAD after update")
        }
        if (offset < 0 || len < 0 || offset > src.size - len) {
            throw IllegalArgumentException("Bad arguments")
        }
        if (len > 0) {
            aadBuffer.write(src, offset, len)
        }
    }

    actual fun updateAAD(src: ByteBuffer) {
        checkInitialized()
        if (mode != Mode.GCM) {
            throw IllegalStateException("Cipher is not in AEAD mode")
        }
        if (updateCalled) {
            throw IllegalStateException("Cannot call updateAAD after update")
        }
        val rem = src.remaining()
        if (rem > 0) {
            val tmp = ByteArray(rem)
            src.get(tmp)
            aadBuffer.write(tmp, 0, rem)
        }
    }

    actual fun update(input: ByteArray): ByteArray? {
        return update(input, 0, input.size)
    }

    actual fun update(input: ByteArray, inputOffset: Int, inputLen: Int): ByteArray? {
        checkInitialized()
        if (inputOffset < 0 || inputLen < 0 || inputOffset > input.size - inputLen) {
            throw IllegalArgumentException("Bad arguments")
        }
        updateCalled = true
        if (inputLen == 0) return null
        streamBuffer.write(input, inputOffset, inputLen)
        return ByteArray(0)
    }

    actual fun update(input: ByteArray, inputOffset: Int, inputLen: Int, output: ByteArray): Int {
        return update(input, inputOffset, inputLen, output, 0)
    }

    actual fun update(input: ByteArray, inputOffset: Int, inputLen: Int, output: ByteArray, outputOffset: Int): Int {
        checkInitialized()
        if (inputOffset < 0 || inputLen < 0 || inputOffset > input.size - inputLen || outputOffset < 0) {
            throw IllegalArgumentException("Bad arguments")
        }
        updateCalled = true
        if (inputLen > 0) {
            streamBuffer.write(input, inputOffset, inputLen)
        }
        return 0
    }

    actual fun doFinal(): ByteArray {
        checkInitialized()
        val allInput = streamBuffer.toByteArray()
        val allAad = aadBuffer.toByteArray()
        return try {
            processAll(allInput, allAad)
        } finally {
            resetState()
        }
    }

    actual fun doFinal(output: ByteArray, outputOffset: Int): Int {
        checkInitialized()
        if (outputOffset < 0) throw IllegalArgumentException("Bad outputOffset")
        val res = doFinal()
        if (output.size - outputOffset < res.size) {
            throw ShortBufferException("Output buffer too short")
        }
        res.copyInto(output, destinationOffset = outputOffset)
        return res.size
    }

    actual fun doFinal(input: ByteArray): ByteArray {
        return doFinal(input, 0, input.size)
    }

    actual fun doFinal(input: ByteArray, inputOffset: Int, inputLen: Int): ByteArray {
        checkInitialized()
        if (inputOffset < 0 || inputLen < 0 || inputOffset > input.size - inputLen) {
            throw IllegalArgumentException("Bad arguments")
        }
        if (inputLen > 0) {
            streamBuffer.write(input, inputOffset, inputLen)
        }
        return doFinal()
    }

    actual fun doFinal(input: ByteArray, inputOffset: Int, inputLen: Int, output: ByteArray): Int {
        return doFinal(input, inputOffset, inputLen, output, 0)
    }

    actual fun doFinal(input: ByteArray, inputOffset: Int, inputLen: Int, output: ByteArray, outputOffset: Int): Int {
        checkInitialized()
        if (inputOffset < 0 || inputLen < 0 || inputOffset > input.size - inputLen || outputOffset < 0) {
            throw IllegalArgumentException("Bad arguments")
        }
        if (inputLen > 0) {
            streamBuffer.write(input, inputOffset, inputLen)
        }
        val res = doFinal()
        if (output.size - outputOffset < res.size) {
            throw ShortBufferException("Output buffer too short")
        }
        res.copyInto(output, destinationOffset = outputOffset)
        return res.size
    }

    private fun processAll(input: ByteArray, aad: ByteArray): ByteArray {
        if (cipherFamily == CipherFamily.RSA) {
            return processRsa(input)
        }
        if (mode == Mode.GCM) {
            return processGcm(input, aad)
        }
        if (mode == Mode.CTR) {
            val data = if (opmode == ENCRYPT_MODE && padding == Padding.PKCS5_PADDING) {
                applyPkcs7Padding(input)
            } else {
                input
            }
            val out = processCtr(data)
            return if (opmode == DECRYPT_MODE && padding == Padding.PKCS5_PADDING) {
                removePkcs7Padding(out)
            } else {
                out
            }
        }

        return if (opmode == ENCRYPT_MODE) {
            val padded = when (padding) {
                Padding.NO_PADDING -> {
                    if (input.size % 16 != 0) {
                        throw IllegalBlockSizeException("Input length not multiple of 16 bytes")
                    }
                    input
                }
                Padding.PKCS5_PADDING, Padding.PKCS1_PADDING -> applyPkcs7Padding(input)
            }
            val out = ByteArray(padded.size)
            val block = ByteArray(16)
            for (offset in 0 until padded.size step 16) {
                if (mode == Mode.CBC) {
                    for (i in 0 until 16) {
                        block[i] = (padded[offset + i].toInt() xor currentVector[i].toInt()).toByte()
                    }
                    AesEngine.encryptBlock(block, 0, out, offset, roundKeys, numRounds)
                    out.copyInto(currentVector, destinationOffset = 0, startIndex = offset, endIndex = offset + 16)
                } else {
                    AesEngine.encryptBlock(padded, offset, out, offset, roundKeys, numRounds)
                }
            }
            out
        } else {
            if (input.size % 16 != 0) {
                throw IllegalBlockSizeException("Input length must be multiple of 16 when decrypting with padded cipher")
            }
            if (padding == Padding.PKCS5_PADDING && input.isEmpty()) {
                throw IllegalBlockSizeException("Input length must be multiple of 16 when decrypting with padded cipher")
            }
            val decrypted = ByteArray(input.size)
            val block = ByteArray(16)
            for (offset in 0 until input.size step 16) {
                if (mode == Mode.CBC) {
                    AesEngine.decryptBlock(input, offset, block, 0, roundKeys, numRounds)
                    for (i in 0 until 16) {
                        decrypted[offset + i] = (block[i].toInt() xor currentVector[i].toInt()).toByte()
                    }
                    input.copyInto(currentVector, destinationOffset = 0, startIndex = offset, endIndex = offset + 16)
                } else {
                    AesEngine.decryptBlock(input, offset, decrypted, offset, roundKeys, numRounds)
                }
            }
            when (padding) {
                Padding.NO_PADDING -> decrypted
                Padding.PKCS5_PADDING, Padding.PKCS1_PADDING -> removePkcs7Padding(decrypted)
            }
        }
    }

    private fun processRsa(input: ByteArray): ByteArray {
        val k = (rsaModulus.bitLength() + 7) / 8
        if (opmode == ENCRYPT_MODE) {
            val em = if (padding == Padding.PKCS1_PADDING) {
                if (input.size > k - 11) {
                    throw IllegalBlockSizeException("Data must not be longer than ${k - 11} bytes")
                }
                val psLen = k - 3 - input.size
                val block = ByteArray(k)
                block[0] = 0x00
                if (rsaIsPublicKey) {
                    block[1] = 0x02
                    val rndByte = ByteArray(1)
                    var i = 0
                    while (i < psLen) {
                        cipherRandom.nextBytes(rndByte)
                        if (rndByte[0] != 0.toByte()) {
                            block[2 + i] = rndByte[0]
                            i++
                        }
                    }
                } else {
                    block[1] = 0x01
                    for (i in 0 until psLen) block[2 + i] = 0xFF.toByte()
                }
                block[2 + psLen] = 0x00
                input.copyInto(block, 3 + psLen)
                block
            } else {
                if (input.size > k) {
                    throw IllegalBlockSizeException("Data must not be longer than $k bytes")
                }
                val block = ByteArray(k)
                input.copyInto(block, k - input.size)
                block
            }
            val m = BigInteger(1, em)
            if (m >= rsaModulus) throw IllegalBlockSizeException("Message is larger than modulus")
            val c = m.modPow(rsaExponent, rsaModulus)
            return toFixedLengthUnsigned(c, k)
        } else {
            if (input.size > k) {
                throw IllegalBlockSizeException("Decryption error: input larger than modulus")
            }
            val c = BigInteger(1, input)
            if (c >= rsaModulus) throw BadPaddingException("Decryption error: integer too large")
            val m = c.modPow(rsaExponent, rsaModulus)
            val em = toFixedLengthUnsigned(m, k)
            if (padding == Padding.NO_PADDING) {
                return em
            }
            if (em.size < 11 || em[0] != 0.toByte()) {
                throw BadPaddingException("Decryption error")
            }
            val bt = em[1].toInt() and 0xFF
            if ((rsaIsPublicKey && bt != 1) || (!rsaIsPublicKey && bt != 2)) {
                if (bt != 1 && bt != 2) throw BadPaddingException("Invalid PKCS#1 block type: $bt")
            }
            var sepIdx = -1
            for (i in 2 until em.size) {
                val b = em[i].toInt() and 0xFF
                if (b == 0) {
                    sepIdx = i
                    break
                }
                if (bt == 1 && b != 0xFF) {
                    throw BadPaddingException("Invalid PKCS#1 type 1 padding")
                }
            }
            if (sepIdx < 10) {
                throw BadPaddingException("PKCS#1 padding too short")
            }
            return em.copyOfRange(sepIdx + 1, em.size)
        }
    }

    private fun toFixedLengthUnsigned(v: BigInteger, length: Int): ByteArray {
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

    private fun processGcm(input: ByteArray, aad: ByteArray): ByteArray {
        val iv = initialIv ?: throw IllegalStateException("GCM IV not initialized")
        val tagBytes = gcmTagLenBits / 8
        val h = ByteArray(16)
        AesEngine.encryptBlock(ByteArray(16), 0, h, 0, roundKeys, numRounds)

        val j0 = if (iv.size == 12) {
            val block = ByteArray(16)
            iv.copyInto(block, 0, 0, 12)
            block[15] = 1
            block
        } else {
            ghash(h, ByteArray(0), iv)
        }

        val encJ0 = ByteArray(16)
        AesEngine.encryptBlock(j0, 0, encJ0, 0, roundKeys, numRounds)

        if (opmode == ENCRYPT_MODE) {
            val cb = j0.copyOf()
            inc32(cb)
            val ciphertext = gctr(cb, input)
            val s = ghash(h, aad, ciphertext)
            val out = ByteArray(ciphertext.size + tagBytes)
            ciphertext.copyInto(out, 0)
            for (i in 0 until tagBytes) {
                out[ciphertext.size + i] = (s[i].toInt() xor encJ0[i].toInt()).toByte()
            }
            return out
        } else {
            if (input.size < tagBytes) {
                throw AEADBadTagException("Input too short - need tag")
            }
            val ctLen = input.size - tagBytes
            val ciphertext = input.copyOfRange(0, ctLen)
            val actualTag = input.copyOfRange(ctLen, input.size)
            val s = ghash(h, aad, ciphertext)
            val expectedTag = ByteArray(tagBytes) { i ->
                (s[i].toInt() xor encJ0[i].toInt()).toByte()
            }
            if (!MessageDigest.isEqual(actualTag, expectedTag)) {
                throw AEADBadTagException("Tag mismatch!")
            }
            val cb = j0.copyOf()
            inc32(cb)
            return gctr(cb, ciphertext)
        }
    }

    private fun gctr(initialCounter: ByteArray, input: ByteArray): ByteArray {
        if (input.isEmpty()) return ByteArray(0)
        val cb = initialCounter.copyOf()
        val out = ByteArray(input.size)
        val keystream = ByteArray(16)
        var offset = 0
        while (offset < input.size) {
            AesEngine.encryptBlock(cb, 0, keystream, 0, roundKeys, numRounds)
            val chunk = minOf(16, input.size - offset)
            for (i in 0 until chunk) {
                out[offset + i] = (input[offset + i].toInt() xor keystream[i].toInt()).toByte()
            }
            inc32(cb)
            offset += chunk
        }
        return out
    }

    private fun inc32(block: ByteArray) {
        for (i in 15 downTo 12) {
            val next = (block[i].toInt() and 0xFF) + 1
            block[i] = next.toByte()
            if ((next and 0xFF) != 0) break
        }
    }

    private fun ghash(h: ByteArray, aad: ByteArray, ciphertext: ByteArray): ByteArray {
        val hHi = readLongBE(h, 0)
        val hLo = readLongBE(h, 8)
        var yHi = 0L
        var yLo = 0L

        fun processData(data: ByteArray) {
            var offset = 0
            val block = ByteArray(16)
            while (offset < data.size) {
                val chunk = minOf(16, data.size - offset)
                block.fill(0)
                data.copyInto(block, 0, offset, offset + chunk)
                yHi = yHi xor readLongBE(block, 0)
                yLo = yLo xor readLongBE(block, 8)
                val prod = gf128Multiply(yHi, yLo, hHi, hLo)
                yHi = prod[0]
                yLo = prod[1]
                offset += 16
            }
        }

        processData(aad)
        processData(ciphertext)

        val aadBits = aad.size.toLong() * 8L
        val ctBits = ciphertext.size.toLong() * 8L
        yHi = yHi xor aadBits
        yLo = yLo xor ctBits
        val prod = gf128Multiply(yHi, yLo, hHi, hLo)
        val out = ByteArray(16)
        writeLongBE(prod[0], out, 0)
        writeLongBE(prod[1], out, 8)
        return out
    }

    private fun gf128Multiply(xHi: Long, xLo: Long, yHi: Long, yLo: Long): LongArray {
        var zHi = 0L
        var zLo = 0L
        var vHi = yHi
        var vLo = yLo
        for (i in 0 until 128) {
            val bitSet = if (i < 64) {
                ((xHi ushr (63 - i)) and 1L) != 0L
            } else {
                ((xLo ushr (127 - i)) and 1L) != 0L
            }
            if (bitSet) {
                zHi = zHi xor vHi
                zLo = zLo xor vLo
            }
            val lsb = (vLo and 1L) != 0L
            vLo = (vLo ushr 1) or (vHi shl 63)
            vHi = vHi ushr 1
            if (lsb) {
                vHi = vHi xor (0xE1L shl 56)
            }
        }
        return longArrayOf(zHi, zLo)
    }

    private fun readLongBE(buf: ByteArray, offset: Int): Long {
        var res = 0L
        for (i in 0 until 8) {
            res = (res shl 8) or (buf[offset + i].toLong() and 0xFFL)
        }
        return res
    }

    private fun writeLongBE(v: Long, buf: ByteArray, offset: Int) {
        for (i in 0 until 8) {
            buf[offset + i] = ((v ushr (56 - i * 8)) and 0xFFL).toByte()
        }
    }

    private fun processCtr(input: ByteArray): ByteArray {
        val out = ByteArray(input.size)
        val keystreamBlock = ByteArray(16)
        var i = 0
        while (i < input.size) {
            AesEngine.encryptBlock(currentVector, 0, keystreamBlock, 0, roundKeys, numRounds)
            val chunk = minOf(16, input.size - i)
            for (j in 0 until chunk) {
                out[i + j] = (input[i + j].toInt() xor keystreamBlock[j].toInt()).toByte()
            }
            incrementCounter(currentVector)
            i += chunk
        }
        return out
    }

    private fun incrementCounter(counter: ByteArray) {
        for (i in 15 downTo 0) {
            val next = (counter[i].toInt() and 0xFF) + 1
            counter[i] = next.toByte()
            if ((next and 0xFF) != 0) break
        }
    }

    private fun applyPkcs7Padding(input: ByteArray): ByteArray {
        val padLen = 16 - (input.size % 16)
        val out = ByteArray(input.size + padLen)
        input.copyInto(out)
        val padByte = padLen.toByte()
        for (i in input.size until out.size) {
            out[i] = padByte
        }
        return out
    }

    private fun removePkcs7Padding(decrypted: ByteArray): ByteArray {
        if (decrypted.isEmpty() || decrypted.size % 16 != 0) {
            throw BadPaddingException("Invalid decrypted block length")
        }
        val padLen = decrypted[decrypted.size - 1].toInt() and 0xFF
        if (padLen < 1 || padLen > 16) {
            throw BadPaddingException("Given final block not properly padded")
        }
        for (i in decrypted.size - padLen until decrypted.size) {
            if ((decrypted[i].toInt() and 0xFF) != padLen) {
                throw BadPaddingException("Given final block not properly padded")
            }
        }
        return decrypted.copyOfRange(0, decrypted.size - padLen)
    }

    actual companion object {
        actual val ENCRYPT_MODE: Int = 1
        actual val DECRYPT_MODE: Int = 2
        actual val WRAP_MODE: Int = 3
        actual val UNWRAP_MODE: Int = 4
        actual val PUBLIC_KEY: Int = 1
        actual val PRIVATE_KEY: Int = 2
        actual val SECRET_KEY: Int = 3

        actual fun getInstance(transformation: String): Cipher {
            if (transformation.isEmpty()) {
                throw NoSuchAlgorithmException("Null or empty transformation")
            }
            val parts = transformation.split('/')
            val alg = parts[0].uppercase()
            if (alg == "RSA") {
                return when (parts.size) {
                    1 -> Cipher(transformation, CipherFamily.RSA, Mode.ECB, Padding.PKCS1_PADDING)
                    3 -> {
                        val modeStr = parts[1].uppercase()
                        if (modeStr != "ECB" && modeStr != "NONE" && modeStr.isNotEmpty()) {
                            throw NoSuchAlgorithmException("Unsupported RSA mode: ${parts[1]}")
                        }
                        val pad = when (parts[2].uppercase()) {
                            "PKCS1PADDING" -> Padding.PKCS1_PADDING
                            "NOPADDING" -> Padding.NO_PADDING
                            else -> throw NoSuchPaddingException("Unsupported RSA padding: ${parts[2]}")
                        }
                        Cipher(transformation, CipherFamily.RSA, Mode.ECB, pad)
                    }
                    else -> throw NoSuchAlgorithmException("Invalid transformation format: $transformation")
                }
            }
            if (alg != "AES" && alg != "AES_128" && alg != "AES_192" && alg != "AES_256") {
                throw NoSuchAlgorithmException("Cannot find any provider supporting $transformation")
            }
            return when (parts.size) {
                1 -> Cipher(transformation, CipherFamily.AES, Mode.ECB, Padding.PKCS5_PADDING)
                3 -> {
                    val mode = when (parts[1].uppercase()) {
                        "ECB" -> Mode.ECB
                        "CBC" -> Mode.CBC
                        "CTR" -> Mode.CTR
                        "GCM" -> Mode.GCM
                        else -> throw NoSuchAlgorithmException("Unsupported mode: ${parts[1]}")
                    }
                    val padding = when (parts[2].uppercase()) {
                        "NOPADDING" -> Padding.NO_PADDING
                        "PKCS5PADDING", "PKCS7PADDING" -> {
                            if (mode == Mode.GCM) {
                                throw NoSuchPaddingException("GCM mode must be used with NoPadding")
                            }
                            Padding.PKCS5_PADDING
                        }
                        else -> throw NoSuchPaddingException("Unsupported padding: ${parts[2]}")
                    }
                    Cipher(transformation, CipherFamily.AES, mode, padding)
                }
                else -> throw NoSuchAlgorithmException("Invalid transformation format: $transformation")
            }
        }
    }
}

actual open class KeyGenerator private constructor(
    private val alg: String,
    private var keySizeBits: Int
) {
    private var random: SecureRandom = SecureRandom()

    actual fun getAlgorithm(): String = alg
    actual val algorithm: String get() = alg

    actual fun init(random: SecureRandom) {
        this.random = random
    }

    actual fun init(keysize: Int) {
        init(keysize, SecureRandom())
    }

    actual fun init(keysize: Int, random: SecureRandom) {
        if (alg.equals("AES", ignoreCase = true)) {
            if (keysize != 128 && keysize != 192 && keysize != 256) {
                throw InvalidParameterException("Wrong keysize: must be equal to 128, 192 or 256")
            }
        } else if (keysize <= 0) {
            throw InvalidParameterException("Key length must be positive")
        }
        this.keySizeBits = keysize
        this.random = random
    }

    actual fun init(params: AlgorithmParameterSpec) {
        throw InvalidAlgorithmParameterException("KeyGenerator.$alg does not support AlgorithmParameterSpec")
    }

    actual fun init(params: AlgorithmParameterSpec, random: SecureRandom) {
        throw InvalidAlgorithmParameterException("KeyGenerator.$alg does not support AlgorithmParameterSpec")
    }

    actual fun generateKey(): SecretKey {
        val numBytes = (keySizeBits + 7) / 8
        val bytes = ByteArray(numBytes)
        random.nextBytes(bytes)
        return SecretKeySpec(bytes, alg)
    }

    actual companion object {
        actual fun getInstance(algorithm: String): KeyGenerator {
            val normalized = algorithm.uppercase().replace("-", "")
            return when (normalized) {
                "AES" -> KeyGenerator(algorithm, 128)
                "HMACMD5" -> KeyGenerator(algorithm, 128)
                "HMACSHA1" -> KeyGenerator(algorithm, 160)
                "HMACSHA256" -> KeyGenerator(algorithm, 256)
                "HMACSHA384" -> KeyGenerator(algorithm, 384)
                "HMACSHA512" -> KeyGenerator(algorithm, 512)
                else -> throw NoSuchAlgorithmException("KeyGenerator.$algorithm not available")
            }
        }
    }
}

actual open class SecretKeyFactory private constructor(
    private val alg: String,
    private val prfAlg: String?
) {
    actual fun getAlgorithm(): String = alg
    actual val algorithm: String get() = alg

    actual fun generateSecret(keySpec: KeySpec): SecretKey {
        if (prfAlg == null) {
            if (keySpec is SecretKeySpec) {
                return SecretKeySpec(keySpec.getEncoded(), alg)
            }
            throw InvalidKeySpecException("Unsupported KeySpec for $alg")
        }
        if (keySpec !is PBEKeySpec) {
            throw InvalidKeySpecException("PBEKeySpec required for PBKDF2")
        }
        val salt = keySpec.getSalt() ?: throw InvalidKeySpecException("Salt not found")
        val iterations = keySpec.getIterationCount()
        val keyLenBits = keySpec.getKeyLength()
        if (iterations <= 0 || keyLenBits <= 0) {
            throw InvalidKeySpecException("Iteration count and key length must be positive")
        }
        val passwordBytes = keySpec.getPassword().concatToString().encodeToByteArray()
        val mac = Mac.getInstance(prfAlg)
        // SecretKeySpec rejects empty byte arrays, so handle empty password by feeding single zero byte if empty
        val macKeyBytes = if (passwordBytes.isEmpty()) ByteArray(1) else passwordBytes
        if (passwordBytes.isEmpty()) {
            // Initialize HMAC with 0-length key directly using internal Mac logic:
            mac.init(SecretKeySpec(ByteArray(mac.getMacLength()), prfAlg))
        } else {
            mac.init(SecretKeySpec(macKeyBytes, prfAlg))
        }
        val hLen = mac.getMacLength()
        val dkLen = (keyLenBits + 7) / 8
        val numBlocks = (dkLen + hLen - 1) / hLen
        val derived = ByteArray(dkLen)
        val blockInput = ByteArray(salt.size + 4)
        salt.copyInto(blockInput, 0)

        var outOffset = 0
        for (blockIdx in 1..numBlocks) {
            blockInput[salt.size] = ((blockIdx ushr 24) and 0xFF).toByte()
            blockInput[salt.size + 1] = ((blockIdx ushr 16) and 0xFF).toByte()
            blockInput[salt.size + 2] = ((blockIdx ushr 8) and 0xFF).toByte()
            blockInput[salt.size + 3] = (blockIdx and 0xFF).toByte()

            var u = mac.doFinal(blockInput)
            val t = u.copyOf()
            for (iter in 2..iterations) {
                u = mac.doFinal(u)
                for (j in t.indices) {
                    t[j] = (t[j].toInt() xor u[j].toInt()).toByte()
                }
            }
            val copyLen = minOf(hLen, dkLen - outOffset)
            t.copyInto(derived, outOffset, 0, copyLen)
            outOffset += copyLen
        }
        return SecretKeySpec(derived, alg)
    }

    actual fun translateKey(key: SecretKey): SecretKey {
        val encoded = key.getEncoded() ?: throw InvalidKeyException("Key encoding is null")
        return SecretKeySpec(encoded, alg)
    }

    actual companion object {
        actual fun getInstance(algorithm: String): SecretKeyFactory {
            val upper = algorithm.uppercase().replace("-", "")
            return when (upper) {
                "PBKDF2WITHHMACSHA1" -> SecretKeyFactory(algorithm, "HmacSHA1")
                "PBKDF2WITHHMACSHA256" -> SecretKeyFactory(algorithm, "HmacSHA256")
                "PBKDF2WITHHMACSHA384" -> SecretKeyFactory(algorithm, "HmacSHA384")
                "PBKDF2WITHHMACSHA512" -> SecretKeyFactory(algorithm, "HmacSHA512")
                "AES" -> SecretKeyFactory(algorithm, null)
                else -> throw NoSuchAlgorithmException("SecretKeyFactory.$algorithm not available")
            }
        }
    }
}

private object AesEngine {
    private val SBOX = intArrayOf(
        0x63, 0x7c, 0x77, 0x7b, 0xf2, 0x6b, 0x6f, 0xc5, 0x30, 0x01, 0x67, 0x2b, 0xfe, 0xd7, 0xab, 0x76,
        0xca, 0x82, 0xc9, 0x7d, 0xfa, 0x59, 0x47, 0xf0, 0xad, 0xd4, 0xa2, 0xaf, 0x9c, 0xa4, 0x72, 0xc0,
        0xb7, 0xfd, 0x93, 0x26, 0x36, 0x3f, 0xf7, 0xcc, 0x34, 0xa5, 0xe5, 0xf1, 0x71, 0xd8, 0x31, 0x15,
        0x04, 0xc7, 0x23, 0xc3, 0x18, 0x96, 0x05, 0x9a, 0x07, 0x12, 0x80, 0xe2, 0xeb, 0x27, 0xb2, 0x75,
        0x09, 0x83, 0x2c, 0x1a, 0x1b, 0x6e, 0x5a, 0xa0, 0x52, 0x3b, 0xd6, 0xb3, 0x29, 0xe3, 0x2f, 0x84,
        0x53, 0xd1, 0x00, 0xed, 0x20, 0xfc, 0xb1, 0x5b, 0x6a, 0xcb, 0xbe, 0x39, 0x4a, 0x4c, 0x58, 0xcf,
        0xd0, 0xef, 0xaa, 0xfb, 0x43, 0x4d, 0x33, 0x85, 0x45, 0xf9, 0x02, 0x7f, 0x50, 0x3c, 0x9f, 0xa8,
        0x51, 0xa3, 0x40, 0x8f, 0x92, 0x9d, 0x38, 0xf5, 0xbc, 0xb6, 0xda, 0x21, 0x10, 0xff, 0xf3, 0xd2,
        0xcd, 0x0c, 0x13, 0xec, 0x5f, 0x97, 0x44, 0x17, 0xc4, 0xa7, 0x7e, 0x3d, 0x64, 0x5d, 0x19, 0x73,
        0x60, 0x81, 0x4f, 0xdc, 0x22, 0x2a, 0x90, 0x88, 0x46, 0xee, 0xb8, 0x14, 0xde, 0x5e, 0x0b, 0xdb,
        0xe0, 0x32, 0x3a, 0x0a, 0x49, 0x06, 0x24, 0x5c, 0xc2, 0xd3, 0xac, 0x62, 0x91, 0x95, 0xe4, 0x79,
        0xe7, 0xc8, 0x37, 0x6d, 0x8d, 0xd5, 0x4e, 0xa9, 0x6c, 0x56, 0xf4, 0xea, 0x65, 0x7a, 0xae, 0x08,
        0xba, 0x78, 0x25, 0x2e, 0x1c, 0xa6, 0xb4, 0xc6, 0xe8, 0xdd, 0x74, 0x1f, 0x4b, 0xbd, 0x8b, 0x8a,
        0x70, 0x3e, 0xb5, 0x66, 0x48, 0x03, 0xf6, 0x0e, 0x61, 0x35, 0x57, 0xb9, 0x86, 0xc1, 0x1d, 0x9e,
        0xe1, 0xf8, 0x98, 0x11, 0x69, 0xd9, 0x8e, 0x94, 0x9b, 0x1e, 0x87, 0xe9, 0xce, 0x55, 0x28, 0xdf,
        0x8c, 0xa1, 0x89, 0x0d, 0xbf, 0xe6, 0x42, 0x68, 0x41, 0x99, 0x2d, 0x0f, 0xb0, 0x54, 0xbb, 0x16
    )

    private val INV_SBOX = intArrayOf(
        0x52, 0x09, 0x6a, 0xd5, 0x30, 0x36, 0xa5, 0x38, 0xbf, 0x40, 0xa3, 0x9e, 0x81, 0xf3, 0xd7, 0xfb,
        0x7c, 0xe3, 0x39, 0x82, 0x9b, 0x2f, 0xff, 0x87, 0x34, 0x8e, 0x43, 0x44, 0xc4, 0xde, 0xe9, 0xcb,
        0x54, 0x7b, 0x94, 0x32, 0xa6, 0xc2, 0x23, 0x3d, 0xee, 0x4c, 0x95, 0x0b, 0x42, 0xfa, 0xc3, 0x4e,
        0x08, 0x2e, 0xa1, 0x66, 0x28, 0xd9, 0x24, 0xb2, 0x76, 0x5b, 0xa2, 0x49, 0x6d, 0x8b, 0xd1, 0x25,
        0x72, 0xf8, 0xf6, 0x64, 0x86, 0x68, 0x98, 0x16, 0xd4, 0xa4, 0x5c, 0xcc, 0x5d, 0x65, 0xb6, 0x92,
        0x6c, 0x70, 0x48, 0x50, 0xfd, 0xed, 0xb9, 0xda, 0x5e, 0x15, 0x46, 0x57, 0xa7, 0x8d, 0x9d, 0x84,
        0x90, 0xd8, 0xab, 0x00, 0x8c, 0xbc, 0xd3, 0x0a, 0xf7, 0xe4, 0x58, 0x05, 0xb8, 0xb3, 0x45, 0x06,
        0xd0, 0x2c, 0x1e, 0x8f, 0xca, 0x3f, 0x0f, 0x02, 0xc1, 0xaf, 0xbd, 0x03, 0x01, 0x13, 0x8a, 0x6b,
        0x3a, 0x91, 0x11, 0x41, 0x4f, 0x67, 0xdc, 0xea, 0x97, 0xf2, 0xcf, 0xce, 0xf0, 0xb4, 0xe6, 0x73,
        0x96, 0xac, 0x74, 0x22, 0xe7, 0xad, 0x35, 0x85, 0xe2, 0xf9, 0x37, 0xe8, 0x1c, 0x75, 0xdf, 0x6e,
        0x47, 0xf1, 0x1a, 0x71, 0x1d, 0x29, 0xc5, 0x89, 0x6f, 0xb7, 0x62, 0x0e, 0xaa, 0x18, 0xbe, 0x1b,
        0xfc, 0x56, 0x3e, 0x4b, 0xc6, 0xd2, 0x79, 0x20, 0x9a, 0xdb, 0xc0, 0xfe, 0x78, 0xcd, 0x5a, 0xf4,
        0x1f, 0xdd, 0xa8, 0x33, 0x88, 0x07, 0xc7, 0x31, 0xb1, 0x12, 0x10, 0x59, 0x27, 0x80, 0xec, 0x5f,
        0x60, 0x51, 0x7f, 0xa9, 0x19, 0xb5, 0x4a, 0x0d, 0x2d, 0xe5, 0x7a, 0x9f, 0x93, 0xc9, 0x9c, 0xef,
        0xa0, 0xe0, 0x3b, 0x4d, 0xae, 0x2a, 0xf5, 0xb0, 0xc8, 0xeb, 0xbb, 0x3c, 0x83, 0x53, 0x99, 0x61,
        0x17, 0x2b, 0x04, 0x7e, 0xba, 0x77, 0xd6, 0x26, 0xe1, 0x69, 0x14, 0x63, 0x55, 0x21, 0x0c, 0x7d
    )

    private val RCON = intArrayOf(
        0x00000000,
        0x01000000, 0x02000000, 0x04000000, 0x08000000,
        0x10000000, 0x20000000, 0x40000000, 0x80000000.toInt(),
        0x1b000000, 0x36000000
    )

    private fun subWord(word: Int): Int {
        return (SBOX[(word ushr 24) and 0xFF] shl 24) or
            (SBOX[(word ushr 16) and 0xFF] shl 16) or
            (SBOX[(word ushr 8) and 0xFF] shl 8) or
            SBOX[word and 0xFF]
    }

    private fun rotWord(word: Int): Int = (word shl 8) or (word ushr 24)

    fun expandKey(key: ByteArray, nr: Int): IntArray {
        val nk = key.size / 4
        val totalWords = 4 * (nr + 1)
        val w = IntArray(totalWords)
        for (i in 0 until nk) {
            w[i] = ((key[4 * i].toInt() and 0xFF) shl 24) or
                ((key[4 * i + 1].toInt() and 0xFF) shl 16) or
                ((key[4 * i + 2].toInt() and 0xFF) shl 8) or
                (key[4 * i + 3].toInt() and 0xFF)
        }
        for (i in nk until totalWords) {
            var temp = w[i - 1]
            if (i % nk == 0) {
                temp = subWord(rotWord(temp)) xor RCON[i / nk]
            } else if (nk > 6 && i % nk == 4) {
                temp = subWord(temp)
            }
            w[i] = w[i - nk] xor temp
        }
        return w
    }

    private fun gmul(a: Int, b: Int): Int {
        var p = 0
        var aa = a and 0xFF
        var bb = b and 0xFF
        for (i in 0 until 8) {
            if ((bb and 1) != 0) {
                p = p xor aa
            }
            val hiBit = aa and 0x80
            aa = (aa shl 1) and 0xFF
            if (hiBit != 0) {
                aa = aa xor 0x1b
            }
            bb = bb ushr 1
        }
        return p
    }

    fun encryptBlock(inBuf: ByteArray, inOff: Int, outBuf: ByteArray, outOff: Int, w: IntArray, nr: Int) {
        val s = IntArray(16)
        for (i in 0 until 16) {
            s[i] = inBuf[inOff + i].toInt() and 0xFF
        }

        addRoundKey(s, w, 0)

        for (round in 1 until nr) {
            for (i in 0 until 16) s[i] = SBOX[s[i]]
            shiftRows(s)
            mixColumns(s)
            addRoundKey(s, w, round)
        }

        for (i in 0 until 16) s[i] = SBOX[s[i]]
        shiftRows(s)
        addRoundKey(s, w, nr)

        for (i in 0 until 16) {
            outBuf[outOff + i] = s[i].toByte()
        }
    }

    fun decryptBlock(inBuf: ByteArray, inOff: Int, outBuf: ByteArray, outOff: Int, w: IntArray, nr: Int) {
        val s = IntArray(16)
        for (i in 0 until 16) {
            s[i] = inBuf[inOff + i].toInt() and 0xFF
        }

        addRoundKey(s, w, nr)

        for (round in nr - 1 downTo 1) {
            invShiftRows(s)
            for (i in 0 until 16) s[i] = INV_SBOX[s[i]]
            addRoundKey(s, w, round)
            invMixColumns(s)
        }

        invShiftRows(s)
        for (i in 0 until 16) s[i] = INV_SBOX[s[i]]
        addRoundKey(s, w, 0)

        for (i in 0 until 16) {
            outBuf[outOff + i] = s[i].toByte()
        }
    }

    private fun addRoundKey(s: IntArray, w: IntArray, round: Int) {
        for (c in 0 until 4) {
            val kw = w[round * 4 + c]
            s[c * 4] = s[c * 4] xor ((kw ushr 24) and 0xFF)
            s[c * 4 + 1] = s[c * 4 + 1] xor ((kw ushr 16) and 0xFF)
            s[c * 4 + 2] = s[c * 4 + 2] xor ((kw ushr 8) and 0xFF)
            s[c * 4 + 3] = s[c * 4 + 3] xor (kw and 0xFF)
        }
    }

    private fun shiftRows(s: IntArray) {
        // Row 1: shift left 1
        var t = s[1]
        s[1] = s[5]; s[5] = s[9]; s[9] = s[13]; s[13] = t
        // Row 2: shift left 2
        t = s[2]; s[2] = s[10]; s[10] = t
        t = s[6]; s[6] = s[14]; s[14] = t
        // Row 3: shift left 3 (right 1)
        t = s[15]
        s[15] = s[11]; s[11] = s[7]; s[7] = s[3]; s[3] = t
    }

    private fun invShiftRows(s: IntArray) {
        // Row 1: shift right 1
        var t = s[13]
        s[13] = s[9]; s[9] = s[5]; s[5] = s[1]; s[1] = t
        // Row 2: shift right 2
        t = s[2]; s[2] = s[10]; s[10] = t
        t = s[6]; s[6] = s[14]; s[14] = t
        // Row 3: shift right 3 (left 1)
        t = s[3]
        s[3] = s[7]; s[7] = s[11]; s[11] = s[15]; s[15] = t
    }

    private fun mixColumns(s: IntArray) {
        for (c in 0 until 4) {
            val i = c * 4
            val s0 = s[i]; val s1 = s[i + 1]; val s2 = s[i + 2]; val s3 = s[i + 3]
            s[i] = gmul(0x02, s0) xor gmul(0x03, s1) xor s2 xor s3
            s[i + 1] = s0 xor gmul(0x02, s1) xor gmul(0x03, s2) xor s3
            s[i + 2] = s0 xor s1 xor gmul(0x02, s2) xor gmul(0x03, s3)
            s[i + 3] = gmul(0x03, s0) xor s1 xor s2 xor gmul(0x02, s3)
        }
    }

    private fun invMixColumns(s: IntArray) {
        for (c in 0 until 4) {
            val i = c * 4
            val s0 = s[i]; val s1 = s[i + 1]; val s2 = s[i + 2]; val s3 = s[i + 3]
            s[i] = gmul(0x0e, s0) xor gmul(0x0b, s1) xor gmul(0x0d, s2) xor gmul(0x09, s3)
            s[i + 1] = gmul(0x09, s0) xor gmul(0x0e, s1) xor gmul(0x0b, s2) xor gmul(0x0d, s3)
            s[i + 2] = gmul(0x0d, s0) xor gmul(0x09, s1) xor gmul(0x0e, s2) xor gmul(0x0b, s3)
            s[i + 3] = gmul(0x0b, s0) xor gmul(0x0d, s1) xor gmul(0x09, s2) xor gmul(0x0e, s3)
        }
    }
}
