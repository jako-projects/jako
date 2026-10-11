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

    val utility = UtilityDemo.run()
    println("[3] Math, Time, Crypto & Optional Demo:")
    println("    invoice           = ${utility.formattedInvoice}")
    println("    sha256 (base64)   = ${utility.sha256Base64}")
    println("    optional value    = ${utility.optionalValue}")

    val echo = NetworkDemo.runEcho("Hello from jako socket!")
    println("[4] Loopback TCP Socket Demo:")
    println("    server response   = $echo")

    println("=== All demos completed successfully! ===")
}
