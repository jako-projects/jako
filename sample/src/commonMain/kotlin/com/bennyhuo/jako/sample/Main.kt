package com.bennyhuo.jako.sample

fun main() {
    println("=== jako KMP Sample ===")

    val storage = StorageDemo.run()
    println("[1] Storage & GZIP Demo:")
    println("    app.name          = ${storage.appName}")
    println("    discovered files  = ${storage.discoveredFiles}")
    println("    compressed bytes  = ${storage.compressedSize}")
    println("    decompressed text = ${storage.decompressedContent}")

    val concurrency = ConcurrencyDemo.run()
    println("[2] Concurrency & Locks Demo:")
    println("    total processed   = ${concurrency.totalProcessed}")
    println("    worker buckets    = ${concurrency.bucketCount}")
    println("    accumulator sum   = ${concurrency.snapshotSum}")
    println("    @Synchronized sum = ${concurrency.synchronizedLedgerSum} (holdsLock=${concurrency.monitorLockVerified})")

    val utility = UtilityDemo.run()
    println("[3] Math, Time, Crypto & Optional Demo:")
    println("    invoice           = ${utility.formattedInvoice}")
    println("    sha256 (base64)   = ${utility.sha256Base64}")
    println("    optional value    = ${utility.optionalValue}")

    val echo = NetworkDemo.runEcho("Hello from jako socket!")
    println("[4] Loopback TCP Socket Demo:")
    println("    server response   = $echo")

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

    println("=== All demos completed successfully! ===")
}
