package com.bennyhuo.jako.sample

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AndroidSampleTest {

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

