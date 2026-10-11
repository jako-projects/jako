package jako.test.text

import java.text.Normalizer
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NormalizerTest {
    @Test
    fun normalizeAsciiIsIdentity() {
        assertEquals("example.com", Normalizer.normalize("example.com", Normalizer.Form.NFC))
        assertTrue(Normalizer.isNormalized("example.com", Normalizer.Form.NFC))
    }
}
