# jako-android API Checklist (Target: Android API 29 / AOSP)

This document tracks the Android Framework API porting progress for `jako-android`.

> **Architecture Note**: `jako-android` declares `android.*` `expect` APIs in `commonMain` and depends on `jako-stdlib` for `java.*` types.
> On JVM / Android, `compileKotlinJvm` has empty sources (`jvmJar` contains zero `.class` files) so real Android SDK / AOSP classes are resolved directly.
> On Native, each API is implemented under the same `android.*` packages using Kotlin / POSIX / Darwin APIs.

## Status Legend
- [x] Implemented & Tested
- [~] Stub / Simplified (compiles but limited functionality)
- [ ] Pending Implementation

---

## 1. `android.util` Package

### `android.util.Log`
- [x] `Log.VERBOSE` / `DEBUG` / `INFO` / `WARN` / `ERROR` / `ASSERT`
- [x] `Log.v(tag: String?, msg: String): Int` / `Log.v(tag: String?, msg: String?, tr: Throwable?): Int`
- [x] `Log.d(tag: String?, msg: String): Int` / `Log.d(tag: String?, msg: String?, tr: Throwable?): Int`
- [x] `Log.i(tag: String?, msg: String): Int` / `Log.i(tag: String?, msg: String?, tr: Throwable?): Int`
- [x] `Log.w(tag: String?, msg: String): Int` / `Log.w(tag: String?, msg: String?, tr: Throwable?): Int` / `Log.w(tag: String?, tr: Throwable?): Int`
- [x] `Log.e(tag: String?, msg: String): Int` / `Log.e(tag: String?, msg: String?, tr: Throwable?): Int`
- [x] `Log.wtf(tag: String?, msg: String?): Int` / `Log.wtf(tag: String?, tr: Throwable): Int` / `Log.wtf(tag: String?, msg: String?, tr: Throwable?): Int`
- [x] `Log.getStackTraceString(tr: Throwable?): String`
- [x] `Log.println(priority: Int, tag: String?, msg: String): Int`
- [x] `Log.isLoggable(tag: String?, level: Int): Boolean` (respects `JakoApplication.config.logMinPriority` & `JAKO_LOG_LEVEL` on Native)

### `android.util` DisplayMetrics & TypedValue
- [x] `DisplayMetrics` (`DENSITY_LOW`, `DENSITY_MEDIUM`, `DENSITY_TV`, `DENSITY_HIGH`, `DENSITY_XHIGH`, `DENSITY_XXHIGH`, `DENSITY_XXXHIGH`, `DENSITY_DEFAULT`, `widthPixels`, `heightPixels`, `density`, `densityDpi`, `scaledDensity`, `xdpi`, `ydpi`, `setTo`, `setToDefaults`)
- [x] `TypedValue` (`TYPE_NULL`, `TYPE_REFERENCE`, `TYPE_ATTRIBUTE`, `TYPE_STRING`, `TYPE_FLOAT`, `TYPE_DIMENSION`, `TYPE_FRACTION`, `TYPE_INT_DEC`, `TYPE_INT_HEX`, `TYPE_INT_BOOLEAN`, `TYPE_INT_COLOR_*`, `COMPLEX_UNIT_PX`, `COMPLEX_UNIT_DIP`, `COMPLEX_UNIT_SP`, `COMPLEX_UNIT_PT`, `COMPLEX_UNIT_IN`, `COMPLEX_UNIT_MM`, `type`, `string`, `data`, `assetCookie`, `resourceId`, `changingConfigurations`, `density`, `float`, `getDimension`, `getFraction`, `coerceToString`, `setTo`, `applyDimension`, `complexToDimension`, `complexToDimensionPixelOffset`, `complexToDimensionPixelSize`, `complexToFloat`)

### `android.util` Value Tuples & Math Models
- [x] `Pair<F, S>(first: F, second: S)` (`first`, `second`, `equals`, `hashCode`, `toString`, `Pair.create`)
- [x] `Size(width: Int, height: Int)` (`getWidth`, `getHeight`, `parseSize`, `equals`, `hashCode`, `toString`)
- [x] `SizeF(width: Float, height: Float)` (`getWidth`, `getHeight`, `parseSizeF`, `equals`, `hashCode`, `toString`)
- [x] `Rational(numerator: Int, denominator: Int)` (`getNumerator`, `getDenominator`, `isNaN`, `isInfinite`, `isZero`, `isFinite`, `compareTo`, `parseRational`, `NaN`, `POSITIVE_INFINITY`, `NEGATIVE_INFINITY`, `ZERO`)
- [x] `Range<T : Comparable<T>>(lower: T, upper: T)` (`getLower`, `getUpper`, `contains`, `clamp`, `intersect`, `extend`, `Range.create`)

### `android.util.Base64`
- [x] `Base64.DEFAULT` / `NO_PADDING` / `NO_WRAP` / `CRLF` / `URL_SAFE` / `NO_CLOSE`
- [x] `Base64.encode(input: ByteArray, flags: Int): ByteArray`
- [x] `Base64.encode(input: ByteArray, offset: Int, len: Int, flags: Int): ByteArray`
- [x] `Base64.encodeToString(input: ByteArray, flags: Int): String`
- [x] `Base64.encodeToString(input: ByteArray, offset: Int, len: Int, flags: Int): String`
- [x] `Base64.decode(str: String, flags: Int): ByteArray`
- [x] `Base64.decode(input: ByteArray, flags: Int): ByteArray`
- [x] `Base64.decode(input: ByteArray, offset: Int, len: Int, flags: Int): ByteArray`

### `android.util.Patterns`
- [x] `Patterns.EMAIL_ADDRESS` / `WEB_URL` / `IP_ADDRESS` / `DOMAIN_NAME` / `PHONE` / `GOOD_IRI_CHAR`
- [x] `Patterns.concatGroups(matcher: Matcher)` / `Patterns.digitsAndPlusOnly(matcher: Matcher)`

### `android.util.Xml`
- [x] `Xml.newPullParser(): XmlPullParser` / `Xml.newSerializer(): XmlSerializer`
- [x] `Xml.findEncodingByName(encodingName: String?): Xml.Encoding` (`US_ASCII`, `UTF_8`, `UTF_16`, `ISO_8859_1`)
- [x] `Xml.FEATURE_RELAXED`

### `android.util` Specialized Containers & Cache
- [x] `LruCache<K, V>(maxSize: Int)` (`get`, `put`, `remove`, `trimToSize`, `evictAll`, `size`, `maxSize`, `hitCount`, `missCount`, `createCount`, `putCount`, `evictionCount`, `snapshot`, `resize`, `create`, `entryRemoved`, `sizeOf`)
- [x] `SparseArray<E>` (`get`, `put`, `append`, `delete`, `remove`, `removeAt`, `clear`, `size`, `keyAt`, `valueAt`, `setValueAt`, `indexOfKey`, `indexOfValue`, `contains`, `clone`)
- [x] `SparseBooleanArray` (`get`, `put`, `append`, `delete`, `removeAt`, `clear`, `size`, `keyAt`, `valueAt`, `setValueAt`, `indexOfKey`, `indexOfValue`, `clone`)
- [x] `SparseIntArray` (`get`, `put`, `append`, `delete`, `removeAt`, `clear`, `size`, `keyAt`, `valueAt`, `setValueAt`, `indexOfKey`, `indexOfValue`, `clone`)
- [x] `SparseLongArray` (`get`, `put`, `append`, `delete`, `removeAt`, `clear`, `size`, `keyAt`, `valueAt`, `setValueAt`, `indexOfKey`, `indexOfValue`, `clone`)
- [x] `LongSparseArray<E>` (`get`, `put`, `append`, `delete`, `remove`, `removeAt`, `clear`, `size`, `keyAt`, `valueAt`, `setValueAt`, `indexOfKey`, `indexOfValue`, `clone`)
- [x] `ArrayMap<K, V>` (`MutableMap<K, V>`, `keyAt`, `valueAt`, `setValueAt`, `removeAt`, `indexOfKey`, `indexOfValue`, `ensureCapacity`, `putAll`)
- [x] `ArraySet<E>` (`MutableCollection<E>`, `MutableSet<E>`, `valueAt`, `removeAt`, `indexOf`, `ensureCapacity`, `addAll`)

### `android.util` Streaming JSON (`JsonReader` & `JsonWriter`)
- [x] `JsonReader(inReader: Reader)` (`beginArray`, `endArray`, `beginObject`, `endObject`, `hasNext`, `peek`, `nextName`, `nextString`, `nextBoolean`, `nextNull`, `nextDouble`, `nextLong`, `nextInt`, `skipValue`, `isLenient`, `close`)
- [x] `JsonWriter(out: Writer)` (`beginArray`, `endArray`, `beginObject`, `endObject`, `name`, `value`, `nullValue`, `setIndent`, `isLenient`, `flush`, `close`)
- [x] `JsonToken` (`BEGIN_ARRAY`, `END_ARRAY`, `BEGIN_OBJECT`, `END_OBJECT`, `NAME`, `STRING`, `NUMBER`, `BOOLEAN`, `NULL`, `END_DOCUMENT`)
- [x] `MalformedJsonException`

---

## 2. `android.text` Package

### `android.text.TextUtils`
- [x] `TextUtils.isEmpty(str: CharSequence?): Boolean`
- [x] `TextUtils.equals(a: CharSequence?, b: CharSequence?): Boolean`
- [x] `TextUtils.isDigitsOnly(str: CharSequence): Boolean`
- [x] `TextUtils.getTrimmedLength(s: CharSequence): Int`
- [x] `TextUtils.join(delimiter: CharSequence, tokens: Array<*>): String`
- [x] `TextUtils.join(delimiter: CharSequence, tokens: Iterable<*>): String`
- [x] `TextUtils.split(text: String, expression: String): Array<String>`
- [x] `TextUtils.split(text: String, pattern: Pattern): Array<String>`
- [x] `TextUtils.htmlEncode(s: String): String`
- [x] `TextUtils.indexOf` / `lastIndexOf` / `substring` / ` regionMatches`

### `android.text.format` (`Formatter` & `DateUtils`)
- [x] `Formatter.formatFileSize(context: Context?, sizeBytes: Long): String`
- [x] `Formatter.formatShortFileSize(context: Context?, sizeBytes: Long): String`
- [x] `DateUtils` (`SECOND_IN_MILLIS`, `MINUTE_IN_MILLIS`, `HOUR_IN_MILLIS`, `DAY_IN_MILLIS`, `WEEK_IN_MILLIS`, `YEAR_IN_MILLIS`, `formatElapsedTime(elapsedSeconds)`, `formatElapsedTime(recycle, elapsedSeconds)`, `isToday(whenMillis)`)

---

## 3. `android.net` Package

### `android.net.Uri` & `Uri.Builder`
- [x] `Uri.EMPTY`
- [x] `Uri.parse(uriString: String): Uri`
- [x] `Uri.fromFile(file: File): Uri`
- [x] `Uri.fromParts(scheme: String, ssp: String, fragment: String?): Uri`
- [x] `Uri.encode(s: String?): String?` / `Uri.encode(s: String?, allow: String?): String?`
- [x] `Uri.decode(s: String?): String?`
- [x] `Uri.withAppendedPath(baseUri: Uri, pathSegment: String): Uri`
- [x] `isHierarchical` / `isOpaque` / `isRelative` / `isAbsolute`
- [x] `scheme` / `schemeSpecificPart` / `encodedSchemeSpecificPart`
- [x] `authority` / `encodedAuthority` / `userInfo` / `encodedUserInfo` / `host` / `port`
- [x] `path` / `encodedPath` / `pathSegments` / `lastPathSegment`
- [x] `query` / `encodedQuery` / `fragment` / `encodedFragment`
- [x] `getQueryParameter(key: String)` / `getQueryParameters(key: String)` / `queryParameterNames` / `getBooleanQueryParameter(key: String, defaultValue: Boolean)`
- [x] `normalizeScheme()` / `buildUpon()`
- [x] `Uri.Builder` (`scheme`, `opaquePart`, `encodedOpaquePart`, `authority`, `encodedAuthority`, `path`, `encodedPath`, `appendPath`, `appendEncodedPath`, `query`, `encodedQuery`, `appendQueryParameter`, `clearQuery`, `fragment`, `encodedFragment`, `build`)

---

## 4. `android.os` Package

### `android.os` System, Build, Environment & Storage Info
- [x] `SystemClock` (`uptimeMillis()`, `elapsedRealtime()`, `elapsedRealtimeNanos()`, `sleep(ms)`)
- [x] `Build` / `Build.VERSION` (`SDK_INT`, `RELEASE`) / `Build.VERSION_CODES`
- [x] `Process` (`myPid()`, `myTid()`, `myUid()`, `is64Bit()`, thread priority constants)
- [x] `Environment` (`MEDIA_MOUNTED`, `MEDIA_MOUNTED_READ_ONLY`, `MEDIA_UNMOUNTED`, `MEDIA_REMOVED`, `MEDIA_UNKNOWN`, `DIRECTORY_MUSIC`, `DIRECTORY_PODCASTS`, `DIRECTORY_RINGTONES`, `DIRECTORY_ALARMS`, `DIRECTORY_NOTIFICATIONS`, `DIRECTORY_PICTURES`, `DIRECTORY_MOVIES`, `DIRECTORY_DOWNLOADS`, `DIRECTORY_DCIM`, `DIRECTORY_DOCUMENTS`, `getRootDirectory()`, `getDataDirectory()`, `getDownloadCacheDirectory()`, `getExternalStorageDirectory()`, `getExternalStoragePublicDirectory(type)`, `getExternalStorageState()`, `isExternalStorageRemovable()`, `isExternalStorageEmulated()`)
- [x] `StatFs(path: String?)` (`restat`, `blockSizeLong`, `blockCountLong`, `freeBlocksLong`, `availableBlocksLong`, `freeBytes`, `availableBytes`, `totalBytes`, plus deprecated `Int` getters)

### `android.os` Parcel, Bundle & Cross-Platform `@Parcelize` (`kotlinx.parcelize`)
- [x] `Parcelable` & `Parcelable.Creator<T>`
- [x] `kotlinx.parcelize.Parcelize` & `kotlinx.parcelize.IgnoredOnParcel` (cross-platform K2 FIR + IR compiler plugin in `:jako-compiler-plugin` that synthesizes `describeContents()`, `writeToParcel(dest, flags)`, and `CREATOR: Parcelable.Creator<T>` across both JVM and Kotlin/Native targets in `commonMain`)
- [x] `Parcel` (`obtain`, `recycle`, `dataPosition`, `setDataPosition`, `dataSize`, `writeByte`, `readByte`, `writeInt`, `readInt`, `writeLong`, `readLong`, `writeFloat`, `readFloat`, `writeDouble`, `readDouble`, `writeString`, `readString`, `writeByteArray`, `createByteArray`, `writeBooleanArray`, `createBooleanArray`, `writeCharArray`, `createCharArray`, `writeIntArray`, `createIntArray`, `writeLongArray`, `createLongArray`, `writeFloatArray`, `createFloatArray`, `writeDoubleArray`, `createDoubleArray`, `writeStringArray`, `createStringArray`, `writeStringList`, `createStringArrayList`, `writeTypedObject`, `readTypedObject`, `writeTypedList`, `createTypedArrayList`, `writeBundle`, `readBundle`, `marshall`, `unmarshall`)
- [x] `BaseBundle` / `Bundle` / `PersistableBundle` (all primitive, String, array, ArrayList, Serializable, Parcelable, and nested Bundle getters/putters, `containsKey`, `remove`, `clear`, `keySet`, `size`, `isEmpty`)

### `android.os` Threading, Cancellation & File I/O
- [x] `Looper` (`prepare`, `prepareMainLooper`, `myLooper`, `getMainLooper`, `loop`, `quit`, `quitSafely`, `isCurrentThread`, `thread`)
- [x] `Message` (`obtain`, `recycle`, `what`, `arg1`, `arg2`, `obj`, `data`, `target`, `callback`, `when`, `sendToTarget`, `copyFrom`)
- [x] `MessageQueue` (`isIdle`, `addIdleHandler`, `removeIdleHandler`)
- [x] `Handler` (`Callback`, `post`, `postDelayed`, `postAtTime`, `postAtFrontOfQueue`, `sendMessage`, `sendEmptyMessage`, `sendMessageDelayed`, `sendMessageAtTime`, `sendMessageAtFrontOfQueue`, `removeCallbacks`, `removeMessages`, `removeCallbacksAndMessages`, `hasMessages`, `hasCallbacks`, `obtainMessage`, `handleMessage`, `dispatchMessage`, `looper`)
- [x] `HandlerThread(name: String, priority: Int)` (`start`, `looper`, `quit`, `quitSafely`, `threadId`)
- [x] `CancellationSignal` (`isCanceled`, `throwIfCanceled`, `cancel`, `setOnCancelListener`, `OnCancelListener`) & `OperationCanceledException`
- [x] `FileUtils` (`copy(inStream: InputStream, outStream: OutputStream): Long`, `closeQuietly(closeable: AutoCloseable?)`)

---

## 5. `android.graphics` Package

### `android.graphics` Geometry & Color
- [x] `Point(x: Int, y: Int)` (`set`, `negate`, `offset`, `equals`)
- [x] `PointF(x: Float, y: Float)` (`set`, `negate`, `offset`, `length`, `equals`)
- [x] `Rect(left: Int, top: Int, right: Int, bottom: Int)` (`isEmpty`, `width`, `height`, `centerX`, `centerY`, `exactCenterX`, `exactCenterY`, `set`, `setEmpty`, `offset`, `offsetTo`, `inset`, `contains`, `intersect`, `intersects`, `union`, `sort`, `flattenToString`, `unflattenFromString`)
- [x] `RectF(left: Float, top: Float, right: Float, bottom: Float)` (`isEmpty`, `width`, `height`, `centerX`, `centerY`, `set`, `setEmpty`, `offset`, `offsetTo`, `inset`, `contains`, `intersect`, `intersects`, `union`, `sort`, `round`, `roundOut`)
- [x] `Matrix` (3x3 affine & perspective matrix: `reset`, `set`, `isIdentity`, `isAffine`, `rectStaysRect`, `setTranslate`, `preTranslate`, `postTranslate`, `setScale`, `preScale`, `postScale`, `setRotate`, `preRotate`, `postRotate`, `setSinCos`, `setSkew`, `preSkew`, `postSkew`, `setConcat`, `preConcat`, `postConcat`, `setRectToRect`, `invert`, `mapPoints`, `mapVectors`, `mapRect`, `mapRadius`, `getValues`, `setValues`)
- [x] `Color` (color constants, `alpha`, `red`, `green`, `blue`, `argb`, `rgb`, `parseColor`, `RGBToHSV`, `HSVToColor`, `colorToHSV`, `luminance`)

---

## 6. `org.json` Package (AOSP Built-in JSON)

- [x] `JSONObject` (constructors from JSON string / `JSONTokener` / `Map` / `JSONObject`, `put`, `putOpt`, `accumulate`, `remove`, `has`, `isNull`, `get*`, `opt*`, `keys`, `names`, `toJSONArray`, `toString`, `NULL`, `quote`, `wrap`, `numberToString`)
- [x] `JSONArray` (constructors from JSON string / `JSONTokener` / `Collection` / primitive or object array, `put`, `remove`, `isNull`, `get*`, `opt*`, `toJSONObject`, `join`, `toString`)
- [x] `JSONTokener` (`nextValue`, `nextString`, `nextClean`, `next`, `nextTo`, `skipPast`, `skipTo`, `back`, `more`, `syntaxError`, `dehexchar`)
- [x] `JSONStringer` (`array`, `endArray`, `object`, `endObject`, `key`, `value`, `toString`)
- [x] `JSONException`

---

## 7. `org.xmlpull.v1` Package (AOSP Built-in XMLPull)

- [x] `XmlPullParser` (`setInput`, `setFeature`, `getFeature`, `next`, `nextToken`, `nextTag`, `nextText`, `require`, `getEventType`, `getName`, `getNamespace`, `getPrefix`, `getText`, `isEmptyElementTag`, `getAttributeCount`, `getAttributeName`, `getAttributeValue`, `getNamespaceCount`, `getDepth`, `getLineNumber`, `getColumnNumber`)
- [x] `XmlSerializer` (`setOutput`, `setFeature`, `startDocument`, `endDocument`, `setPrefix`, `getPrefix`, `startTag`, `attribute`, `endTag`, `text`, `cdsect`, `entityRef`, `processingInstruction`, `comment`, `docdecl`, `ignorableWhitespace`, `flush`)
- [x] `XmlPullParserFactory` (`newInstance`, `newPullParser`, `newSerializer`, `setNamespaceAware`, `isNamespaceAware`, `setValidating`, `isValidating`, `setFeature`, `getFeature`)
- [x] `XmlPullParserException` (`detail`, `lineNumber`, `columnNumber`)

---

## 8. `android.webkit` Package

- [x] `MimeTypeMap` (`getSingleton()`, `getFileExtensionFromUrl(url)`, `hasMimeType(mimeType)`, `getMimeTypeFromExtension(extension)`, `hasExtension(extension)`, `getExtensionFromMimeType(mimeType)`)
- [x] `URLUtil` (`guessUrl`, `composeSearchUrl`, `decode`, `isAssetUrl`, `isFileUrl`, `isAboutUrl`, `isDataUrl`, `isJavaScriptUrl`, `isHttpUrl`, `isHttpsUrl`, `isNetworkUrl`, `isContentUrl`, `isValidUrl`, `stripAnchor`, `guessFileName`)

---

## 9. `android.content`, `android.content.res`, `android.content.pm`, `android.app` & `android.preference` Packages

- [x] `SharedPreferences` (`getAll`, `getString`, `getStringSet`, `getInt`, `getLong`, `getFloat`, `getBoolean`, `contains`, `edit`, `registerOnSharedPreferenceChangeListener`, `unregisterOnSharedPreferenceChangeListener`)
- [x] `SharedPreferences.Editor` (`putString`, `putStringSet`, `putInt`, `putLong`, `putFloat`, `putBoolean`, `remove`, `clear`, `commit`, `apply`)
- [x] `SharedPreferences.OnSharedPreferenceChangeListener` (`onSharedPreferenceChanged`)
- [x] `Context` (`MODE_PRIVATE`, `MODE_APPEND`, `assets`, `resources`, `packageManager`, `mainLooper`, `applicationContext`, `packageName`, `applicationInfo`, `dataDir`, `filesDir`, `noBackupFilesDir`, `externalFilesDir`, `obbDir`, `cacheDir`, `codeCacheDir`, `externalCacheDir`, `getDir`, `getFileStreamPath`, `openFileInput`, `openFileOutput`, `deleteFile`, `fileList`, `getDatabasePath`, `databaseList`, `deleteDatabase`, `getSharedPreferences`, `deleteSharedPreferences`, `moveSharedPreferencesFrom`, `getString`, `getText`, `getColor`, `checkPermission`, `checkSelfPermission`)
- [x] `ContextWrapper` (`baseContext`, `attachBaseContext`, and full `Context` delegation)
- [x] `Application` (`onCreate`, `onTerminate`, `onConfigurationChanged`, `onLowMemory`, `onTrimMemory`, and full `ContextWrapper` support)
- [x] `AssetManager` (`ACCESS_UNKNOWN`, `ACCESS_RANDOM`, `ACCESS_STREAMING`, `ACCESS_BUFFER`, `open(fileName)`, `open(fileName, accessMode)`, `list(path)`, `locales`, `close()`)
- [x] `Configuration` (`ORIENTATION_UNDEFINED`, `ORIENTATION_PORTRAIT`, `ORIENTATION_LANDSCAPE`, `UI_MODE_NIGHT_MASK`, `UI_MODE_NIGHT_NO`, `UI_MODE_NIGHT_YES`, `DENSITY_DPI_UNDEFINED`, `fontScale`, `locale`, `orientation`, `uiMode`, `screenWidthDp`, `screenHeightDp`, `smallestScreenWidthDp`, `densityDpi`, `setTo`, `setToDefaults`, `updateFrom`, `diff`, `setLocale`)
- [x] `Resources` & `Resources.NotFoundException` (`getSystem()`, `assets`, `displayMetrics`, `configuration`, `updateConfiguration`, `getString`, `getText`, `getStringArray`, `getIntArray`, `getInteger`, `getBoolean`, `getColor`, `getDimension`, `getDimensionPixelOffset`, `getDimensionPixelSize`, `openRawResource`, `getIdentifier`, `getResourceName`, `getResourcePackageName`, `getResourceTypeName`, `getResourceEntryName`, `getValue` — supports both programmatic registration via `JakoApplication.init` and XML/raw files under `res/values/*.xml` and `res/raw/*`)
- [x] `PackageItemInfo` / `ApplicationInfo` / `PackageInfo` / `PackageManager` (`GET_META_DATA`, `PERMISSION_GRANTED`, `PERMISSION_DENIED`, `getPackageInfo`, `getApplicationInfo`, `getApplicationLabel`, `getInstallerPackageName`, `checkPermission`, `hasSystemFeature`)
- [x] `PreferenceManager` (`getDefaultSharedPreferences`, `getDefaultSharedPreferencesName`)

---

## 10. `com.bennyhuo.jako` Startup Initialization & Conventions

- [x] `JakoApplication.init { ... }` & `JakoApplicationConfig` (programmatic configuration of `packageName`, `versionName`, `versionCode`, `dataDir`, `cacheDir`, `externalStorageDir`, `assetsDir`, `resourcesDir`, `densityDpi`, `screenWidthPx`, `screenHeightPx`, `locale`, `metaData`, `logMinPriority`, and `registerStringResource`/`registerIntegerResource`/`registerBooleanResource`/`registerColorResource`/`registerDimensionResource`/`registerStringArrayResource`/`registerIntArrayResource`/`registerRawResource`)
- [x] `JakoApplication.application` & `JakoApplication.applicationContext` (ready out-of-the-box with zero-config OS conventions even if `JakoApplication.init` is not called)
- [x] Multi-level directory resolution for PC/Native:
  1. Explicit `JakoApplication.init { ... }` configuration
  2. Environment variables (`JAKO_ASSETS_DIR`, `JAKO_RESOURCES_DIR`, `JAKO_DATA_DIR`, `JAKO_CACHE_DIR`, `JAKO_EXTERNAL_STORAGE_DIR`, `JAKO_PACKAGE_NAME`)
  3. Bundle / executable sibling directories (`NSBundle.mainBundle.resourcePath` on Apple, `<exe_dir>/assets` & `<exe_dir>/res` on Linux/Windows/Apple)
  4. Working directory fallback (`./assets`, `./res`)
- [x] `:jako-gradle-plugin` (`id("com.bennyhuo.jako")`, `jako { packageName.set("...") }`) — scans `res/values/*.xml` and `res/raw/*` at compile time and generates `R.kt` (`R.raw`, `R.string`, `R.integer`, `R.bool`, `R.color`, `R.dimen`, `R.array`) with deterministic FNV-1a 16-bit entry IDs (`0x7fTTNNNN`) matching runtime `Resources`

---

## 11. Explicitly Unsupported / Out-of-Scope Android APIs (明确不移植的 API)

The following Android Framework APIs are **explicitly excluded** from `jako-android` by design (either because the original Android API design is obsolete/flawed and has no porting value, or because they belong to platform-specific UI/IPC subsystems better served by modern Kotlin Multiplatform libraries):

| Category / Package | Excluded Android APIs | Rationale & Modern KMP Alternatives |
| :--- | :--- | :--- |
| **Legacy Async & Timers** (`android.os`) | `AsyncTask`, `CountDownTimer` | **`AsyncTask` is officially deprecated in Android (API 30) due to notorious lifecycle leaks, rigid threading semantics, and poor error handling** — porting it has zero value. Use **Kotlin Coroutines (`kotlinx.coroutines`)** (`Dispatchers.Default` / `Dispatchers.IO`, `flow`) or `java.util.concurrent.Executors` / `Handler` instead. |
| **Legacy SQLite & Cursor APIs** (`android.database`, `android.database.sqlite`, `android.content`) | `SQLiteDatabase`, `SQLiteOpenHelper`, `SQLiteStatement`, `Cursor`, `MatrixCursor`, `AbstractCursor`, `ContentValues` | **Android's raw `SQLiteDatabase` / `Cursor` / `ContentValues` API is poorly designed** (untyped column-index lookups, stateful cursor lifecycle traps, untyped `ContentValues` bags, and leaky `SQLiteOpenHelper` singletons). Note that `Context.getDatabasePath` / `databaseList` / `deleteDatabase` are still provided for database file path management; for actual database access, use **Room KMP (`androidx.room`)** or **SQLDelight**. |
| **Hidden / Internal OS Properties** (`android.os`) | `SystemProperties` | Hidden `@hide` AOSP internal API backed by Android's `bionic` `/dev/__properties__` shared memory. Not part of the public Android SDK. Use `System.getProperty(...)`, `System.getenv(...)`, or `android.os.Build`. |
| **Kernel File Watching** (`android.os`) | `FileObserver` | Directly tied to Linux `inotify` masks and flags, behaving inconsistently across macOS (`kqueue`/`FSEvents`) and Windows (`ReadDirectoryChangesW`). |
| **Pixel Bitmap & UI Rendering** (`android.graphics`, `android.view`) | `Bitmap`, `BitmapFactory`, `Canvas`, `Paint`, `Drawable`, `View`, `ViewGroup`, `Window` | Tied to Android's C++ `libhwui` / `Skia` rendering pipeline (`Point`, `PointF`, `Rect`, `RectF`, `Matrix`, and `Color` value/math classes *are* supported). For cross-platform UI and image rendering, use **Compose Multiplatform**, **Skiko**, or **Coil 3 KMP**. |
| **Android Rich-Text Span Model** (`android.text`, `android.text.style`) | `Spanned`, `Spannable`, `SpannableString`, `SpannableStringBuilder`, `Editable`, `ForegroundColorSpan`, `StyleSpan`, `ClickableSpan`, `Html` | `SpannableString` and `android.text.style.*` spans (`CharacterStyle.updateDrawState(TextPaint)`, `ReplacementSpan.draw(Canvas, ...)`, `ClickableSpan.onClick(View)`) are exclusively consumed by Android's `TextView` / `StaticLayout` / `TextPaint` rendering engine. On non-Android targets (Compose Multiplatform, iOS `UILabel`/`SwiftUI`, Web), there is no consumer for `SpannableString`. Use **`androidx.compose.ui.text.AnnotatedString`** (`buildAnnotatedString`) in Compose Multiplatform instead. |
| **Android IPC & Component Lifecycle** (`android.app`, `android.content`) | `Activity`, `Service`, `BroadcastReceiver`, `ContentProvider`, `ContentResolver`, `Intent`, `Binder`, `IBinder` | Bound to Android's `Zygote` / `ActivityManagerService` / Linux `binder` kernel driver (`Context`, `ContextWrapper`, and `Application` *are* supported for app environment, file/cache/database paths, `SharedPreferences`, `Assets`, and `Resources`). |
