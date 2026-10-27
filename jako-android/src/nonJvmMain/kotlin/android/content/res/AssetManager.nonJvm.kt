package android.content.res

import android.content.platformDefaultAssetsDir
import android.content.platformDefaultResourcesDir
import com.bennyhuo.jako.JakoApplication
import java.io.File
import java.io.FileInputStream
import java.io.FileNotFoundException
import java.io.InputStream
import kotlin.math.roundToInt

actual final class AssetManager internal constructor(
    private val customAssetsDir: File? = null
) : AutoCloseable {

    private var closed = false

    internal fun resolveAssetsRoot(): File {
        return customAssetsDir
            ?: JakoApplication.config.assetsDir
            ?: platformDefaultAssetsDir()
    }

    private fun resolveAssetFile(relativePath: String): File {
        if (closed) {
            throw IllegalStateException("AssetManager has been closed")
        }
        val normalized = relativePath.trim().trimStart('/', '\\').replace('\\', '/')
        val parts = if (normalized.isEmpty()) emptyList() else normalized.split('/')
        for (part in parts) {
            if (part == "..") {
                throw FileNotFoundException("Asset path cannot escape root: $relativePath")
            }
        }
        val root = resolveAssetsRoot()
        return if (normalized.isEmpty()) root else File(root, normalized)
    }

    actual fun open(fileName: String): InputStream = open(fileName, ACCESS_STREAMING)

    actual fun open(fileName: String, accessMode: Int): InputStream {
        val target = resolveAssetFile(fileName)
        if (!target.exists() || !target.isFile) {
            throw FileNotFoundException("Asset file not found: $fileName (resolved: ${target.path})")
        }
        return FileInputStream(target)
    }

    actual fun list(path: String): Array<String>? {
        val target = resolveAssetFile(path)
        if (!target.exists() || !target.isDirectory) {
            return emptyArray()
        }
        val names = target.list() ?: return emptyArray()
        names.sort()
        return names
    }

    actual override fun close() {
        closed = true
    }

    actual companion object {
        actual const val ACCESS_UNKNOWN: Int = 0
        actual const val ACCESS_RANDOM: Int = 1
        actual const val ACCESS_STREAMING: Int = 2
        actual const val ACCESS_BUFFER: Int = 3
    }
}
