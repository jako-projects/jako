package com.bennyhuo.jako.sample

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SampleTest {

    @Test
    fun testStorageDemo() {
        val report = StorageDemo.run()
        assertEquals("jako-sample", report.appName)
        assertEquals(listOf("README.txt", "conf/app.properties"), report.discoveredFiles)
        assertTrue(report.compressedSize > 0)
        assertEquals("Hello from jako KMP storage!", report.decompressedContent)
    }

    @Test
    fun testConcurrencyDemo() {
        val report = ConcurrencyDemo.run(workerCount = 4, tasksPerWorker = 10)
        assertEquals(40, report.totalProcessed)
        assertEquals(4, report.bucketCount)
        assertEquals((1..40).sum(), report.snapshotSum)
        assertEquals((1..40).sum(), report.synchronizedLedgerSum)
        assertTrue(report.monitorLockVerified)
    }

    @Test
    fun testUtilityDemo() {
        val report = UtilityDemo.run()
        assertEquals("[2026-10-10 12:30:00] Total: $64.64", report.formattedInvoice)
        assertEquals("FWgFTH/q91EQHSgbgGeJ2vagKzQTuNyzIABZQHUyPBc=", report.sha256Base64)
        assertEquals("verified", report.optionalValue)
    }

    @Test
    @Suppress("UNSUPPORTED_WEB_API")
    fun testNetworkDemo() {
        try {
            val reply = NetworkDemo.runEcho("ping-jako")
            assertEquals("ECHO:ping-jako", reply)
        } catch (e: UnsupportedOperationException) {
            assertTrue(e.message!!.contains("is not supported on target 'WasmWasi'"))
        }
    }

    @Test
    fun testAndroidDemo() {
        val report = AndroidDemo.run()
        assertEquals("https://api.example.com/v1/projects/jako?target=native#overview", report.endpointUrl)
        assertEquals("10:Uri, 20:JSONObject, 30:SparseArray", report.activeFeatures)
        assertEquals(1600 * 1000, report.viewportArea)
        assertEquals(4, report.parsedTokenCount)
        assertEquals("Jako Cross-Platform Sample", report.appTitle)
        assertEquals("Running Jako Cross-Platform Sample v1.2.0 on 320 DPI", report.formattedStatusBanner)
        assertEquals("jako-multiplatform", report.assetEngine)
        assertEquals("Welcome to Jako Multiplatform!", report.rawBannerFirstLine)
        assertEquals(32, report.contentPaddingPx)
        assertEquals("multiplatform-desktop:JakoRuntimeReady", report.savedPreferenceChannel)
        assertTrue(report.availableDiskBytes > 0L)
        assertEquals("jako-kmp#2026 tags=[K2, FIR, IR] dpi=320 ignoredCache=false", report.parcelizedSummary)
    }
}
