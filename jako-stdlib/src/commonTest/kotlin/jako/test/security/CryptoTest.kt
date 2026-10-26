package jako.test.security

import java.nio.ByteBuffer
import java.security.DigestException
import java.security.GeneralSecurityException
import java.security.InvalidAlgorithmParameterException
import java.security.InvalidKeyException
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.security.Signature
import java.security.interfaces.RSAKey
import java.security.interfaces.RSAPrivateKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.ECGenParameterSpec
import java.security.spec.EncodedKeySpec
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.RSAPrivateKeySpec
import java.security.spec.RSAPublicKeySpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.AEADBadTagException
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.KeyGenerator
import javax.crypto.Mac
import javax.crypto.NoSuchPaddingException
import javax.crypto.SecretKeyFactory
import javax.crypto.ShortBufferException
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.PBEKeySpec
import javax.crypto.spec.SecretKeySpec
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class CryptoTest {

    private fun ByteArray.toHex(): String = joinToString("") {
        (it.toInt() and 0xFF).toString(16).padStart(2, '0')
    }

    private fun hexToBytes(hex: String): ByteArray {
        val out = ByteArray(hex.length / 2)
        for (i in out.indices) {
            out[i] = hex.substring(i * 2, i * 2 + 2).toInt(16).toByte()
        }
        return out
    }

    @Test
    fun testSha384AndSha512() {
        val md384 = MessageDigest.getInstance("SHA-384")
        assertEquals("SHA-384", md384.algorithm)
        assertEquals(48, md384.digestLength)
        assertEquals(
            "cb00753f45a35e8bb5a03d699ac65007272c32ab0eded1631a8b605a43ff5bed8086072ba1e7cc2358baeca134c825a7",
            md384.digest("abc".encodeToByteArray()).toHex()
        )

        val md512 = MessageDigest.getInstance("SHA-512")
        assertEquals("SHA-512", md512.algorithm)
        assertEquals(64, md512.digestLength)
        assertEquals(
            "ddaf35a193617abacc417349ae20413112e6fa4e89a97ea20a9eeee64b55d39a2192992a274fc1a836ba3c23a3feebbd454d4423643ce80e2a9ac94fa54ca49f",
            md512.digest("abc".encodeToByteArray()).toHex()
        )

        // Multi-block input (> 112 bytes for SHA-512 padding boundary)
        val longInput = "abcdefghbcdefghicdefghijdefghijkefghijklfghijklmghijklmnhijklmnoijklmnopjklmnopqklmnopqrlmnopqrsmnopqrstnopqrstu"
        assertEquals(
            "8e959b75dae313da8cf4f72814fc143f8f7779c6eb9f7fa17299aeadb6889018501d289e4900f7e4331b99dec4b5433ac7d329eeb6dd26545e96e55b874be909",
            md512.digest(longInput.encodeToByteArray()).toHex()
        )
        assertEquals(
            "09330c33f71147e83d192fc782cd1b4753111b173b3b05d22fa08086e3b0f712fcc7c71a557e2db966c3e9fa91746039",
            md384.digest(longInput.encodeToByteArray()).toHex()
        )
    }

    @Test
    fun testMessageDigestByteBufferAndOffset() {
        val md = MessageDigest.getInstance("SHA-256")
        val buf = ByteBuffer.wrap("abc".encodeToByteArray())
        md.update(buf)
        assertEquals(0, buf.remaining())

        val out = ByteArray(40)
        val written = md.digest(out, 4, 32)
        assertEquals(32, written)
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            out.copyOfRange(4, 36).toHex()
        )

        md.update("abc".encodeToByteArray())
        assertFailsWith<DigestException> {
            md.digest(ByteArray(32), 0, 16)
        }
    }

    @Test
    fun testSecretKeySpecAndIvParameterSpec() {
        val raw = byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17)
        val key1 = SecretKeySpec(raw, 1, 16, "AES")
        val key2 = SecretKeySpec(raw.copyOfRange(1, 17), "aes")
        assertEquals("AES", key1.algorithm)
        assertEquals("RAW", key1.format)
        assertEquals(key1, key2)
        assertEquals(key1.hashCode(), key2.hashCode())

        val encoded = key1.encoded
        encoded[0] = 99
        assertNotEquals(99.toByte(), key1.encoded[0])

        assertFailsWith<IllegalArgumentException> {
            SecretKeySpec(ByteArray(0), "AES")
        }

        val ivSpec = IvParameterSpec(raw, 2, 16)
        assertContentEquals(raw.copyOfRange(2, 18), ivSpec.iv)
        assertContentEquals(raw.copyOfRange(2, 18), ivSpec.getIV())
    }

    @Test
    fun testMacHmacVariants() {
        // RFC 4231 Test Case 2: key = "Jefe", data = "what do ya want for nothing?"
        val keyBytes = "Jefe".encodeToByteArray()
        val dataBytes = "what do ya want for nothing?".encodeToByteArray()

        val hmacMd5 = Mac.getInstance("HmacMD5")
        hmacMd5.init(SecretKeySpec(keyBytes, "HmacMD5"))
        assertEquals(16, hmacMd5.macLength)
        assertEquals("750c783e6ab0b503eaa86e310a5db738", hmacMd5.doFinal(dataBytes).toHex())

        val hmacSha1 = Mac.getInstance("HmacSHA1")
        hmacSha1.init(SecretKeySpec(keyBytes, "HmacSHA1"))
        assertEquals(20, hmacSha1.macLength)
        assertEquals("effcdf6ae5eb2fa2d27416d5f184df9c259a7c79", hmacSha1.doFinal(dataBytes).toHex())

        val hmacSha256 = Mac.getInstance("HmacSHA256")
        hmacSha256.init(SecretKeySpec(keyBytes, "HmacSHA256"))
        assertEquals(32, hmacSha256.macLength)
        assertEquals(
            "5bdcc146bf60754e6a042426089575c75a003f089d2739839dec58b964ec3843",
            hmacSha256.doFinal(dataBytes).toHex()
        )

        val hmacSha384 = Mac.getInstance("HmacSHA384")
        hmacSha384.init(SecretKeySpec(keyBytes, "HmacSHA384"))
        assertEquals(48, hmacSha384.macLength)
        assertEquals(
            "af45d2e376484031617f78d2b58a6b1b9c7ef464f5a01b47e42ec3736322445e8e2240ca5e69e2c78b3239ecfab21649",
            hmacSha384.doFinal(dataBytes).toHex()
        )

        val hmacSha512 = Mac.getInstance("HmacSHA512")
        hmacSha512.init(SecretKeySpec(keyBytes, "HmacSHA512"))
        assertEquals(64, hmacSha512.macLength)
        assertEquals(
            "164b7a7bfcf819e2e395fbe73b56e0a387bd64222e831fd610270cd7ea2505549758bf75c05a994a6d034f65f8f0e6fdcaeab1a34d4a6b4b636e070a38bce737",
            hmacSha512.doFinal(dataBytes).toHex()
        )

        // Test long key (> blockSize) + streaming update + reuse
        val longKey = SecretKeySpec(ByteArray(131) { 0xaa.toByte() }, "HmacSHA256")
        hmacSha256.init(longKey)
        val msg = "Test Using Larger Than Block-Size Key - Hash Key First".encodeToByteArray()
        hmacSha256.update(msg[0])
        hmacSha256.update(msg, 1, 10)
        hmacSha256.update(ByteBuffer.wrap(msg, 11, msg.size - 11))
        val outBuf = ByteArray(40)
        hmacSha256.doFinal(outBuf, 4)
        assertEquals(
            "60e431591ee0b67f0d8a26aacbf5b77f8e0bc6213728c5140546040f0ee37f54",
            outBuf.copyOfRange(4, 36).toHex()
        )

        // Reuse without re-init
        assertEquals(
            "60e431591ee0b67f0d8a26aacbf5b77f8e0bc6213728c5140546040f0ee37f54",
            hmacSha256.doFinal(msg).toHex()
        )

        assertFailsWith<ShortBufferException> {
            hmacSha256.doFinal(ByteArray(10), 0)
        }
    }

    @Test
    fun testAesCipherVectorsAndModes() {
        // NIST SP 800-38A F.1.1 AES-128 ECB vector
        val key128 = SecretKeySpec(hexToBytes("2b7e151628aed2a6abf7158809cf4f3c"), "AES")
        val plainBlock = hexToBytes("6bc1bee22e409f96e93d7e117393172a")
        val expectedCipherBlock = hexToBytes("3ad77bb40d7a3660a89ecaf32466ef97")

        val ecbNoPad = Cipher.getInstance("AES/ECB/NoPadding")
        assertEquals(16, ecbNoPad.blockSize)
        ecbNoPad.init(Cipher.ENCRYPT_MODE, key128)
        assertContentEquals(expectedCipherBlock, ecbNoPad.doFinal(plainBlock))

        ecbNoPad.init(Cipher.DECRYPT_MODE, key128)
        assertContentEquals(plainBlock, ecbNoPad.doFinal(expectedCipherBlock))

        assertFailsWith<IllegalBlockSizeException> {
            ecbNoPad.doFinal(ByteArray(15))
        }

        // AES-256 CBC PKCS5Padding round-trip + streaming update
        val key256 = SecretKeySpec(ByteArray(32) { (it * 3 + 1).toByte() }, "AES")
        val iv = IvParameterSpec(ByteArray(16) { (15 - it).toByte() })
        val message = "Hello Kotlin Multiplatform & Native AES-256-CBC Encryption!".encodeToByteArray()

        val cbcEnc = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cbcEnc.init(Cipher.ENCRYPT_MODE, key256, iv)
        assertContentEquals(iv.iv, cbcEnc.iv)
        val p1 = cbcEnc.update(message, 0, 10) ?: ByteArray(0)
        val p2 = cbcEnc.doFinal(message, 10, message.size - 10)
        val encrypted = p1 + p2
        assertEquals(64, encrypted.size)

        val cbcDec = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cbcDec.init(Cipher.DECRYPT_MODE, key256, iv)
        val decrypted = cbcDec.doFinal(encrypted)
        assertContentEquals(message, decrypted)

        // Tamper with last byte -> BadPaddingException
        val tampered = encrypted.copyOf()
        tampered[tampered.size - 1] = (tampered[tampered.size - 1].toInt() xor 0xFF).toByte()
        assertFailsWith<BadPaddingException> {
            cbcDec.doFinal(tampered)
        }

        // AES-192 CTR NoPadding round-trip
        val key192 = SecretKeySpec(ByteArray(24) { (it + 7).toByte() }, "AES")
        val ctrEnc = Cipher.getInstance("AES/CTR/NoPadding")
        ctrEnc.init(Cipher.ENCRYPT_MODE, key192, iv)
        val ctrCiphertext = ctrEnc.doFinal(message)
        assertEquals(message.size, ctrCiphertext.size)

        val ctrDec = Cipher.getInstance("AES/CTR/NoPadding")
        ctrDec.init(Cipher.DECRYPT_MODE, key192, iv)
        assertContentEquals(message, ctrDec.doFinal(ctrCiphertext))

        // Error cases
        assertFailsWith<NoSuchAlgorithmException> {
            Cipher.getInstance("UNKNOWN/ECB/PKCS5Padding")
        }
        assertFailsWith<GeneralSecurityException> {
            Cipher.getInstance("AES/CBC/UnknownPadding")
        }
        assertFailsWith<InvalidKeyException> {
            cbcEnc.init(Cipher.ENCRYPT_MODE, SecretKeySpec(ByteArray(15), "AES"), iv)
        }
        assertFailsWith<InvalidAlgorithmParameterException> {
            cbcEnc.init(Cipher.ENCRYPT_MODE, key128, IvParameterSpec(ByteArray(12)))
        }
    }

    @Test
    fun testAesGcmVectorsAndAadTamper() {
        // NIST SP 800-38D Test Case 4 (AES-128-GCM with 96-bit IV and AAD)
        val key = SecretKeySpec(hexToBytes("feffe9928665731c6d6a8f9467308308"), "AES")
        val iv = hexToBytes("cafebabefacedbaddecaf888")
        val pt = hexToBytes(
            "d9313225f88406e5a55909c5aff5269a" +
                "86a7a9531534f7da2e4c303d8a318a72" +
                "1c3c0c95956809532fcf0e2449a6b525" +
                "b16aedf5aa0de657ba637b39"
        )
        val aad = hexToBytes("feedfacedeadbeeffeedfacedeadbeefabaddad2")
        val expectedCt = hexToBytes(
            "42831ec2217774244b7221b784d0d49c" +
                "e3aa212f2c02a4e035c17e2329aca12e" +
                "21d514b25466931c7d8f6a5aac84aa05" +
                "1ba30b396a0aac973d58e091"
        )
        val expectedTag = hexToBytes("5bc94fbc3221a5db94fae95ae7121a47")

        val gcmSpec = GCMParameterSpec(128, iv)
        assertEquals(128, gcmSpec.tLen)
        assertContentEquals(iv, gcmSpec.iv)

        val enc = Cipher.getInstance("AES/GCM/NoPadding")
        enc.init(Cipher.ENCRYPT_MODE, key, gcmSpec)
        enc.updateAAD(aad)
        val ctAndTag = enc.doFinal(pt)
        assertContentEquals(expectedCt + expectedTag, ctAndTag)

        val dec = Cipher.getInstance("AES/GCM/NoPadding")
        dec.init(Cipher.DECRYPT_MODE, key, gcmSpec)
        dec.updateAAD(ByteBuffer.wrap(aad))
        val recovered = dec.doFinal(ctAndTag)
        assertContentEquals(pt, recovered)

        // Tamper with AAD -> AEADBadTagException
        dec.init(Cipher.DECRYPT_MODE, key, gcmSpec)
        val badAad = aad.copyOf().also { it[0] = (it[0].toInt() xor 1).toByte() }
        dec.updateAAD(badAad)
        assertFailsWith<AEADBadTagException> {
            dec.doFinal(ctAndTag)
        }

        // Tamper with tag -> AEADBadTagException
        dec.init(Cipher.DECRYPT_MODE, key, gcmSpec)
        dec.updateAAD(aad)
        val badCt = ctAndTag.copyOf().also { it[it.size - 1] = (it[it.size - 1].toInt() xor 1).toByte() }
        assertFailsWith<AEADBadTagException> {
            dec.doFinal(badCt)
        }
    }

    @Test
    fun testPbkdf2AndKeyGenerator() {
        // RFC 6070 PBKDF2-HMAC-SHA1 test vector: P="password", S="salt", c=4096, dkLen=20
        val skfSha1 = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1")
        val spec1 = PBEKeySpec("password".toCharArray(), "salt".encodeToByteArray(), 4096, 160)
        val key1 = skfSha1.generateSecret(spec1)
        assertEquals("4b007901b765489abead49d926f721d065a429c1", key1.encoded.toHex())
        spec1.clearPassword()

        // PBKDF2-HMAC-SHA256 vector: P="password", S="salt", c=4096, dkLen=32
        val skfSha256 = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256")
        val spec256 = PBEKeySpec("password".toCharArray(), "salt".encodeToByteArray(), 4096, 256)
        val key256 = skfSha256.generateSecret(spec256)
        assertEquals(
            "c5e478d59288c841aa530db6845c4c8d962893a001ce4e11a4963873aa98134a",
            key256.encoded.toHex()
        )

        val aesGen = KeyGenerator.getInstance("AES")
        aesGen.init(256)
        val aesKey = aesGen.generateKey()
        assertEquals("AES", aesKey.algorithm)
        assertEquals(32, aesKey.encoded.size)

        val hmacGen = KeyGenerator.getInstance("HmacSHA256")
        val hmacKey = hmacGen.generateKey()
        assertEquals(32, hmacKey.encoded.size)
    }

    @Test
    fun testRsaKeyPairKeyFactorySignatureAndCipher() {
        val kpg = KeyPairGenerator.getInstance("RSA")
        kpg.initialize(512)
        val kp = kpg.generateKeyPair()
        val pub = kp.public as RSAPublicKey
        val priv = kp.private as RSAPrivateKey
        assertEquals(512, pub.modulus.bitLength())
        assertEquals(pub.modulus, priv.modulus)

        // X.509 & PKCS#8 DER round-trip via KeyFactory
        val kf = KeyFactory.getInstance("RSA")
        val decodedPub = kf.generatePublic(X509EncodedKeySpec(pub.encoded)) as RSAPublicKey
        val decodedPriv = kf.generatePrivate(PKCS8EncodedKeySpec(priv.encoded)) as RSAPrivateKey
        assertEquals(pub.modulus, decodedPub.modulus)
        assertEquals(pub.publicExponent, decodedPub.publicExponent)
        assertEquals(priv.modulus, decodedPriv.modulus)
        assertEquals(priv.privateExponent, decodedPriv.privateExponent)

        // RSAPublicKeySpec & RSAPrivateKeySpec via KeyFactory
        val specPub = kf.generatePublic(RSAPublicKeySpec(pub.modulus, pub.publicExponent)) as RSAPublicKey
        val specPriv = kf.generatePrivate(RSAPrivateKeySpec(priv.modulus, priv.privateExponent)) as RSAPrivateKey
        assertEquals(pub.modulus, specPub.modulus)
        assertEquals(priv.privateExponent, specPriv.privateExponent)

        // Signature SHA256withRSA
        val data = "Signed message for RSA-SHA256 verification".encodeToByteArray()
        val signer = Signature.getInstance("SHA256withRSA")
        signer.initSign(decodedPriv)
        signer.update(data)
        val sigBytes = signer.sign()
        assertTrue(sigBytes.isNotEmpty())

        val verifier = Signature.getInstance("SHA256withRSA")
        verifier.initVerify(decodedPub)
        verifier.update(data)
        assertTrue(verifier.verify(sigBytes))

        verifier.update(data)
        val tamperedSig = sigBytes.copyOf().also { it[it.size - 1] = (it[it.size - 1].toInt() xor 1).toByte() }
        assertFalse(verifier.verify(tamperedSig))

        // Cipher RSA/ECB/PKCS1Padding
        val rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding")
        val secretPayload = "RSA-PKCS1-Secret-Key-Material".encodeToByteArray()
        rsaCipher.init(Cipher.ENCRYPT_MODE, decodedPub)
        val encrypted = rsaCipher.doFinal(secretPayload)
        assertEquals(64, encrypted.size)

        rsaCipher.init(Cipher.DECRYPT_MODE, specPriv)
        val decrypted = rsaCipher.doFinal(encrypted)
        assertContentEquals(secretPayload, decrypted)
    }

    @Test
    fun testEcdsaP256KeyGenKeyFactoryAndSignature() {
        val kpg = KeyPairGenerator.getInstance("EC")
        kpg.initialize(ECGenParameterSpec("secp256r1"))
        val kp = kpg.generateKeyPair()

        val kf = KeyFactory.getInstance("EC")
        val pub = kf.generatePublic(X509EncodedKeySpec(kp.public!!.encoded))
        val priv = kf.generatePrivate(PKCS8EncodedKeySpec(kp.private!!.encoded))
        assertContentEquals(kp.public!!.encoded, pub.encoded)
        assertContentEquals(kp.private!!.encoded, priv.encoded)

        val message = "Hello ECDSA P-256 digital signature!".encodeToByteArray()
        val signer = Signature.getInstance("SHA256withECDSA")
        signer.initSign(priv)
        signer.update(message)
        val signature = signer.sign()

        val verifier = Signature.getInstance("SHA256withECDSA")
        verifier.initVerify(pub)
        verifier.update(message)
        assertTrue(verifier.verify(signature))

        // Tamper with message -> false
        verifier.update("Tampered ECDSA message!".encodeToByteArray())
        assertFalse(verifier.verify(signature))
    }

    @Test
    fun testNistEmptyAndMultiBlockShaAndRfc4231HmacVectors() {
        // NIST FIPS 180-4 empty string vectors
        val empty = ByteArray(0)
        assertEquals(
            "da39a3ee5e6b4b0d3255bfef95601890afd80709",
            MessageDigest.getInstance("SHA-1").digest(empty).toHex()
        )
        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            MessageDigest.getInstance("SHA-256").digest(empty).toHex()
        )
        assertEquals(
            "38b060a751ac96384cd9327eb1b1e36a21fdb71114be07434c0cc7bf63f6e1da274edebfe76f65fbd51ad2f14898b95b",
            MessageDigest.getInstance("SHA-384").digest(empty).toHex()
        )
        assertEquals(
            "cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e",
            MessageDigest.getInstance("SHA-512").digest(empty).toHex()
        )

        // NIST 448-bit multi-block vector ("abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq")
        val multiBlock = "abcdbcdecdefdefgefghfghighijhijkijkljklmklmnlmnomnopnopq".encodeToByteArray()
        assertEquals(
            "84983e441c3bd26ebaae4aa1f95129e5e54670f1",
            MessageDigest.getInstance("SHA-1").digest(multiBlock).toHex()
        )
        assertEquals(
            "248d6a61d20638b8e5c026930c3e6039a33ce45964ff2167f6ecedd419db06c1",
            MessageDigest.getInstance("SHA-256").digest(multiBlock).toHex()
        )

        // RFC 4231 Test Case 1: Key = 20 bytes of 0x0b, Data = "Hi There"
        val keyCase1 = ByteArray(20) { 0x0b.toByte() }
        val dataCase1 = "Hi There".encodeToByteArray()
        val mac256 = Mac.getInstance("HmacSHA256")
        mac256.init(SecretKeySpec(keyCase1, "HmacSHA256"))
        assertEquals(
            "b0344c61d8db38535ca8afceaf0bf12b881dc200c9833da726e9376c2e32cff7",
            mac256.doFinal(dataCase1).toHex()
        )

        val mac384 = Mac.getInstance("HmacSHA384")
        mac384.init(SecretKeySpec(keyCase1, "HmacSHA384"))
        assertEquals(
            "afd03944d84895626b0825f4ab46907f15f9dadbe4101ec682aa034c7cebc59cfaea9ea9076ede7f4af152e8b2fa9cb6",
            mac384.doFinal(dataCase1).toHex()
        )

        val mac512 = Mac.getInstance("HmacSHA512")
        mac512.init(SecretKeySpec(keyCase1, "HmacSHA512"))
        assertEquals(
            "87aa7cdea5ef619d4ff0b4241a1d6cb02379f4e2ce4ec2787ad0b30545e17cde" +
                "daa833b7d6b8a702038b274eaea3f4e4be9d914eeb61f1702e696c203a126854",
            mac512.doFinal(dataCase1).toHex()
        )
    }

    @Test
    fun testNistAesCtrAndGcmTagSizesVectors() {
        // NIST SP 800-38A F.5.1 CTR-AES128.Encrypt
        val ctrKey = SecretKeySpec(hexToBytes("2b7e151628aed2a6abf7158809cf4f3c"), "AES")
        val initCounter = IvParameterSpec(hexToBytes("f0f1f2f3f4f5f6f7f8f9fafbfcfdfeff"))
        val plain = hexToBytes("6bc1bee22e409f96e93d7e117393172aae2d8a571e03ac9c9eb76fac45af8e51")
        val expectedCtr = hexToBytes("874d6191b620e3261bef6864990db6ce9806f66b7970fdff8617187bb9fffdff")

        val ctr = Cipher.getInstance("AES/CTR/NoPadding")
        ctr.init(Cipher.ENCRYPT_MODE, ctrKey, initCounter)
        assertContentEquals(expectedCtr, ctr.doFinal(plain))

        ctr.init(Cipher.DECRYPT_MODE, ctrKey, initCounter)
        assertContentEquals(plain, ctr.doFinal(expectedCtr))

        // AES-GCM with 96-bit (12-byte) authentication tag and empty plaintext (GMAC mode)
        val gcmKey = SecretKeySpec(hexToBytes("feffe9928665731c6d6a8f9467308308"), "AES")
        val gcmIv = hexToBytes("cafebabefacedbaddecaf888")
        val aad = "authenticated-header-only".encodeToByteArray()

        val gcm96 = Cipher.getInstance("AES/GCM/NoPadding")
        gcm96.init(Cipher.ENCRYPT_MODE, gcmKey, GCMParameterSpec(96, gcmIv))
        gcm96.updateAAD(aad)
        val tagOnly = gcm96.doFinal(ByteArray(0))
        assertEquals(12, tagOnly.size)

        val gcm96Dec = Cipher.getInstance("AES/GCM/NoPadding")
        gcm96Dec.init(Cipher.DECRYPT_MODE, gcmKey, GCMParameterSpec(96, gcmIv))
        gcm96Dec.updateAAD(aad)
        assertContentEquals(ByteArray(0), gcm96Dec.doFinal(tagOnly))
    }

    @Test
    fun testRsaKeyAndEncodedKeySpecRoundTrips() {
        val kpg = KeyPairGenerator.getInstance("RSA")
        kpg.initialize(512)
        val kp = kpg.generateKeyPair()

        val pubKey = kp.public as RSAPublicKey
        val privKey = kp.private as RSAPrivateKey

        // Verify RSAKey superinterface contract on both public and private keys
        val pubRsaKey: RSAKey = pubKey
        val privRsaKey: RSAKey = privKey
        assertEquals(pubRsaKey.getModulus(), privRsaKey.getModulus())
        assertEquals(pubRsaKey.modulus, privRsaKey.modulus)
        assertEquals(512, pubRsaKey.modulus.bitLength())

        // Verify EncodedKeySpec hierarchy (X509EncodedKeySpec & PKCS8EncodedKeySpec)
        val x509Spec: EncodedKeySpec = X509EncodedKeySpec(pubKey.encoded)
        assertEquals("X.509", x509Spec.getFormat())
        assertEquals("X.509", x509Spec.format)
        assertContentEquals(pubKey.encoded, x509Spec.getEncoded())
        assertContentEquals(pubKey.encoded, x509Spec.encoded)

        val pkcs8Spec: EncodedKeySpec = PKCS8EncodedKeySpec(privKey.encoded)
        assertEquals("PKCS#8", pkcs8Spec.getFormat())
        assertEquals("PKCS#8", pkcs8Spec.format)
        assertContentEquals(privKey.encoded, pkcs8Spec.getEncoded())
        assertContentEquals(privKey.encoded, pkcs8Spec.encoded)

        val kf = KeyFactory.getInstance("RSA")
        val decodedPub = kf.generatePublic(x509Spec) as RSAPublicKey
        val decodedPriv = kf.generatePrivate(pkcs8Spec) as RSAPrivateKey
        assertEquals(pubRsaKey.modulus, (decodedPub as RSAKey).modulus)
        assertEquals(privRsaKey.modulus, (decodedPriv as RSAKey).modulus)

        // KeyFactory.getKeySpec and translateKey round-trip
        val pubSpecTemplate = RSAPublicKeySpec(pubRsaKey.modulus, pubKey.publicExponent)
        val extractedPubSpec = kf.getKeySpec(decodedPub, pubSpecTemplate.javaClass)
        assertEquals(pubRsaKey.modulus, extractedPubSpec.modulus)
        assertEquals(pubKey.publicExponent, extractedPubSpec.publicExponent)

        val extractedX509 = kf.getKeySpec(decodedPub, x509Spec.javaClass)
        assertContentEquals(pubKey.encoded, extractedX509.encoded)

        val extractedPkcs8 = kf.getKeySpec(decodedPriv, pkcs8Spec.javaClass)
        assertContentEquals(privKey.encoded, extractedPkcs8.encoded)

        val translatedPub = kf.translateKey(decodedPub) as RSAPublicKey
        assertEquals(pubRsaKey.modulus, (translatedPub as RSAKey).modulus)
    }
}

