package android.content.pm

import android.os.Bundle

open class PackageItemInfo {
    @JvmField var name: String? = null
    @JvmField var packageName: String? = null
    @JvmField var labelRes: Int = 0
    @JvmField var nonLocalizedLabel: CharSequence? = null
    @JvmField var icon: Int = 0
    @JvmField var banner: Int = 0
    @JvmField var logo: Int = 0
    @JvmField var metaData: Bundle? = null

    constructor()

    constructor(orig: PackageItemInfo?) {
        if (orig != null) {
            name = orig.name
            packageName = orig.packageName
            labelRes = orig.labelRes
            nonLocalizedLabel = orig.nonLocalizedLabel
            icon = orig.icon
            banner = orig.banner
            logo = orig.logo
            metaData = orig.metaData?.let { Bundle(it) }
        }
    }
}
