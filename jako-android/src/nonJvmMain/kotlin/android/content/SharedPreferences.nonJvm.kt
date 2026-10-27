package android.content

import android.util.Xml
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Collections
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import org.xmlpull.v1.XmlPullParser

/**
 * [Nullability Decision] SharedPreferences getters for String and Set<String> accept and return nullable values,
 * while primitive getters return non-null primitives and Editor methods return non-null Editor for chaining,
 * matching AOSP android.content.SharedPreferences.
 */
actual interface SharedPreferences {
    actual val all: Map<String, *>

    actual fun getAll(): Map<String, *>
    actual fun getString(key: String?, defValue: String?): String?
    actual fun getStringSet(key: String?, defValues: Set<String>?): Set<String>?
    actual fun getInt(key: String?, defValue: Int): Int
    actual fun getLong(key: String?, defValue: Long): Long
    actual fun getFloat(key: String?, defValue: Float): Float
    actual fun getBoolean(key: String?, defValue: Boolean): Boolean
    actual fun contains(key: String?): Boolean
    actual fun edit(): Editor
    actual fun registerOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener?)
    actual fun unregisterOnSharedPreferenceChangeListener(listener: OnSharedPreferenceChangeListener?)

    actual interface Editor {
        actual fun putString(key: String?, value: String?): Editor
        actual fun putStringSet(key: String?, values: Set<String>?): Editor
        actual fun putInt(key: String?, value: Int): Editor
        actual fun putLong(key: String?, value: Long): Editor
        actual fun putFloat(key: String?, value: Float): Editor
        actual fun putBoolean(key: String?, value: Boolean): Editor
        actual fun remove(key: String?): Editor
        actual fun clear(): Editor
        actual fun commit(): Boolean
        actual fun apply()
    }

    actual fun interface OnSharedPreferenceChangeListener {
        actual fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences, key: String?)
    }
}

internal object SharedPreferencesRegistry {
    private val lock = ReentrantLock()
    private val instances = LinkedHashMap<String, SharedPreferencesImpl>()

    fun getOrCreate(file: File): SharedPreferencesImpl = lock.withLock {
        val path = file.absolutePath
        instances.getOrPut(path) { SharedPreferencesImpl(file) }
    }

    fun delete(file: File): Boolean = lock.withLock {
        val path = file.absolutePath
        instances.remove(path)
        val bakFile = makeBackupFile(file)
        bakFile.delete()
        if (file.exists()) file.delete() else true
    }

    fun move(sourceFile: File, targetFile: File): Boolean = lock.withLock {
        val srcPath = sourceFile.absolutePath
        val dstPath = targetFile.absolutePath
        instances.remove(srcPath)
        instances.remove(dstPath)

        val srcBak = makeBackupFile(sourceFile)
        if (srcBak.exists()) {
            sourceFile.delete()
            srcBak.renameTo(sourceFile)
        }
        if (!sourceFile.exists()) return@withLock true

        val targetParent = targetFile.parentFile
        if (targetParent != null && !targetParent.exists()) {
            targetParent.mkdirs()
        }
        makeBackupFile(targetFile).delete()
        if (targetFile.exists()) {
            targetFile.delete()
        }
        if (sourceFile.renameTo(targetFile)) {
            true
        } else {
            try {
                FileInputStream(sourceFile).use { input ->
                    FileOutputStream(targetFile).use { output ->
                        val buf = ByteArray(8192)
                        while (true) {
                            val n = input.read(buf)
                            if (n <= 0) break
                            output.write(buf, 0, n)
                        }
                        output.flush()
                    }
                }
                sourceFile.delete()
                true
            } catch (_: Throwable) {
                false
            }
        }
    }

    internal fun makeBackupFile(prefsFile: File): File = File("${prefsFile.path}.bak")
}

/**
 * Native implementation of AOSP `android.app.SharedPreferencesImpl` backed by XML files
 * with atomic `.bak` crash-recovery semantics and decoupled memory/disk locks.
 */
internal class SharedPreferencesImpl(private val file: File) : SharedPreferences {
    private val backupFile: File = SharedPreferencesRegistry.makeBackupFile(file)
    private val lock = ReentrantLock()
    private val writingToDiskLock = ReentrantLock()
    private val map = LinkedHashMap<String, Any?>()
    private val listeners = LinkedHashSet<SharedPreferences.OnSharedPreferenceChangeListener>()
    private var currentMemoryStateGeneration: Long = 0L
    private var diskStateGeneration: Long = 0L

    init {
        loadFromDisk()
    }

    private fun loadFromDisk() {
        lock.withLock {
            if (backupFile.exists()) {
                file.delete()
                backupFile.renameTo(file)
            }
            if (!file.exists() || file.length() == 0L) return
            try {
                FileInputStream(file).use { fis ->
                    val parser = Xml.newPullParser()
                    parser.setInput(fis, "UTF-8")
                    var eventType = parser.getEventType()
                    while (eventType != XmlPullParser.END_DOCUMENT) {
                        if (eventType == XmlPullParser.START_TAG) {
                            when (parser.getName()) {
                                "map" -> {}
                                "string" -> {
                                    val key = parser.getAttributeValue(null, "name")
                                    val value = parser.nextText()
                                    if (key != null) map[key] = value
                                }
                                "int" -> {
                                    val key = parser.getAttributeValue(null, "name")
                                    val v = parser.getAttributeValue(null, "value")?.toIntOrNull()
                                    if (key != null && v != null) map[key] = v
                                }
                                "long" -> {
                                    val key = parser.getAttributeValue(null, "name")
                                    val v = parser.getAttributeValue(null, "value")?.toLongOrNull()
                                    if (key != null && v != null) map[key] = v
                                }
                                "float" -> {
                                    val key = parser.getAttributeValue(null, "name")
                                    val v = parser.getAttributeValue(null, "value")?.toFloatOrNull()
                                    if (key != null && v != null) map[key] = v
                                }
                                "boolean" -> {
                                    val key = parser.getAttributeValue(null, "name")
                                    val v = parser.getAttributeValue(null, "value")?.toBooleanStrictOrNull()
                                    if (key != null && v != null) map[key] = v
                                }
                                "set" -> {
                                    val key = parser.getAttributeValue(null, "name")
                                    val setValues = LinkedHashSet<String>()
                                    val setDepth = parser.getDepth()
                                    var innerEvent = parser.next()
                                    while (innerEvent != XmlPullParser.END_DOCUMENT &&
                                        !(innerEvent == XmlPullParser.END_TAG && parser.getDepth() == setDepth && parser.getName() == "set")
                                    ) {
                                        if (innerEvent == XmlPullParser.START_TAG && parser.getName() == "string") {
                                            setValues.add(parser.nextText())
                                        }
                                        innerEvent = parser.next()
                                    }
                                    if (key != null) {
                                        map[key] = Collections.unmodifiableSet(setValues)
                                    }
                                }
                                "null" -> {
                                    val key = parser.getAttributeValue(null, "name")
                                    if (key != null) map.remove(key)
                                }
                                else -> {}
                            }
                        }
                        eventType = parser.next()
                    }
                }
            } catch (_: Throwable) {
                // Keep empty or partially loaded state if file is corrupted, matching AOSP SharedPreferencesImpl
            }
        }
    }

    override val all: Map<String, *> get() = getAll()

    override fun getAll(): Map<String, *> = lock.withLock {
        Collections.unmodifiableMap(LinkedHashMap(map))
    }

    override fun getString(key: String?, defValue: String?): String? = lock.withLock {
        if (key == null) return defValue
        (map[key] as? String) ?: defValue
    }

    @Suppress("UNCHECKED_CAST")
    override fun getStringSet(key: String?, defValues: Set<String>?): Set<String>? = lock.withLock {
        if (key == null) return defValues
        val v = map[key] as? Set<String> ?: return defValues
        LinkedHashSet(v)
    }

    override fun getInt(key: String?, defValue: Int): Int = lock.withLock {
        if (key == null) return defValue
        (map[key] as? Int) ?: defValue
    }

    override fun getLong(key: String?, defValue: Long): Long = lock.withLock {
        if (key == null) return defValue
        (map[key] as? Long) ?: defValue
    }

    override fun getFloat(key: String?, defValue: Float): Float = lock.withLock {
        if (key == null) return defValue
        (map[key] as? Float) ?: defValue
    }

    override fun getBoolean(key: String?, defValue: Boolean): Boolean = lock.withLock {
        if (key == null) return defValue
        (map[key] as? Boolean) ?: defValue
    }

    override fun contains(key: String?): Boolean = lock.withLock {
        key != null && map.containsKey(key)
    }

    override fun edit(): SharedPreferences.Editor = EditorImpl()

    override fun registerOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?
    ) {
        if (listener == null) return
        lock.withLock {
            listeners.add(listener)
        }
    }

    override fun unregisterOnSharedPreferenceChangeListener(
        listener: SharedPreferences.OnSharedPreferenceChangeListener?
    ) {
        if (listener == null) return
        lock.withLock {
            listeners.remove(listener)
        }
    }

    private class MemoryCommitResult(
        val memoryStateGeneration: Long,
        val mapToWriteToDisk: Map<String, Any?>,
        val keysModified: List<String>,
        val listeners: List<SharedPreferences.OnSharedPreferenceChangeListener>
    )

    private fun writeToFile(mcr: MemoryCommitResult): Boolean = writingToDiskLock.withLock {
        if (diskStateGeneration >= mcr.memoryStateGeneration && file.exists()) {
            return@withLock true
        }
        val parent = file.parentFile
        if (parent != null && !parent.exists()) {
            parent.mkdirs()
        }
        if (file.exists()) {
            if (!backupFile.exists()) {
                if (!file.renameTo(backupFile)) {
                    return@withLock false
                }
            } else {
                file.delete()
            }
        }
        return@withLock try {
            FileOutputStream(file).use { fos ->
                val serializer = Xml.newSerializer()
                serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true)
                serializer.setOutput(fos, "UTF-8")
                serializer.startDocument("UTF-8", true)
                serializer.startTag(null, "map")
                for ((k, v) in mcr.mapToWriteToDisk) {
                    when (v) {
                        is String -> {
                            serializer.startTag(null, "string")
                            serializer.attribute(null, "name", k)
                            serializer.text(v)
                            serializer.endTag(null, "string")
                        }
                        is Int -> {
                            serializer.startTag(null, "int")
                            serializer.attribute(null, "name", k)
                            serializer.attribute(null, "value", v.toString())
                            serializer.endTag(null, "int")
                        }
                        is Long -> {
                            serializer.startTag(null, "long")
                            serializer.attribute(null, "name", k)
                            serializer.attribute(null, "value", v.toString())
                            serializer.endTag(null, "long")
                        }
                        is Float -> {
                            serializer.startTag(null, "float")
                            serializer.attribute(null, "name", k)
                            serializer.attribute(null, "value", v.toString())
                            serializer.endTag(null, "float")
                        }
                        is Boolean -> {
                            serializer.startTag(null, "boolean")
                            serializer.attribute(null, "name", k)
                            serializer.attribute(null, "value", v.toString())
                            serializer.endTag(null, "boolean")
                        }
                        is Set<*> -> {
                            serializer.startTag(null, "set")
                            serializer.attribute(null, "name", k)
                            for (item in v) {
                                if (item is String) {
                                    serializer.startTag(null, "string")
                                    serializer.text(item)
                                    serializer.endTag(null, "string")
                                }
                            }
                            serializer.endTag(null, "set")
                        }
                    }
                }
                serializer.endTag(null, "map")
                serializer.endDocument()
                fos.flush()
            }
            backupFile.delete()
            diskStateGeneration = mcr.memoryStateGeneration
            true
        } catch (_: Throwable) {
            if (file.exists()) {
                file.delete()
            }
            false
        }
    }

    private inner class EditorImpl : SharedPreferences.Editor {
        private val editorLock = ReentrantLock()
        private val modified = LinkedHashMap<String, Any?>()
        private var clearRequested = false

        private val REMOVAL_SENTINEL = Any()

        override fun putString(key: String?, value: String?): SharedPreferences.Editor = editorLock.withLock {
            if (key != null) {
                modified[key] = value ?: REMOVAL_SENTINEL
            }
            this
        }

        override fun putStringSet(key: String?, values: Set<String>?): SharedPreferences.Editor = editorLock.withLock {
            if (key != null) {
                modified[key] = if (values == null) REMOVAL_SENTINEL else LinkedHashSet(values)
            }
            this
        }

        override fun putInt(key: String?, value: Int): SharedPreferences.Editor = editorLock.withLock {
            if (key != null) modified[key] = value
            this
        }

        override fun putLong(key: String?, value: Long): SharedPreferences.Editor = editorLock.withLock {
            if (key != null) modified[key] = value
            this
        }

        override fun putFloat(key: String?, value: Float): SharedPreferences.Editor = editorLock.withLock {
            if (key != null) modified[key] = value
            this
        }

        override fun putBoolean(key: String?, value: Boolean): SharedPreferences.Editor = editorLock.withLock {
            if (key != null) modified[key] = value
            this
        }

        override fun remove(key: String?): SharedPreferences.Editor = editorLock.withLock {
            if (key != null) modified[key] = REMOVAL_SENTINEL
            this
        }

        override fun clear(): SharedPreferences.Editor = editorLock.withLock {
            clearRequested = true
            this
        }

        override fun commit(): Boolean {
            val mcr = commitToMemory()
            val written = writeToFile(mcr)
            notifyListeners(mcr)
            return written
        }

        override fun apply() {
            val mcr = commitToMemory()
            writeToFile(mcr)
            notifyListeners(mcr)
        }

        private fun commitToMemory(): MemoryCommitResult {
            val (modsCopy, doClear) = editorLock.withLock {
                val copy = LinkedHashMap(modified)
                val clr = clearRequested
                modified.clear()
                clearRequested = false
                copy to clr
            }

            return lock.withLock {
                val keysModified = ArrayList<String>()
                var changesMade = false

                if (doClear) {
                    if (map.isNotEmpty()) {
                        changesMade = true
                        map.clear()
                    }
                }

                for ((k, v) in modsCopy) {
                    if (v === REMOVAL_SENTINEL || v == null) {
                        if (map.containsKey(k)) {
                            map.remove(k)
                            changesMade = true
                            keysModified.add(k)
                        }
                    } else {
                        val existing = map[k]
                        if (existing == null || existing != v) {
                            map[k] = v
                            changesMade = true
                            keysModified.add(k)
                        }
                    }
                }

                if (changesMade) {
                    currentMemoryStateGeneration++
                }

                MemoryCommitResult(
                    memoryStateGeneration = currentMemoryStateGeneration,
                    mapToWriteToDisk = LinkedHashMap(map),
                    keysModified = keysModified,
                    listeners = listeners.toList()
                )
            }
        }

        private fun notifyListeners(mcr: MemoryCommitResult) {
            if (mcr.listeners.isEmpty() || mcr.keysModified.isEmpty()) return
            for (i in mcr.keysModified.lastIndex downTo 0) {
                val key = mcr.keysModified[i]
                for (listener in mcr.listeners) {
                    listener.onSharedPreferenceChanged(this@SharedPreferencesImpl, key)
                }
            }
        }
    }
}
