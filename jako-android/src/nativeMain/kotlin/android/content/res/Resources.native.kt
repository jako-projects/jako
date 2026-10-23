package android.content.res

import android.content.platformDefaultAssetsDir
import android.content.platformDefaultResourcesDir
import android.graphics.Color
import android.util.DisplayMetrics
import android.util.TypedValue
import android.util.Xml
import com.bennyhuo.jako.JakoApplication
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.util.Locale
import org.xmlpull.v1.XmlPullParser
import kotlin.math.roundToInt

actual open class Resources actual constructor(
    assets: AssetManager?,
    metrics: DisplayMetrics?,
    config: Configuration?
) {
    actual open class NotFoundException : RuntimeException {
        actual constructor() : super()
        actual constructor(name: String?) : super(name)
        actual constructor(name: String?, cause: Exception?) : super(name, cause)
    }

    private val mAssets: AssetManager = assets ?: AssetManager()
    private val mMetrics: DisplayMetrics = DisplayMetrics().apply {
        if (metrics != null) setTo(metrics) else setToDefaults()
    }
    private val mConfig: Configuration = Configuration().apply {
        if (config != null) setTo(config) else setToDefaults()
    }

    private val diskStrings = LinkedHashMap<Int, String>()
    private val diskStringArrays = LinkedHashMap<Int, Array<String>>()
    private val diskIntArrays = LinkedHashMap<Int, IntArray>()
    private val diskIntegers = LinkedHashMap<Int, Int>()
    private val diskBooleans = LinkedHashMap<Int, Boolean>()
    private val diskColors = LinkedHashMap<Int, Int>()
    private val diskDimensions = LinkedHashMap<Int, Float>()
    private val diskRawFiles = LinkedHashMap<Int, File>()
    private val diskNameToId = LinkedHashMap<String, Int>()
    private val diskIdToName = LinkedHashMap<Int, Pair<String, String>>()
    private var loadedResDirPath: String? = null

    private fun ensureDiskResourcesLoaded() {
        val resDir = JakoApplication.config.resourcesDir ?: platformDefaultResourcesDir()
        val currentPath = resDir.absolutePath
        if (loadedResDirPath == currentPath) return
        loadedResDirPath = currentPath

        diskStrings.clear()
        diskStringArrays.clear()
        diskIntArrays.clear()
        diskIntegers.clear()
        diskBooleans.clear()
        diskColors.clear()
        diskDimensions.clear()
        diskRawFiles.clear()
        diskNameToId.clear()
        diskIdToName.clear()

        if (!resDir.exists() || !resDir.isDirectory) return

        fun computeEntryHash16(name: String): Int {
            var h = 0x811c9dc5.toInt()
            for (i in 0 until name.length) {
                h = (h xor name[i].code) * 0x01000193
            }
            val folded = ((h ushr 16) xor (h and 0xFFFF)) and 0xFFFF
            return if (folded == 0) 1 else folded
        }

        fun allocateId(typeCode: Int, typeName: String, entryName: String): Int {
            val normalizedName = entryName.replace('.', '_').replace('-', '_')
            val key = "$typeName/$entryName"
            val normKey = "$typeName/$normalizedName"
            val existing = JakoApplication.config.resourceNameToId[key]
                ?: JakoApplication.config.resourceNameToId[normKey]
                ?: diskNameToId[key]
                ?: diskNameToId[normKey]
            if (existing != null) return existing

            val prefix = 0x7f000000 or ((typeCode and 0xFF) shl 16)
            var low16 = computeEntryHash16(normalizedName)
            var id = prefix or low16
            while (diskIdToName.containsKey(id) || JakoApplication.config.resourceIdToName.containsKey(id)) {
                low16 = (low16 % 0xFFFF) + 1
                id = prefix or low16
            }
            diskNameToId[key] = id
            diskNameToId[normKey] = id
            diskIdToName[id] = typeName to entryName
            return id
        }

        // 1. Scan res/raw/*
        val rawDir = File(resDir, "raw")
        if (rawDir.isDirectory) {
            val files = rawDir.listFiles() ?: emptyArray()
            for (f in files.sortedBy { it.name }) {
                if (f.isFile) {
                    val entryName = f.nameWithoutExtension.ifEmpty { f.name }
                    val id = allocateId(0x01, "raw", entryName)
                    diskRawFiles[id] = f
                }
            }
        }

        // 2. Scan res/values/*.xml
        val valuesDir = File(resDir, "values")
        if (valuesDir.isDirectory) {
            val xmlFiles = valuesDir.listFiles()?.filter { it.isFile && it.name.endsWith(".xml", ignoreCase = true) }
                ?.sortedBy { it.name } ?: emptyList()
            for (xmlFile in xmlFiles) {
                parseValuesXml(xmlFile, ::allocateId)
            }
        }
    }

    private fun parseValuesXml(
        xmlFile: File,
        allocateId: (Int, String, String) -> Int
    ) {
        try {
            FileInputStream(xmlFile).use { fis ->
                val parser = Xml.newPullParser()
                parser.setInput(fis, "UTF-8")
                var event = parser.getEventType()
                while (event != XmlPullParser.END_DOCUMENT) {
                    if (event == XmlPullParser.START_TAG) {
                        val tag = parser.getName()
                        val name = parser.getAttributeValue(null, "name")?.trim()
                        if (!name.isNullOrEmpty()) {
                            when (tag) {
                                "string" -> {
                                    val text = parser.nextText()
                                    val id = allocateId(0x02, "string", name)
                                    diskStrings[id] = text
                                }
                                "integer" -> {
                                    val text = parser.nextText().trim()
                                    val v = text.toIntOrNull()
                                    if (v != null) {
                                        val id = allocateId(0x03, "integer", name)
                                        diskIntegers[id] = v
                                    }
                                }
                                "bool" -> {
                                    val text = parser.nextText().trim()
                                    val v = text.equals("true", ignoreCase = true)
                                    val id = allocateId(0x04, "bool", name)
                                    diskBooleans[id] = v
                                }
                                "color" -> {
                                    val text = parser.nextText().trim()
                                    try {
                                        val c = Color.parseColor(text)
                                        val id = allocateId(0x05, "color", name)
                                        diskColors[id] = c
                                    } catch (_: Throwable) {
                                    }
                                }
                                "dimen" -> {
                                    val text = parser.nextText().trim()
                                    val px = parseDimensionToPixels(text, mMetrics)
                                    if (px != null) {
                                        val id = allocateId(0x06, "dimen", name)
                                        diskDimensions[id] = px
                                    }
                                }
                                "string-array" -> {
                                    val items = ArrayList<String>()
                                    val depth = parser.getDepth()
                                    var inner = parser.next()
                                    while (inner != XmlPullParser.END_DOCUMENT &&
                                        !(inner == XmlPullParser.END_TAG && parser.getDepth() == depth)
                                    ) {
                                        if (inner == XmlPullParser.START_TAG && parser.getName() == "item") {
                                            items.add(parser.nextText())
                                        }
                                        inner = parser.next()
                                    }
                                    val id = allocateId(0x07, "array", name)
                                    diskStringArrays[id] = items.toTypedArray()
                                }
                                "integer-array" -> {
                                    val items = ArrayList<Int>()
                                    val depth = parser.getDepth()
                                    var inner = parser.next()
                                    while (inner != XmlPullParser.END_DOCUMENT &&
                                        !(inner == XmlPullParser.END_TAG && parser.getDepth() == depth)
                                    ) {
                                        if (inner == XmlPullParser.START_TAG && parser.getName() == "item") {
                                            parser.nextText().trim().toIntOrNull()?.let { items.add(it) }
                                        }
                                        inner = parser.next()
                                    }
                                    val id = allocateId(0x07, "array", name)
                                    diskIntArrays[id] = items.toIntArray()
                                }
                            }
                        }
                    }
                    event = parser.next()
                }
            }
        } catch (_: Throwable) {
        }
    }

    private fun parseDimensionToPixels(raw: String, metrics: DisplayMetrics): Float? {
        val s = raw.trim().lowercase(Locale.ROOT)
        return when {
            s.endsWith("dip") -> s.removeSuffix("dip").trim().toFloatOrNull()?.let {
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, it, metrics)
            }
            s.endsWith("dp") -> s.removeSuffix("dp").trim().toFloatOrNull()?.let {
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, it, metrics)
            }
            s.endsWith("sp") -> s.removeSuffix("sp").trim().toFloatOrNull()?.let {
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, it, metrics)
            }
            s.endsWith("px") -> s.removeSuffix("px").trim().toFloatOrNull()
            s.endsWith("pt") -> s.removeSuffix("pt").trim().toFloatOrNull()?.let {
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PT, it, metrics)
            }
            s.endsWith("in") -> s.removeSuffix("in").trim().toFloatOrNull()?.let {
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_IN, it, metrics)
            }
            s.endsWith("mm") -> s.removeSuffix("mm").trim().toFloatOrNull()?.let {
                TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_MM, it, metrics)
            }
            else -> s.toFloatOrNull()
        }
    }

    actual val assets: AssetManager get() = getAssets()
    actual val displayMetrics: DisplayMetrics get() = getDisplayMetrics()
    actual val configuration: Configuration get() = getConfiguration()

    actual fun getAssets(): AssetManager = mAssets

    actual open fun getDisplayMetrics(): DisplayMetrics = mMetrics

    actual open fun getConfiguration(): Configuration = mConfig

    actual open fun getString(id: Int): String {
        ensureDiskResourcesLoaded()
        return JakoApplication.config.stringResources[id]
            ?: diskStrings[id]
            ?: throw NotFoundException("String resource ID #0x${id.toUInt().toString(16)}")
    }

    actual open fun getString(id: Int, vararg formatArgs: Any?): String {
        val raw = getString(id)
        val loc = mConfig.locale ?: Locale.getDefault()
        return raw.format(loc, *formatArgs)
    }

    actual open fun getText(id: Int): CharSequence = getString(id)

    actual open fun getText(id: Int, def: CharSequence?): CharSequence? {
        return try {
            getString(id)
        } catch (_: NotFoundException) {
            def
        }
    }

    actual open fun getStringArray(id: Int): Array<String> {
        ensureDiskResourcesLoaded()
        val arr = JakoApplication.config.stringArrayResources[id]
            ?: diskStringArrays[id]
            ?: throw NotFoundException("String array resource ID #0x${id.toUInt().toString(16)}")
        return arr.copyOf()
    }

    actual open fun getIntArray(id: Int): IntArray {
        ensureDiskResourcesLoaded()
        val arr = JakoApplication.config.intArrayResources[id]
            ?: diskIntArrays[id]
            ?: throw NotFoundException("Int array resource ID #0x${id.toUInt().toString(16)}")
        return arr.copyOf()
    }

    actual open fun getInteger(id: Int): Int {
        ensureDiskResourcesLoaded()
        return JakoApplication.config.integerResources[id]
            ?: diskIntegers[id]
            ?: throw NotFoundException("Integer resource ID #0x${id.toUInt().toString(16)}")
    }

    actual open fun getBoolean(id: Int): Boolean {
        ensureDiskResourcesLoaded()
        return JakoApplication.config.booleanResources[id]
            ?: diskBooleans[id]
            ?: throw NotFoundException("Boolean resource ID #0x${id.toUInt().toString(16)}")
    }

    actual open fun getColor(id: Int): Int {
        ensureDiskResourcesLoaded()
        return JakoApplication.config.colorResources[id]
            ?: diskColors[id]
            ?: throw NotFoundException("Color resource ID #0x${id.toUInt().toString(16)}")
    }

    actual open fun getDimension(id: Int): Float {
        ensureDiskResourcesLoaded()
        return JakoApplication.config.dimensionResources[id]
            ?: diskDimensions[id]
            ?: throw NotFoundException("Dimension resource ID #0x${id.toUInt().toString(16)}")
    }

    actual open fun getDimensionPixelOffset(id: Int): Int {
        return getDimension(id).toInt()
    }

    actual open fun getDimensionPixelSize(id: Int): Int {
        val f = getDimension(id)
        val res = (if (f >= 0f) f + 0.5f else f - 0.5f).toInt()
        if (res != 0) return res
        if (f == 0f) return 0
        return if (f > 0f) 1 else -1
    }

    actual open fun openRawResource(id: Int): InputStream = openRawResource(id, null)

    actual open fun openRawResource(id: Int, value: TypedValue?): InputStream {
        ensureDiskResourcesLoaded()
        val file = JakoApplication.config.rawResources[id]
            ?: diskRawFiles[id]
            ?: throw NotFoundException("Raw resource ID #0x${id.toUInt().toString(16)}")
        if (!file.exists() || !file.isFile) {
            throw NotFoundException("Raw resource file does not exist: ${file.path}")
        }
        if (value != null) {
            value.resourceId = id
            value.type = TypedValue.TYPE_STRING
            value.string = file.path
        }
        return FileInputStream(file)
    }

    actual open fun getValue(id: Int, outValue: TypedValue?, resolveRefs: Boolean) {
        ensureDiskResourcesLoaded()
        val out = outValue ?: return
        out.resourceId = id
        val cfg = JakoApplication.config
        when {
            cfg.stringResources.containsKey(id) || diskStrings.containsKey(id) -> {
                out.type = TypedValue.TYPE_STRING
                out.string = cfg.stringResources[id] ?: diskStrings[id]
            }
            cfg.integerResources.containsKey(id) || diskIntegers.containsKey(id) -> {
                out.type = TypedValue.TYPE_INT_DEC
                out.data = cfg.integerResources[id] ?: diskIntegers[id]!!
            }
            cfg.booleanResources.containsKey(id) || diskBooleans.containsKey(id) -> {
                out.type = TypedValue.TYPE_INT_BOOLEAN
                out.data = if ((cfg.booleanResources[id] ?: diskBooleans[id]) == true) 1 else 0
            }
            cfg.colorResources.containsKey(id) || diskColors.containsKey(id) -> {
                out.type = TypedValue.TYPE_INT_COLOR_ARGB8
                out.data = cfg.colorResources[id] ?: diskColors[id]!!
            }
            cfg.dimensionResources.containsKey(id) || diskDimensions.containsKey(id) -> {
                out.type = TypedValue.TYPE_DIMENSION
                val f = cfg.dimensionResources[id] ?: diskDimensions[id]!!
                out.data = f.toRawBits()
            }
            cfg.rawResources.containsKey(id) || diskRawFiles.containsKey(id) -> {
                out.type = TypedValue.TYPE_STRING
                out.string = (cfg.rawResources[id] ?: diskRawFiles[id])?.path
            }
            else -> throw NotFoundException("Resource ID #0x${id.toUInt().toString(16)}")
        }
    }

    actual open fun getIdentifier(name: String?, defType: String?, defPackage: String?): Int {
        if (name.isNullOrBlank()) return 0
        ensureDiskResourcesLoaded()
        var entry = name.trim()
        var type = defType?.trim() ?: ""
        if (':' in entry) {
            entry = entry.substringAfter(':')
        }
        if ('/' in entry) {
            type = entry.substringBefore('/')
            entry = entry.substringAfter('/')
        }
        if (type.isEmpty()) return 0
        val key = "$type/$entry"
        return JakoApplication.config.resourceNameToId[key] ?: diskNameToId[key] ?: 0
    }

    private fun lookupResourceInfo(resid: Int): Pair<String, String> {
        ensureDiskResourcesLoaded()
        return JakoApplication.config.resourceIdToName[resid]
            ?: diskIdToName[resid]
            ?: throw NotFoundException("Unable to find resource ID #0x${resid.toUInt().toString(16)}")
    }

    actual open fun getResourceName(resid: Int): String {
        val (type, entry) = lookupResourceInfo(resid)
        return "${JakoApplication.config.packageName}:$type/$entry"
    }

    actual open fun getResourcePackageName(resid: Int): String {
        lookupResourceInfo(resid)
        return JakoApplication.config.packageName
    }

    actual open fun getResourceTypeName(resid: Int): String {
        return lookupResourceInfo(resid).first
    }

    actual open fun getResourceEntryName(resid: Int): String {
        return lookupResourceInfo(resid).second
    }

    actual open fun updateConfiguration(config: Configuration?, metrics: DisplayMetrics?) {
        if (config != null) mConfig.setTo(config)
        if (metrics != null) mMetrics.setTo(metrics)
    }

    actual companion object {
        actual fun getSystem(): Resources = Resources(AssetManager(), DisplayMetrics(), Configuration())
    }
}
