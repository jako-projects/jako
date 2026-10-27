package android.os

import android.content.platformAppCacheDir
import android.content.platformAppDataDir
import android.content.platformDefaultExternalStorageDir

actual open class Build actual constructor() {
    actual class VERSION actual constructor() {
        actual companion object {
            actual val INCREMENTAL: String = "unknown"
            actual val RELEASE: String = "10"
            actual val SDK: String = "29"
            actual val SDK_INT: Int = 29
            actual val CODENAME: String = "REL"
        }
    }

    actual class VERSION_CODES actual constructor() {
        actual companion object {
            actual val CUR_DEVELOPMENT: Int = 10000
            actual val BASE: Int = 1
            actual val BASE_1_1: Int = 2
            actual val CUPCAKE: Int = 3
            actual val DONUT: Int = 4
            actual val ECLAIR: Int = 5
            actual val ECLAIR_0_1: Int = 6
            actual val ECLAIR_MR1: Int = 7
            actual val FROYO: Int = 8
            actual val GINGERBREAD: Int = 9
            actual val GINGERBREAD_MR1: Int = 10
            actual val HONEYCOMB: Int = 11
            actual val HONEYCOMB_MR1: Int = 12
            actual val HONEYCOMB_MR2: Int = 13
            actual val ICE_CREAM_SANDWICH: Int = 14
            actual val ICE_CREAM_SANDWICH_MR1: Int = 15
            actual val JELLY_BEAN: Int = 16
            actual val JELLY_BEAN_MR1: Int = 17
            actual val JELLY_BEAN_MR2: Int = 18
            actual val KITKAT: Int = 19
            actual val KITKAT_WATCH: Int = 20
            actual val LOLLIPOP: Int = 21
            actual val LOLLIPOP_MR1: Int = 22
            actual val M: Int = 23
            actual val N: Int = 24
            actual val N_MR1: Int = 25
            actual val O: Int = 26
            actual val O_MR1: Int = 27
            actual val P: Int = 28
            actual val Q: Int = 29
        }
    }

    actual companion object {
        actual val UNKNOWN: String = "unknown"
        actual val ID: String = UNKNOWN
        actual val DISPLAY: String = UNKNOWN
        actual val PRODUCT: String = UNKNOWN
        actual val DEVICE: String = UNKNOWN
        actual val BOARD: String = UNKNOWN
        actual val MANUFACTURER: String = UNKNOWN
        actual val BRAND: String = UNKNOWN
        actual val MODEL: String = UNKNOWN
        actual val BOOTLOADER: String = UNKNOWN
        actual val HARDWARE: String = UNKNOWN
        actual val TYPE: String = "user"
        actual val TAGS: String = UNKNOWN
        actual val FINGERPRINT: String = UNKNOWN
        actual val USER: String = UNKNOWN
        actual val HOST: String = UNKNOWN
    }
}
