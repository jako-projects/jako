@file:Suppress("NO_ACTUAL_FOR_EXPECT")

package android.content

/**
 * [Nullability Decision] ContextWrapper wraps a base Context (which may be attached via constructor
 * or attachBaseContext) and delegates all Context operations to it, matching AOSP android.content.ContextWrapper.
 */
expect open class ContextWrapper(base: Context?) : Context {
    val baseContext: Context?
    open fun getBaseContext(): Context?
}
