@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.content.res

import java.io.InputStream

/**
 * Provides access to an application's raw asset files.
 *
 * On PC / Native, [AssetManager] resolves files from the configured `assetsDir`
 * (`JakoApplicationConfig.assetsDir`), environment variable `JAKO_ASSETS_DIR`,
 * platform bundle / executable sibling `assets/` directory, or `./assets` in the working directory.
 *
 * [Nullability Decision]:
 * - `open(...)` takes non-null `fileName: String` and returns non-null `InputStream` (throwing `IOException` / `FileNotFoundException` if missing).
 * - `list(path: String)` returns `Array<String>?` (non-null empty array when directory has no entries, matching AOSP).
 */
expect final class AssetManager : AutoCloseable {
    fun open(fileName: String): InputStream
    fun open(fileName: String, accessMode: Int): InputStream
    fun list(path: String): Array<String>?
    override fun close()

    companion object {
        val ACCESS_UNKNOWN: Int
        val ACCESS_RANDOM: Int
        val ACCESS_STREAMING: Int
        val ACCESS_BUFFER: Int
    }
}
