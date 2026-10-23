package android.content.pm

import android.os.Bundle

actual open class PackageItemInfo {
    actual var name: String? = null
    actual var packageName: String? = null
    actual var labelRes: Int = 0
    actual var nonLocalizedLabel: CharSequence? = null
    actual var icon: Int = 0
    actual var banner: Int = 0
    actual var logo: Int = 0
    actual var metaData: Bundle? = null

    actual constructor()

    actual constructor(orig: PackageItemInfo?) {
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
