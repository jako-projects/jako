package com.bennyhuo.jako.gradle

import java.io.File
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class GenerateAndroidResourcesTest {

    @Test
    fun testDeterministicRGenerationMatchesRuntimeFormula() {
        val tempRoot = File(System.getProperty("java.io.tmpdir") ?: "/tmp", "jako_r_gen_test_${System.nanoTime()}")
        val resDir = File(tempRoot, "res")
        val rawDir = File(resDir, "raw")
        val valuesDir = File(resDir, "values")
        rawDir.mkdirs()
        valuesDir.mkdirs()

        try {
            File(rawDir, "welcome_banner.txt").writeText("Hello")
            File(valuesDir, "strings.xml").writeText(
                """
                <?xml version="1.0" encoding="utf-8"?>
                <resources>
                    <string name="app_title">Sample</string>
                    <integer name="max_threads">8</integer>
                    <bool name="enable_cache">true</bool>
                    <color name="brand">#FF001122</color>
                    <dimen name="padding">16dp</dimen>
                    <string-array name="layers">
                        <item>a</item>
                    </string-array>
                </resources>
                """.trimIndent()
            )

            val scanned = RClassGenerator.scanResources(resDir)
            val expectedTitleId = 0x7f020000 or RClassGenerator.computeEntryHash16("app_title")
            val expectedRawId = 0x7f010000 or RClassGenerator.computeEntryHash16("welcome_banner")
            val expectedDimenId = 0x7f060000 or RClassGenerator.computeEntryHash16("padding")

            assertEquals(expectedTitleId, scanned["string"]?.get("app_title"))
            assertEquals(expectedRawId, scanned["raw"]?.get("welcome_banner"))
            assertEquals(expectedDimenId, scanned["dimen"]?.get("padding"))

            val code = RClassGenerator.generateRFileContent("com.example.demo", resDir)
            assertTrue(code.contains("package com.example.demo"))
            assertTrue(code.contains("public object R {"))
            assertTrue(code.contains("public const val app_title: Int = 0x${expectedTitleId.toUInt().toString(16).padStart(8, '0')}"))
            assertTrue(code.contains("public const val welcome_banner: Int = 0x${expectedRawId.toUInt().toString(16).padStart(8, '0')}"))
        } finally {
            tempRoot.deleteRecursively()
        }
    }

    @Test
    fun testJakoGradlePluginCompilerPluginConfiguration() {
        val plugin = JakoGradlePlugin()
        assertEquals("com.bennyhuo.jako.compiler", plugin.getCompilerPluginId())
        val artifact = plugin.getPluginArtifact()
        assertEquals("com.bennyhuo.jako", artifact.groupId)
        assertEquals("jako-compiler-plugin", artifact.artifactId)
        assertTrue(!artifact.version.isNullOrBlank())
    }
}
