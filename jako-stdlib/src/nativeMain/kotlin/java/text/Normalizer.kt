package java.text

actual object Normalizer {
    actual enum class Form {
        NFD,
        NFC,
        NFKD,
        NFKC,
    }

    /**
     * Native has no ICU/Java Normalizer. Identity is enough for compile and ASCII hostnames;
     * non-ASCII IDN still needs a real NFC implementation later.
     */
    actual fun normalize(src: CharSequence, form: Form): String = src.toString()

    actual fun isNormalized(src: CharSequence, form: Form): Boolean = true
}
