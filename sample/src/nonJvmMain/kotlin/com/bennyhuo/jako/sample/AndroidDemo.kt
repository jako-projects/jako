package com.bennyhuo.jako.sample

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Rect
import android.net.Uri
import android.os.Environment
import android.os.Parcel
import android.os.Parcelable
import android.os.StatFs
import android.text.TextUtils
import android.util.DisplayMetrics
import android.util.JsonReader
import android.util.SparseArray
import com.bennyhuo.jako.JakoApplication
import com.bennyhuo.jako.init
import java.io.File
import java.io.StringReader
import java.util.UUID
import kotlin.io.writeText
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.parcelableCreator
import org.json.JSONArray
import org.json.JSONObject

object AndroidDemo {

    @Parcelize
    data class SessionSnapshot(
        val sessionId: Long,
        val user: String,
        val tags: List<String>,
        val metrics: Map<String, Int>,
        val endpoint: Uri,
        val viewport: Rect
    ) : Parcelable {
        @IgnoredOnParcel
        var localCacheHit: Boolean = false
    }

    data class AndroidReport(
        val endpointUrl: String,
        val activeFeatures: String,
        val viewportArea: Int,
        val parsedTokenCount: Int,
        val appTitle: String,
        val formattedStatusBanner: String,
        val assetEngine: String,
        val rawBannerFirstLine: String,
        val contentPaddingPx: Int,
        val savedPreferenceChannel: String,
        val availableDiskBytes: Long,
        val parcelizedSummary: String
    )

    private fun resolveSampleDir(subPath: String): File {
        val candidates = mutableListOf(
            File(subPath),
            File("sample", subPath),
            File("../sample", subPath)
        )
        var dir: File? = File(".").absoluteFile
        repeat(8) {
            if (dir != null) {
                candidates.add(File(File(dir, "sample"), subPath))
                candidates.add(File(dir, subPath))
                dir = dir.parentFile
            }
        }
        return candidates.firstOrNull { it.isDirectory } ?: File(subPath)
    }

    private fun ensureSampleDirs(tempRoot: File): Pair<File, File> {
        val diskAssets = resolveSampleDir("assets")
        val diskRes = resolveSampleDir("res")
        if (diskAssets.isDirectory && diskRes.isDirectory) {
            return diskAssets to diskRes
        }
        val fallbackAssets = File(tempRoot, "assets")
        val fallbackRes = File(tempRoot, "res")
        fallbackAssets.mkdirs()
        File(fallbackAssets, "app_manifest.json").writeText(
            """{"appId":"com.bennyhuo.jako.sample","engine":"jako-multiplatform","modules":["jako-stdlib","jako-android"]}"""
        )
        val valuesDir = File(fallbackRes, "values").apply { mkdirs() }
        File(valuesDir, "strings.xml").writeText(
            """
            <?xml version="1.0" encoding="utf-8"?>
            <resources>
                <string name="app_title">Jako Cross-Platform Sample</string>
                <string name="status_banner">Running %1${'$'}s v%2${'$'}s on %3${'$'}d DPI</string>
                <integer name="max_worker_threads">8</integer>
                <bool name="enable_disk_cache">true</bool>
                <color name="primary_brand">#FF1E88E5</color>
                <dimen name="content_padding">16dp</dimen>
                <string-array name="supported_layers">
                    <item>jako-stdlib</item>
                    <item>jako-android</item>
                </string-array>
            </resources>
            """.trimIndent()
        )
        val rawDir = File(fallbackRes, "raw").apply { mkdirs() }
        File(rawDir, "welcome_banner.txt").writeText(
            "Welcome to Jako Multiplatform!\nAssets and Android XML resources are loaded seamlessly on PC & Native.\n"
        )
        return fallbackAssets to fallbackRes
    }

    fun run(): AndroidReport {
        val tempRoot = File(System.getProperty("java.io.tmpdir") ?: "/tmp", "jako-sample-android-${UUID.randomUUID()}")
        try {
            val (resolvedAssetsDir, resolvedResDir) = ensureSampleDirs(tempRoot)
            // 1. Initialize JakoApplication with sample assets/, res/, metadata, and custom directories
            val app = JakoApplication.init {
                packageName = "com.bennyhuo.jako.sample"
                versionName = "1.2.0"
                versionCode = 120
                dataDir = File(tempRoot, "data")
                cacheDir = File(tempRoot, "cache")
                externalStorageDir = File(tempRoot, "sdcard")
                assetsDir = resolvedAssetsDir
                resourcesDir = resolvedResDir
                densityDpi = DisplayMetrics.DENSITY_XHIGH // 320 dpi (2.0x density)
                putMetaData("release_channel", "multiplatform-desktop")
                registerStringResource(0x7f010099, "custom_tag", "JakoRuntimeReady")
            }

            // 2. AssetManager: read assets/app_manifest.json
            val manifestJsonText = app.getAssets().open("app_manifest.json").bufferedReader().use { it.readText() }
            val manifestObj = JSONObject(manifestJsonText)
            val assetEngine = manifestObj.getString("engine")

            // 3. Resources: load XML values (res/values/strings.xml) & raw files (res/raw/welcome_banner.txt) via generated R class
            val res = app.getResources()
            val pkg = app.getPackageName()

            val appTitle = app.getString(R.string.app_title)
            val pkgInfo = app.getPackageManager().getPackageInfo(pkg, PackageManager.GET_META_DATA)
            val statusBanner = app.getString(
                R.string.status_banner,
                appTitle,
                pkgInfo.versionName,
                res.getDisplayMetrics().densityDpi
            )
            // 16dp @ 320dpi (2.0x) = 32px
            val contentPaddingPx = res.getDimensionPixelSize(R.dimen.content_padding)

            val rawBannerFirstLine = res.openRawResource(R.raw.welcome_banner).bufferedReader().use { reader ->
                reader.readLine() ?: ""
            }

            // 4. SharedPreferences & ApplicationInfo metaData
            val appInfo = app.getPackageManager().getApplicationInfo(pkg, PackageManager.GET_META_DATA)
            val channelFromMeta = appInfo.metaData?.getString("release_channel") ?: "unknown"
            val prefs = app.getSharedPreferences("sample_settings", Context.MODE_PRIVATE)
            prefs.edit()
                .putString("channel", channelFromMeta)
                .putString("runtime_tag", app.getString(0x7f010099))
                .commit()
            val savedChannel = "${prefs.getString("channel", "")}:${prefs.getString("runtime_tag", "")}"

            // 5. Environment & StatFs disk statistics
            val extDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val statFs = StatFs(extDownloads.absolutePath)
            val availableDiskBytes = statFs.getAvailableBytes()

            // 6. android.net.Uri & Uri.Builder
            val uri = Uri.parse("https://api.example.com/v1")
                .buildUpon()
                .appendPath("projects")
                .appendPath("jako")
                .appendQueryParameter("target", "native")
                .fragment("overview")
                .build()

            // 7. org.json.JSONObject & JSONArray + SparseArray & TextUtils
            val payload = JSONObject()
                .put("module", "jako-android")
                .put("features", JSONArray().put("Uri").put("JSONObject").put("SparseArray"))

            val sparse = SparseArray<String>()
            val featuresArray = payload.getJSONArray("features")
            for (i in 0 until featuresArray.length()) {
                sparse.put((i + 1) * 10, featuresArray.getString(i))
            }
            val labels = ArrayList<String>(sparse.size())
            for (i in 0 until sparse.size()) {
                labels.add("${sparse.keyAt(i)}:${sparse.valueAt(i)}")
            }
            val joinedFeatures = TextUtils.join(", ", labels)

            // 8. android.graphics.Rect geometry & JsonReader streaming parser
            val viewport = Rect(0, 0, 1920, 1080)
            viewport.inset(160, 40)
            val area = viewport.width() * viewport.height()

            var tokenCount = 0
            JsonReader(StringReader(payload.toString())).use { reader ->
                reader.beginObject()
                while (reader.hasNext()) {
                    when (reader.nextName()) {
                        "module" -> {
                            reader.nextString()
                            tokenCount++
                        }
                        "features" -> {
                            reader.beginArray()
                            while (reader.hasNext()) {
                                reader.nextString()
                                tokenCount++
                            }
                            reader.endArray()
                        }
                        else -> reader.skipValue()
                    }
                }
                reader.endObject()
            }

            // 9. Cross-platform @Parcelize binary Parcel serialization
            val originalSnapshot = SessionSnapshot(
                sessionId = 2026L,
                user = "jako-kmp",
                tags = listOf("K2", "FIR", "IR"),
                metrics = mapOf("dpi" to res.getDisplayMetrics().densityDpi, "padding" to contentPaddingPx),
                endpoint = uri,
                viewport = viewport
            ).apply {
                localCacheHit = true
            }
            val parcel = Parcel.obtain()
            val restoredSnapshot = try {
                parcel.writeTypedObject(originalSnapshot, 0)
                val bytes = parcel.marshall()
                val readParcel = Parcel.obtain()
                try {
                    readParcel.unmarshall(bytes, 0, bytes.size)
                    readParcel.setDataPosition(0)
                    readParcel.readTypedObject(parcelableCreator<SessionSnapshot>())!!
                } finally {
                    readParcel.recycle()
                }
            } finally {
                parcel.recycle()
            }
            val parcelizedSummary = "${restoredSnapshot.user}#${restoredSnapshot.sessionId} " +
                "tags=${restoredSnapshot.tags} dpi=${restoredSnapshot.metrics["dpi"]} " +
                "ignoredCache=${restoredSnapshot.localCacheHit}"

            return AndroidReport(
                endpointUrl = uri.toString(),
                activeFeatures = joinedFeatures,
                viewportArea = area,
                parsedTokenCount = tokenCount,
                appTitle = appTitle,
                formattedStatusBanner = statusBanner,
                assetEngine = assetEngine,
                rawBannerFirstLine = rawBannerFirstLine,
                contentPaddingPx = contentPaddingPx,
                savedPreferenceChannel = savedChannel,
                availableDiskBytes = availableDiskBytes,
                parcelizedSummary = parcelizedSummary
            )
        } finally {
            JakoApplication.reset()
            tempRoot.deleteRecursively()
        }
    }
}
