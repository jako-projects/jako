@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package java.text

/**
 * Java `Normalizer` is a non-instantiable utility class.
 * Declaring it as an `object` lets Kotlin Native resolve `Normalizer.Form` static imports.
 */
expect object Normalizer {
    enum class Form {
        NFD,
        NFC,
        NFKD,
        NFKC,
    }

    fun normalize(src: CharSequence, form: Form): String
    fun isNormalized(src: CharSequence, form: Form): Boolean
}
