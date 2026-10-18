package jako.test.text

import java.text.Normalizer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class NormalizerTest {
    @Test
    fun normalizeAsciiIsIdentity() {
        assertEquals("example.com", Normalizer.normalize("example.com", Normalizer.Form.NFC))
        assertTrue(Normalizer.isNormalized("example.com", Normalizer.Form.NFC))
    }

    @Test
    fun testCanonicalDecompositionAndComposition() {
        val precomposed = "caf\u00E9" // café
        val decomposed = "cafe\u0301"

        assertEquals(decomposed, Normalizer.normalize(precomposed, Normalizer.Form.NFD))
        assertEquals(precomposed, Normalizer.normalize(decomposed, Normalizer.Form.NFC))

        assertTrue(Normalizer.isNormalized(precomposed, Normalizer.Form.NFC))
        assertFalse(Normalizer.isNormalized(precomposed, Normalizer.Form.NFD))
        assertTrue(Normalizer.isNormalized(decomposed, Normalizer.Form.NFD))
        assertFalse(Normalizer.isNormalized(decomposed, Normalizer.Form.NFC))

        // Canonical combining class ordering: acute (230) + cedilla (202) -> cedilla (202) + acute (230) -> ḉ (U+1E09)
        val unordered = "c\u0301\u0327"
        val orderedNfd = "c\u0327\u0301"
        assertEquals(orderedNfd, Normalizer.normalize(unordered, Normalizer.Form.NFD))
        assertEquals("\u1E09", Normalizer.normalize(unordered, Normalizer.Form.NFC))
    }

    @Test
    fun testHangulNormalization() {
        val syllable = "\uD55C\uAE00" // 한글
        val jamo = "\u1112\u1161\u11AB\u1100\u1173\u11AF"

        assertEquals(jamo, Normalizer.normalize(syllable, Normalizer.Form.NFD))
        assertEquals(syllable, Normalizer.normalize(jamo, Normalizer.Form.NFC))
        assertTrue(Normalizer.isNormalized(syllable, Normalizer.Form.NFC))
        assertTrue(Normalizer.isNormalized(jamo, Normalizer.Form.NFD))
    }

    @Test
    fun testCompatibilityNormalization() {
        // Fullwidth ASCII, ligatures, superscripts
        val input = "\uFF21\uFF22\uFF23\uFB01\u00B2" // ＡＢＣﬁ²
        assertEquals("ABCfi2", Normalizer.normalize(input, Normalizer.Form.NFKC))
        assertEquals("ABCfi2", Normalizer.normalize(input, Normalizer.Form.NFKD))
        assertFalse(Normalizer.isNormalized(input, Normalizer.Form.NFKC))
        assertTrue(Normalizer.isNormalized("ABCfi2", Normalizer.Form.NFKC))
    }
}
