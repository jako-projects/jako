package jako.android.test.runtime

import android.app.Application
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Color
import android.os.Environment
import android.os.Parcel
import android.os.StatFs
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import com.bennyhuo.jako.JakoApplication
import com.bennyhuo.jako.init
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class JakoAndroidRuntimeAndResourcesTest {

    @AfterTest
    fun tearDown() {
        JakoApplication.reset()
    }

    @Test
    fun testUninitializedDefaultsAndApplicationLifecycle() {
        JakoApplication.reset()
        assertFalse(JakoApplication.isInitialized)

        val defaultApp = JakoApplication.application
        val defaultCtx = JakoApplication.applicationContext
        assertNotNull(defaultApp)
        assertEquals(defaultApp, defaultCtx)
        assertTrue(defaultCtx.getPackageName().isNotEmpty())
        assertTrue(defaultCtx.getFilesDir().exists())
        assertTrue(defaultCtx.getCacheDir().exists())
    }

    @Test
    fun testJakoApplicationInitDirectoriesPackageManagerAndExternalStorage() {
        val tempRoot = File(System.getProperty("java.io.tmpdir") ?: "/tmp", "jako_runtime_test_${System.nanoTime()}")
        val customDataDir = File(tempRoot, "data")
        val customCacheDir = File(tempRoot, "cache")
        val customExtDir = File(tempRoot, "sdcard")
        val customAssetsDir = File(tempRoot, "assets")
        val customResDir = File(tempRoot, "res")

        try {
            val app = JakoApplication.init {
                packageName = "com.example.desktopapp"
                versionName = "2.4.0"
                versionCode = 240
                longVersionCode = 240L
                minSdkVersion = 24
                targetSdkVersion = 29
                dataDir = customDataDir
                cacheDir = customCacheDir
                externalStorageDir = customExtDir
                assetsDir = customAssetsDir
                resourcesDir = customResDir
                densityDpi = DisplayMetrics.DENSITY_XHIGH
                screenWidthPx = 2560
                screenHeightPx = 1440
                locale = Locale.US
                logMinPriority = Log.DEBUG
                putMetaData("api_endpoint", "https://api.example.com")
                putMetaData("retry_limit", 3)
                putMetaData("feature_x", true)
            }

            assertTrue(JakoApplication.isInitialized)
            assertEquals("com.example.desktopapp", app.getPackageName())
            assertEquals("com.example.desktopapp", Application.getProcessName())
            assertEquals(customDataDir.canonicalPath, app.getDataDir().canonicalPath)
            assertEquals(File(customDataDir, "files").canonicalPath, app.getFilesDir().canonicalPath)
            assertEquals(customCacheDir.canonicalPath, app.getCacheDir().canonicalPath)

            // Verify Log.isLoggable reflects configured logMinPriority
            assertTrue(Log.isLoggable("TestTag", Log.DEBUG))
            assertTrue(Log.isLoggable("TestTag", Log.INFO))
            assertFalse(Log.isLoggable("TestTag", Log.VERBOSE))

            // Verify PackageManager & ApplicationInfo & PackageInfo
            val pm = app.getPackageManager()
            val pkgInfo = pm.getPackageInfo("com.example.desktopapp", PackageManager.GET_META_DATA)
            assertEquals("com.example.desktopapp", pkgInfo.packageName)
            assertEquals("2.4.0", pkgInfo.versionName)
            assertEquals(240, pkgInfo.versionCode)
            assertEquals(240L, pkgInfo.getLongVersionCode())

            val appInfo = pm.getApplicationInfo("com.example.desktopapp", PackageManager.GET_META_DATA)
            assertEquals("com.example.desktopapp", appInfo.packageName)
            assertEquals(24, appInfo.minSdkVersion)
            assertEquals(29, appInfo.targetSdkVersion)
            assertEquals(customDataDir.canonicalPath, File(appInfo.dataDir!!).canonicalPath)
            assertEquals("https://api.example.com", appInfo.metaData?.getString("api_endpoint"))
            assertEquals(3, appInfo.metaData?.getInt("retry_limit"))
            assertEquals(true, appInfo.metaData?.getBoolean("feature_x"))
            assertEquals(PackageManager.PERMISSION_GRANTED, app.checkSelfPermission("android.permission.INTERNET"))

            assertFailsWith<PackageManager.NameNotFoundException> {
                pm.getPackageInfo("com.unknown.pkg", 0)
            }

            // Verify Environment & Context external storage directories
            assertEquals(Environment.MEDIA_MOUNTED, Environment.getExternalStorageState())
            assertEquals(customExtDir.canonicalPath, Environment.getExternalStorageDirectory().canonicalPath)

            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            assertEquals(File(customExtDir, "Downloads").canonicalPath, downloadsDir.canonicalPath)
            assertTrue(downloadsDir.exists() && downloadsDir.isDirectory)

            val extFilesDir = app.getExternalFilesDir( null)
            assertNotNull(extFilesDir)
            assertEquals(
                File(customExtDir, "Android/data/com.example.desktopapp/files").canonicalPath,
                extFilesDir.canonicalPath
            )

            val extPicturesDir = app.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            assertNotNull(extPicturesDir)
            assertEquals(
                File(customExtDir, "Android/data/com.example.desktopapp/files/Pictures").canonicalPath,
                extPicturesDir.canonicalPath
            )

            val extCacheDir = app.getExternalCacheDir()
            assertNotNull(extCacheDir)
            assertEquals(
                File(customExtDir, "Android/data/com.example.desktopapp/cache").canonicalPath,
                extCacheDir.canonicalPath
            )

            val obbDir = app.getObbDir()
            assertEquals(
                File(customExtDir, "Android/obb/com.example.desktopapp").canonicalPath,
                obbDir.canonicalPath
            )
        } finally {
            tempRoot.deleteRecursively()
        }
    }

    @Test
    fun testAssetManagerAndResourcesFromDiskAndConfig() {
        val tempRoot = File(System.getProperty("java.io.tmpdir") ?: "/tmp", "jako_assets_res_test_${System.nanoTime()}")
        val assetsRoot = File(tempRoot, "assets")
        val resRoot = File(tempRoot, "res")

        try {
            // Prepare assets directory tree
            val shadersDir = File(assetsRoot, "shaders")
            shadersDir.mkdirs()
            FileOutputStream(File(assetsRoot, "app_config.json")).use {
                it.write("""{"env":"production"}""".encodeToByteArray())
            }
            FileOutputStream(File(shadersDir, "vertex.glsl")).use {
                it.write("void main() {}".encodeToByteArray())
            }
            FileOutputStream(File(shadersDir, "fragment.glsl")).use {
                it.write("void frag() {}".encodeToByteArray())
            }

            // Prepare res/raw and res/values/strings.xml
            val rawDir = File(resRoot, "raw")
            val valuesDir = File(resRoot, "values")
            rawDir.mkdirs()
            valuesDir.mkdirs()

            val licenseFile = File(rawDir, "license.txt")
            FileOutputStream(licenseFile).use {
                it.write("MIT License".encodeToByteArray())
            }

            val stringsXml = File(valuesDir, "strings.xml")
            val xmlContent = """
                <?xml version="1.0" encoding="utf-8"?>
                <resources>
                    <string name="app_title">Jako Desktop</string>
                    <string name="welcome_fmt">Hello, %1${'$'}s (%2${'$'}d)!</string>
                    <integer name="max_connections">16</integer>
                    <bool name="enable_tls">true</bool>
                    <color name="brand_color">#FF336699</color>
                    <dimen name="card_padding">12dp</dimen>
                    <string-array name="supported_targets">
                        <item>jvm</item>
                        <item>linuxX64</item>
                        <item>macosArm64</item>
                        <item>mingwX64</item>
                    </string-array>
                    <integer-array name="retry_delays">
                        <item>100</item>
                        <item>250</item>
                        <item>500</item>
                    </integer-array>
                </resources>
            """.trimIndent()
            FileOutputStream(stringsXml).use {
                it.write(xmlContent.encodeToByteArray())
            }

            val customIdGreeting = 0x7f020099
            val ctx = JakoApplication.init {
                packageName = "com.example.resapp"
                assetsDir = assetsRoot
                resourcesDir = resRoot
                densityDpi = DisplayMetrics.DENSITY_XHIGH // 320 dpi -> density = 2.0f
                registerStringResource(customIdGreeting, "custom_greeting", "Registered Greeting")
            }

            // 1. Test AssetManager
            val rootAssets = ctx.getAssets().list("")
            assertNotNull(rootAssets)
            assertTrue(rootAssets.contains("app_config.json"))
            assertTrue(rootAssets.contains("shaders"))

            val shaderList = ctx.getAssets().list("shaders")
            assertContentEquals(arrayOf("fragment.glsl", "vertex.glsl"), shaderList)

            val configJson = ctx.getAssets().open("app_config.json").use { input ->
                val buf = ByteArray(64)
                val n = input.read(buf)
                buf.decodeToString(0, n)
            }
            assertEquals("""{"env":"production"}""", configJson)

            assertFailsWith<FileNotFoundException> {
                ctx.getAssets().open("../escape.txt")
            }
            assertFailsWith<FileNotFoundException> {
                ctx.getAssets().open("missing_file.txt")
            }

            // 2. Test Resources loaded from res/values/strings.xml & res/raw/license.txt
            val res = ctx.getResources()
            assertEquals(2.0f, res.getDisplayMetrics().density)

            // Programmatically registered resource
            assertEquals("Registered Greeting", ctx.getString(customIdGreeting))
            assertEquals(customIdGreeting, res.getIdentifier("custom_greeting", "string", ctx.getPackageName()))
            assertEquals("com.example.resapp:string/custom_greeting", res.getResourceName(customIdGreeting))
            assertEquals("string", res.getResourceTypeName(customIdGreeting))
            assertEquals("custom_greeting", res.getResourceEntryName(customIdGreeting))

            // XML-discovered resources
            val titleId = res.getIdentifier("app_title", "string", ctx.getPackageName())
            assertTrue(titleId != 0)
            assertEquals("Jako Desktop", ctx.getString(titleId))

            val welcomeId = res.getIdentifier("welcome_fmt", "string", ctx.getPackageName())
            assertEquals("Hello, Developer (42)!", ctx.getString(welcomeId, "Developer", 42))

            val maxConnId = res.getIdentifier("max_connections", "integer", ctx.getPackageName())
            assertEquals(16, res.getInteger(maxConnId))

            val tlsId = res.getIdentifier("enable_tls", "bool", ctx.getPackageName())
            assertTrue(res.getBoolean(tlsId))

            val colorId = res.getIdentifier("brand_color", "color", ctx.getPackageName())
            assertEquals(Color.parseColor("#FF336699"), ctx.getColor(colorId))

            val dimenId = res.getIdentifier("card_padding", "dimen", ctx.getPackageName())
            // 12dp at density 2.0 = 24.0px
            assertEquals(24.0f, res.getDimension(dimenId))
            assertEquals(24, res.getDimensionPixelSize(dimenId))

            val targetsArrayId = res.getIdentifier("supported_targets", "array", ctx.getPackageName())
            assertContentEquals(arrayOf("jvm", "linuxX64", "macosArm64", "mingwX64"), res.getStringArray(targetsArrayId))

            val delaysArrayId = res.getIdentifier("retry_delays", "array", ctx.getPackageName())
            assertContentEquals(intArrayOf(100, 250, 500), res.getIntArray(delaysArrayId))

            // Raw resource
            val rawLicenseId = res.getIdentifier("license", "raw", ctx.getPackageName())
            assertTrue(rawLicenseId != 0)
            val tv = TypedValue()
            val licenseText = res.openRawResource(rawLicenseId, tv).use { input ->
                val buf = ByteArray(64)
                val n = input.read(buf)
                buf.decodeToString(0, n)
            }
            assertEquals("MIT License", licenseText)
            assertEquals(rawLicenseId, tv.resourceId)

            assertFailsWith<Resources.NotFoundException> {
                res.getString(0x7f999999)
            }
        } finally {
            tempRoot.deleteRecursively()
        }
    }

    @Test
    fun testStatFsConfigurationAndTypedValue() {
        val tmpDir = File(System.getProperty("java.io.tmpdir") ?: "/tmp")
        val statFs = StatFs(tmpDir.absolutePath)
        assertTrue(statFs.getBlockSizeLong() > 0L)
        assertTrue(statFs.getTotalBytes() > 0L)
        assertTrue(statFs.getAvailableBytes() >= 0L)
        assertTrue(statFs.getFreeBytes() >= statFs.getAvailableBytes())
        assertTrue(statFs.getBlockCountLong() > 0L)

        assertFailsWith<IllegalArgumentException> {
            StatFs("/non_existent_path_jako_${System.nanoTime()}")
        }

        // TypedValue dimension conversion
        val metrics = DisplayMetrics().apply {
            density = 3.0f
            scaledDensity = 3.0f
            xdpi = 480f
            ydpi = 480f
        }
        assertEquals(30.0f, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 10.0f, metrics))
        assertEquals(45.0f, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 15.0f, metrics))
        assertEquals(12.0f, TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, 12.0f, metrics))

        // Configuration parcel & diff
        val c1 = Configuration()
        c1.fontScale = 1.25f
        c1.densityDpi = 320
        c1.orientation = Configuration.ORIENTATION_PORTRAIT

        val parcel = Parcel.obtain()
        c1.writeToParcel(parcel, 0)
        val bytes = parcel.marshall()
        parcel.recycle()

        val parcel2 = Parcel.obtain()
        parcel2.unmarshall(bytes, 0, bytes.size)
        val c2 = Configuration()
        c2.readFromParcel(parcel2)
        parcel2.recycle()

        assertEquals(1.25f, c2.fontScale)
        assertEquals(320, c2.densityDpi)
        assertEquals(Configuration.ORIENTATION_PORTRAIT, c2.orientation)
    }
}
