package android.os

open class Build() {
    class VERSION() {
        companion object {
            @JvmField
            val INCREMENTAL: String = "unknown"
            @JvmField
            val RELEASE: String = "10"
            @JvmField
            val SDK: String = "29"
            @JvmField
            val SDK_INT: Int = 29
            @JvmField
            val CODENAME: String = "REL"
        }
    }

    class VERSION_CODES() {
        companion object {
            const val CUR_DEVELOPMENT: Int = 10000
            const val BASE: Int = 1
            const val BASE_1_1: Int = 2
            const val CUPCAKE: Int = 3
            const val DONUT: Int = 4
            const val ECLAIR: Int = 5
            const val ECLAIR_0_1: Int = 6
            const val ECLAIR_MR1: Int = 7
            const val FROYO: Int = 8
            const val GINGERBREAD: Int = 9
            const val GINGERBREAD_MR1: Int = 10
            const val HONEYCOMB: Int = 11
            const val HONEYCOMB_MR1: Int = 12
            const val HONEYCOMB_MR2: Int = 13
            const val ICE_CREAM_SANDWICH: Int = 14
            const val ICE_CREAM_SANDWICH_MR1: Int = 15
            const val JELLY_BEAN: Int = 16
            const val JELLY_BEAN_MR1: Int = 17
            const val JELLY_BEAN_MR2: Int = 18
            const val KITKAT: Int = 19
            const val KITKAT_WATCH: Int = 20
            const val LOLLIPOP: Int = 21
            const val LOLLIPOP_MR1: Int = 22
            const val M: Int = 23
            const val N: Int = 24
            const val N_MR1: Int = 25
            const val O: Int = 26
            const val O_MR1: Int = 27
            const val P: Int = 28
            const val Q: Int = 29
        }
    }

    companion object {
        const val UNKNOWN: String = "unknown"
        @JvmField
        val ID: String = UNKNOWN
        @JvmField
        val DISPLAY: String = UNKNOWN
        @JvmField
        val PRODUCT: String = UNKNOWN
        @JvmField
        val DEVICE: String = UNKNOWN
        @JvmField
        val BOARD: String = UNKNOWN
        @JvmField
        val MANUFACTURER: String = UNKNOWN
        @JvmField
        val BRAND: String = UNKNOWN
        @JvmField
        val MODEL: String = UNKNOWN
        @JvmField
        val BOOTLOADER: String = UNKNOWN
        @JvmField
        val HARDWARE: String = UNKNOWN
        @JvmField
        val TYPE: String = "user"
        @JvmField
        val TAGS: String = UNKNOWN
        @JvmField
        val FINGERPRINT: String = UNKNOWN
        @JvmField
        val USER: String = UNKNOWN
        @JvmField
        val HOST: String = UNKNOWN
    }
}
