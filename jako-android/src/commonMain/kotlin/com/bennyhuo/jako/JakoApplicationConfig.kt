package com.bennyhuo.jako

import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import java.io.File
import java.util.Locale

internal var platformDefaultPackageNameProvider: (() -> String)? = null

internal fun resolveDefaultPackageName(): String {
    val envPkg = System.getenv("JAKO_PACKAGE_NAME")?.trim()
    if (!envPkg.isNullOrEmpty()) {
        val cleaned = envPkg.replace('/', '_').replace('\\', '_').replace(':', '_')
        if (cleaned.isNotEmpty() && cleaned != "." && cleaned != "..") {
            return cleaned
        }
    }
    platformDefaultPackageNameProvider?.invoke()?.let { return it }
    return "jako.android.app"
}

/**
 * Configuration model for initializing `jako-android` on PC / Native platforms.
 *
 * All properties have sensible OS-aware defaults when left unconfigured:
 * - [packageName]: defaults to environment variable `JAKO_PACKAGE_NAME`, bundle identifier / executable name, or `"jako.android.app"`.
 * - [dataDir] / [filesDir] / [cacheDir]: defaults to standard OS user app-data / cache directories
 *   (`~/Library/Application Support/<packageName>` on macOS/iOS, `$XDG_DATA_HOME/<packageName>` on Linux,
 *   `%LOCALAPPDATA%\<packageName>` on Windows).
 * - [assetsDir]: defaults to `JAKO_ASSETS_DIR`, `<bundle>/Resources/assets`, `<executableDir>/assets`, or `./assets`.
 * - [resourcesDir]: defaults to `JAKO_RESOURCES_DIR`, `<bundle>/Resources/res`, `<executableDir>/res`, or `./res`.
 */
class JakoApplicationConfig() {
    internal var explicitPackageName: String? = null

    var packageName: String
        get() = explicitPackageName ?: resolveDefaultPackageName()
        set(value) {
            explicitPackageName = value.trim().takeIf { it.isNotEmpty() }
        }

    var versionName: String = "1.0.0"
    var versionCode: Int = 1
    var longVersionCode: Long = 1L
    var targetSdkVersion: Int = 29
    var minSdkVersion: Int = 21
    var dataDir: File? = null
    var filesDir: File? = null
    var cacheDir: File? = null
    var externalStorageDir: File? = null
    var assetsDir: File? = null
    var resourcesDir: File? = null
    var nativeLibraryDir: File? = null
    var logMinPriority: Int = Log.INFO
    var densityDpi: Int = DisplayMetrics.DENSITY_DEFAULT
    var screenWidthPx: Int = 1920
    var screenHeightPx: Int = 1080
    var locale: Locale = Locale.getDefault()

    val metaDataBundle = Bundle()
    val stringResources = LinkedHashMap<Int, String>()
    val stringArrayResources = LinkedHashMap<Int, Array<String>>()
    val intArrayResources = LinkedHashMap<Int, IntArray>()
    val integerResources = LinkedHashMap<Int, Int>()
    val booleanResources = LinkedHashMap<Int, Boolean>()
    val colorResources = LinkedHashMap<Int, Int>()
    val dimensionResources = LinkedHashMap<Int, Float>()
    val rawResources = LinkedHashMap<Int, File>()
    val resourceNameToId = LinkedHashMap<String, Int>()
    val resourceIdToName = LinkedHashMap<Int, Pair<String, String>>()

    private fun registerName(id: Int, type: String, name: String?) {
        if (!name.isNullOrBlank()) {
            val clean = name.trim()
            resourceNameToId["$type/$clean"] = id
            resourceIdToName[id] = type to clean
        }
    }

    fun putMetaData(key: String, value: String): JakoApplicationConfig {
        metaDataBundle.putString(key, value)
        return this
    }

    fun putMetaData(key: String, value: Int): JakoApplicationConfig {
        metaDataBundle.putInt(key, value)
        return this
    }

    fun putMetaData(key: String, value: Boolean): JakoApplicationConfig {
        metaDataBundle.putBoolean(key, value)
        return this
    }

    fun putMetaData(key: String, value: Float): JakoApplicationConfig {
        metaDataBundle.putFloat(key, value)
        return this
    }

    fun registerStringResource(id: Int, value: String): JakoApplicationConfig =
        registerStringResource(id, "", value)

    fun registerStringResource(id: Int, name: String, value: String): JakoApplicationConfig {
        stringResources[id] = value
        registerName(id, "string", name)
        return this
    }

    fun registerStringArrayResource(id: Int, values: Array<String>): JakoApplicationConfig =
        registerStringArrayResource(id, "", values)

    fun registerStringArrayResource(id: Int, name: String, values: Array<String>): JakoApplicationConfig {
        stringArrayResources[id] = values.copyOf()
        registerName(id, "array", name)
        return this
    }

    fun registerIntArrayResource(id: Int, values: IntArray): JakoApplicationConfig =
        registerIntArrayResource(id, "", values)

    fun registerIntArrayResource(id: Int, name: String, values: IntArray): JakoApplicationConfig {
        intArrayResources[id] = values.copyOf()
        registerName(id, "array", name)
        return this
    }

    fun registerIntegerResource(id: Int, value: Int): JakoApplicationConfig =
        registerIntegerResource(id, "", value)

    fun registerIntegerResource(id: Int, name: String, value: Int): JakoApplicationConfig {
        integerResources[id] = value
        registerName(id, "integer", name)
        return this
    }

    fun registerBooleanResource(id: Int, value: Boolean): JakoApplicationConfig =
        registerBooleanResource(id, "", value)

    fun registerBooleanResource(id: Int, name: String, value: Boolean): JakoApplicationConfig {
        booleanResources[id] = value
        registerName(id, "bool", name)
        return this
    }

    fun registerColorResource(id: Int, color: Int): JakoApplicationConfig =
        registerColorResource(id, "", color)

    fun registerColorResource(id: Int, name: String, color: Int): JakoApplicationConfig {
        colorResources[id] = color
        registerName(id, "color", name)
        return this
    }

    fun registerDimensionResource(id: Int, pixelValue: Float): JakoApplicationConfig =
        registerDimensionResource(id, "", pixelValue)

    fun registerDimensionResource(id: Int, name: String, pixelValue: Float): JakoApplicationConfig {
        dimensionResources[id] = pixelValue
        registerName(id, "dimen", name)
        return this
    }

    fun registerRawResource(id: Int, file: File): JakoApplicationConfig =
        registerRawResource(id, file.nameWithoutExtension, file)

    fun registerRawResource(id: Int, name: String, file: File): JakoApplicationConfig {
        rawResources[id] = file
        registerName(id, "raw", name)
        return this
    }
}
