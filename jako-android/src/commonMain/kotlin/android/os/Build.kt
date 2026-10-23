@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.os

/**
 * Information about the current build, extracted from system properties.
 */
expect open class Build() {
    class VERSION() {
        companion object {
            val INCREMENTAL: String
            val RELEASE: String
            val SDK: String
            val SDK_INT: Int
            val CODENAME: String
        }
    }

    class VERSION_CODES() {
        companion object {
            val CUR_DEVELOPMENT: Int
            val BASE: Int
            val BASE_1_1: Int
            val CUPCAKE: Int
            val DONUT: Int
            val ECLAIR: Int
            val ECLAIR_0_1: Int
            val ECLAIR_MR1: Int
            val FROYO: Int
            val GINGERBREAD: Int
            val GINGERBREAD_MR1: Int
            val HONEYCOMB: Int
            val HONEYCOMB_MR1: Int
            val HONEYCOMB_MR2: Int
            val ICE_CREAM_SANDWICH: Int
            val ICE_CREAM_SANDWICH_MR1: Int
            val JELLY_BEAN: Int
            val JELLY_BEAN_MR1: Int
            val JELLY_BEAN_MR2: Int
            val KITKAT: Int
            val KITKAT_WATCH: Int
            val LOLLIPOP: Int
            val LOLLIPOP_MR1: Int
            val M: Int
            val N: Int
            val N_MR1: Int
            val O: Int
            val O_MR1: Int
            val P: Int
            val Q: Int
        }
    }

    companion object {
        val UNKNOWN: String
        val ID: String
        val DISPLAY: String
        val PRODUCT: String
        val DEVICE: String
        val BOARD: String
        val MANUFACTURER: String
        val BRAND: String
        val MODEL: String
        val BOOTLOADER: String
        val HARDWARE: String
        val TYPE: String
        val TAGS: String
        val FINGERPRINT: String
        val USER: String
        val HOST: String
    }
}
