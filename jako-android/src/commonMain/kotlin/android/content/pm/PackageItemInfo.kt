@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.content.pm

import android.os.Bundle

/**
 * Base class containing information common to all package items held by the Package Manager.
 *
 * [Nullability Decision]:
 * - Fields `name`, `packageName`, `nonLocalizedLabel`, and `metaData` are nullable on unpopulated instances.
 */
expect open class PackageItemInfo {
    var name: String?
    var packageName: String?
    var labelRes: Int
    var nonLocalizedLabel: CharSequence?
    var icon: Int
    var banner: Int
    var logo: Int
    var metaData: Bundle?

    constructor()
    constructor(orig: PackageItemInfo?)
}
