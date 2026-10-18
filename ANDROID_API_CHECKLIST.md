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
- [x] `Log.isLoggable(tag: String?, level: Int): Boolean`

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
- [x] `Patterns.EMAIL_ADDRESS` / `WEB_URL` / `IP_ADDRESS` / `DOMAIN_NAME` / `PHONE`

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

### `android.os` System & Build Info
- [x] `SystemClock` (`uptimeMillis()`, `elapsedRealtime()`, `elapsedRealtimeNanos()`, `sleep(ms)`)
- [x] `Build` / `Build.VERSION` (`SDK_INT`, `RELEASE`) / `Build.VERSION_CODES`
- [x] `Process` (`myPid()`, `myTid()`, `myUid()`, `is64Bit()`, thread priority constants)

### `android.os` Parcel & Bundle
- [x] `Parcelable` & `Parcelable.Creator<T>`
- [x] `Parcel` (`obtain`, `recycle`, `dataPosition`, `setDataPosition`, `dataSize`, `writeInt`, `readInt`, `writeLong`, `readLong`, `writeFloat`, `readFloat`, `writeDouble`, `readDouble`, `writeString`, `readString`, `writeByteArray`, `createByteArray`, `writeBundle`, `readBundle`, `marshall`, `unmarshall`)
- [x] `BaseBundle` / `Bundle` / `PersistableBundle` (all primitive, String, array, ArrayList, Serializable, and nested Bundle getters/putters, `containsKey`, `remove`, `clear`, `keySet`, `size`, `isEmpty`)

### `android.os` Threading & Messaging
- [x] `Looper` (`prepare`, `prepareMainLooper`, `myLooper`, `getMainLooper`, `loop`, `quit`, `quitSafely`, `isCurrentThread`, `thread`)
- [x] `Message` (`obtain`, `recycle`, `what`, `arg1`, `arg2`, `obj`, `data`, `target`, `callback`, `when`, `sendToTarget`, `copyFrom`)
- [x] `MessageQueue` (`isIdle`, `addIdleHandler`, `removeIdleHandler`)
- [x] `Handler` (`Callback`, `post`, `postDelayed`, `postAtTime`, `postAtFrontOfQueue`, `sendMessage`, `sendEmptyMessage`, `sendMessageDelayed`, `sendMessageAtTime`, `sendMessageAtFrontOfQueue`, `removeCallbacks`, `removeMessages`, `removeCallbacksAndMessages`, `hasMessages`, `hasCallbacks`, `obtainMessage`, `handleMessage`, `dispatchMessage`, `looper`)
- [x] `HandlerThread(name: String, priority: Int)` (`start`, `looper`, `quit`, `quitSafely`, `threadId`)

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
