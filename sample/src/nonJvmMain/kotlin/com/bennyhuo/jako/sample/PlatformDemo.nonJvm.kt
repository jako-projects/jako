package com.bennyhuo.jako.sample

actual fun runPlatformDemos() {
    val android = AndroidDemo.run()
    println("[5] Android Framework, JakoApplication & Resources Demo:")
    println("    app title         = ${android.appTitle}")
    println("    status banner     = ${android.formattedStatusBanner}")
    println("    asset engine      = ${android.assetEngine}")
    println("    raw banner        = ${android.rawBannerFirstLine}")
    println("    padding (16dp)    = ${android.contentPaddingPx}px")
    println("    shared prefs      = ${android.savedPreferenceChannel}")
    println("    free disk bytes   = ${android.availableDiskBytes}")
    println("    endpoint uri      = ${android.endpointUrl}")
    println("    active features   = ${android.activeFeatures}")
    println("    viewport area     = ${android.viewportArea}")
    println("    parsed tokens     = ${android.parsedTokenCount}")
    println("    @Parcelize result = ${android.parcelizedSummary}")
}

