# jako-stdlib API Checklist (Target: Java 8)

This document tracks the Java 8 Standard Library API porting progress for `jako-stdlib`.

> **Architecture Note**: `jako` declares `java.*` / `javax.*` `expect` APIs and concrete `kotlin.*` extensions in `commonMain`.
> On JVM, `compileKotlinJvm` has empty sources (`jvmJar` contains zero `.class` files) so real JDK and `kotlin-stdlib` classes are resolved directly.
> On Native, each API is implemented via POSIX / Win32 C-Interop or pure Kotlin under the same `java.*` / `javax.*` packages.

## Status Legend
- [x] Implemented & Tested
- [~] Stub / Simplified (compiles but limited functionality)
- [ ] Pending Implementation

---

## 1. `java.io` Package

### `java.io.File`
- [x] `File(pathname: String)`
- [x] `File(parent: String, child: String)`
- [x] `File(parent: File?, child: String)`
- [x] `getName(): String`
- [x] `getParent(): String?`
- [x] `getParentFile(): File?`
- [x] `getPath(): String`
- [x] `isAbsolute(): Boolean`
- [x] `getAbsolutePath(): String`
- [x] `exists(): Boolean`
- [x] `isDirectory(): Boolean`
- [x] `isFile(): Boolean`
- [x] `length(): Long`
- [x] `delete(): Boolean`
- [x] `mkdir(): Boolean`
- [x] `mkdirs(): Boolean`
- [x] `list(): Array<String>?`
- [x] `listFiles(): Array<File>?`
- [x] `list(FilenameFilter?)` / `listFiles(FileFilter?)` / `listFiles(FilenameFilter?)`
- [x] `lastModified()` / `setLastModified(time)`
- [x] `renameTo(dest)` / `createNewFile()` / `isHidden()` / `deleteOnExit()`
- [x] `canRead()` / `canWrite()` / `canExecute()` / `setReadable()` / `setWritable()` / `setExecutable()` / `setReadOnly()`
- [x] `getCanonicalPath()` / `getCanonicalFile()`
- [x] `File(uri: URI)` / `toURI(): URI` / `toPath(): Path`
- [x] `File.listRoots(): Array<File>` / `getTotalSpace()` / `getFreeSpace()` / `getUsableSpace()`
- [x] `File.separator` / `separatorChar` / `pathSeparator` / `pathSeparatorChar`

### `java.io` Interfaces & Filters
- [x] `Closeable` (`close()`)
- [x] `Flushable` (`flush()`)
- [x] `Serializable`
- [x] `Externalizable` (`writeExternal`, `readExternal`)
- [x] `ObjectInput` (extends `DataInput`, `AutoCloseable`; `readObject`, `read`, `skip`, `available`, `close`)
- [x] `ObjectOutput` (extends `DataOutput`, `AutoCloseable`; `writeObject`, `write`, `flush`, `close`)
- [x] `ObjectStreamConstants` (`STREAM_MAGIC`, `STREAM_VERSION`, `TC_*`, `SC_*`, `PROTOCOL_VERSION_1`, `PROTOCOL_VERSION_2`)
- [x] `FileFilter` (`accept(pathname: File): Boolean`)
- [x] `FilenameFilter` (`accept(dir: File, name: String): Boolean`)

### `java.io` Exceptions
- [x] `IOException`
- [x] `UncheckedIOException` (`cause: IOException`)
- [x] `FileNotFoundException`
- [x] `EOFException`
- [x] `InterruptedIOException`
- [x] `UnsupportedEncodingException`
- [x] `UTFDataFormatException`
- [x] `CharConversionException`, `SyncFailedException`
- [x] `ObjectStreamException`, `InvalidClassException`, `NotSerializableException`, `InvalidObjectException`, `StreamCorruptedException`, `NotActiveException`, `WriteAbortedException`

### `java.io.InputStream` & `java.io.OutputStream`
- [x] `InputStream.read(): Int`
- [x] `InputStream.read(b: ByteArray): Int`
- [x] `InputStream.read(b: ByteArray, off: Int, len: Int): Int`
- [x] `InputStream.skip(n: Long): Long`
- [x] `InputStream.available(): Int`
- [x] `InputStream.close()`
- [x] `InputStream.mark(readlimit: Int)`
- [x] `InputStream.reset()`
- [x] `InputStream.markSupported(): Boolean`
- [x] `OutputStream.write(b: Int)`
- [x] `OutputStream.write(b: ByteArray)`
- [x] `OutputStream.write(b: ByteArray, off: Int, len: Int)`
- [x] `OutputStream.flush()`
- [x] `OutputStream.close()`

### `java.io` Memory & Buffered Byte Streams
- [x] `ByteArrayInputStream(buf: ByteArray)`
- [x] `ByteArrayOutputStream()`
- [x] `BufferedInputStream(inStream: InputStream)`
- [x] `BufferedOutputStream(out: OutputStream)`

### `java.io` File Byte & Character Streams
- [x] `FileDescriptor` (`in`, `out`, `err`, `valid()`, `sync()`)
- [x] `FileInputStream(file: File)` / `FileInputStream(fd: FileDescriptor)`
- [x] `FileOutputStream(file: File)` / `FileOutputStream(fd: FileDescriptor)`
- [x] `FileReader(file: File)`
- [x] `FileWriter(file: File)`

### `java.io` Data Streams & Interfaces
- [x] `DataInput` (`readInt()`, `readLong()`, `readUTF()`, etc.)
- [x] `DataOutput` (`writeInt()`, `writeLong()`, `writeUTF()`, etc.)
- [x] `DataInputStream(inStream: InputStream)`
- [x] `DataOutputStream(out: OutputStream)`

### `java.io` Formatted Print Streams
- [x] `PrintStream(out: OutputStream)`
- [x] `PrintWriter(out: Writer)`

### `java.io.Reader` & `java.io.Writer`
- [x] `Reader` (implements `java.lang.Readable`, `Closeable`; `read(CharBuffer)`, `read()`, `read(CharArray)`, `read(CharArray, Int, Int)`)
- [x] `Writer.write(c: Int)`
- [x] `Writer.write(cbuf: CharArray)`
- [x] `Writer.write(cbuf: CharArray, off: Int, len: Int)`
- [x] `Writer.write(str: String)`
- [x] `Writer.flush()`
- [x] `Writer.close()`

### Encoding Bridges (`InputStreamReader` & `OutputStreamWriter`)
- [x] `InputStreamReader(inStream: InputStream)`
- [x] `InputStreamReader(inStream: InputStream, charsetName: String)`
- [x] `InputStreamReader(inStream: InputStream, cs: Charset)`
- [x] `OutputStreamWriter(out: OutputStream)`
- [x] `OutputStreamWriter(out: OutputStream, charsetName: String)`
- [x] `OutputStreamWriter(out: OutputStream, cs: Charset)`

### Buffered & In-Memory Character Streams
- [x] `BufferedReader(inReader: Reader)` (`readLine()`, `lines(): Stream<String>`)
- [x] `BufferedWriter(out: Writer)`
- [x] `StringReader(s: String)`
- [x] `StringWriter()`
- [x] `CharArrayReader(buf: CharArray)`
- [x] `CharArrayWriter()`

### `java.io` Filter, Pushback, Tokenizer & Special Streams
- [x] `FilterInputStream(inStream: InputStream)`
- [x] `FilterOutputStream(out: OutputStream)`
- [x] `FilterReader(inReader: Reader)`
- [x] `FilterWriter(out: Writer)`
- [x] `PushbackInputStream(inStream: InputStream)`
- [x] `PushbackReader(inReader: Reader)`
- [x] `SequenceInputStream(s1: InputStream, s2: InputStream)`
- [x] `LineNumberReader(inReader: Reader)`
- [x] `StreamTokenizer` (`TT_EOF`, `TT_EOL`, `TT_NUMBER`, `TT_WORD`, `nextToken`, `pushBack`, `lineno`, `wordChars`, `whitespaceChars`, `ordinaryChar`, `commentChar`, `quoteChar`, `parseNumbers`, `slashSlashComments`, `slashStarComments`, `lowerCaseMode`)

### `java.io` Piped, Random Access & Object Streams
- [x] `PipedInputStream` / `PipedOutputStream`
- [x] `PipedReader` / `PipedWriter`
- [x] `RandomAccessFile(file: File, mode: String)` (implements `DataOutput`, `DataInput`, `Closeable`; `getChannel()`, `getFD()`)
- [x] `ObjectInputStream` / `ObjectOutputStream` (implements `ObjectInput` / `ObjectOutput`, `ObjectStreamConstants`, stream header & standard type/collection binary serialization)

---

## 2. `java.lang` & `java.lang.reflect` Packages

### `java.lang.System`
- [x] `System.currentTimeMillis(): Long`
- [x] `System.nanoTime(): Long`
- [x] `System.arraycopy(src, srcPos, dest, destPos, length)`
- [x] `System.getProperty(key: String): String?` (`user.dir`, `user.home`, `user.name`, `java.io.tmpdir`, `os.name`, `os.arch`, `os.version`, `file.separator`, `path.separator`, `line.separator`)
- [x] `System.lineSeparator(): String`
- [x] `System.in` / `System.out` / `System.err`
- [x] `System.setIn` / `setOut` / `setErr`

### `java.lang.Process`, `ProcessBuilder` & `Runtime`
- [x] `Process` (`inputStream`, `outputStream`, `errorStream`, `waitFor()`, `waitFor(timeout, unit)`, `exitValue()`, `destroy()`, `destroyForcibly()`, `isAlive`)
- [x] `ProcessBuilder` (`command`, `environment`, `directory`, `redirectErrorStream`, `inheritIO`, `start`)
- [x] `Runtime.getRuntime()` (`exec`, `availableProcessors`, `freeMemory`, `totalMemory`, `maxMemory`, `gc`, `exit`, `halt`)

### `java.lang.Thread`
- [x] `Thread()` / `Thread(target)` / `Thread(name)` / `Thread(target, name)`
- [x] `Thread.State` (`NEW`, `RUNNABLE`, `BLOCKED`, `WAITING`, `TIMED_WAITING`, `TERMINATED`) & `getState()`
- [x] `Thread.UncaughtExceptionHandler` (`uncaughtExceptionHandler`, `defaultUncaughtExceptionHandler`)
- [x] `start()` (Kotlin Native `Worker` 后台执行)
- [x] `run()` / `join()` / `join(millis)` / `join(millis, nanos)`
- [x] `interrupt()` / `isInterrupted()` / `Thread.interrupted()`
- [x] `Thread.currentThread()` (按 OS 线程绑定同一实例)
- [x] `Thread.sleep()` (可中断) / `Thread.yield()` / `Thread.holdsLock(obj)`
- [x] `getName()` / `setName()` / `getId()` / `isAlive()` / `isDaemon()` / `setDaemon()` / `getPriority()` / `setPriority()` (`MIN_PRIORITY`, `NORM_PRIORITY`, `MAX_PRIORITY`)
- [x] `IllegalThreadStateException`

### `java.lang.ThreadLocal` & `InheritableThreadLocal`
- [x] `ThreadLocal<T>` (`get` / `set` / `remove` / `withInitial`，按 `currentThread()` 隔离)
- [x] `InheritableThreadLocal<T>` (`childValue(parentValue)`，子线程自动继承父线程快照)

### `java.lang.Class`, `Void` & `java.lang.reflect.Array`
- [x] `Class<T>` (implements `Serializable`; `getName`, `getSimpleName`, `getCanonicalName`, `isPrimitive`, `isArray`, `isInstance`, `getComponentType`, `cast`, `forName`)
- [x] `Void` (`Void.TYPE`)
- [x] `java.lang.reflect.Array` (`newInstance`, `getLength`, `get*`, `set*` with widening primitive conversions)

### `java.lang.Character` & `Integer`
- [x] `Integer` (`toHexString`, `toOctalString`, `toBinaryString`, `parseInt`, `valueOf`, `highestOneBit`, `lowestOneBit`, `numberOfLeadingZeros`, `numberOfTrailingZeros`, `bitCount`, `rotateLeft`, `rotateRight`, `reverse`, `reverseBytes`, `signum`, `compare`, `sum`, `max`, `min`)
- [x] `Character.isISOControl()`
- [x] `Character.isDigit()`
- [x] `Character.isLetter()`
- [x] `Character.isLetterOrDigit()`
- [x] `Character.isWhitespace()`
- [x] `Character.toLowerCase()` / `toUpperCase()`
- [x] `Character.digit()` / `forDigit()`
- [x] `Character.charCount()` / `toChars()`
- [x] `Character.codePointAt()`

### `java.lang.ref`
- [x] `Reference<T>` (`get()`, `clear()`, `isEnqueued()`, `enqueue()`)
- [x] `ReferenceQueue<T>` (`poll()`, `remove()`, `remove(timeout)`)
- [x] `WeakReference<T>` (wraps Kotlin Native `WeakReference`, supports `ReferenceQueue`)
- [x] `SoftReference<T>` & `PhantomReference<T>`

### `java.lang` Interfaces, StackTrace & Exceptions
- [x] `Readable` (`read(cb: CharBuffer): Int`)
- [x] `StackTraceElement` (`declaringClass`, `methodName`, `fileName`, `lineNumber`, `isNativeMethod`)
- [x] `Object` (`wait` / `notify` / `notifyAll`)
- [x] `ClassNotFoundException`, `NoSuchFieldException`
- [x] `SecurityException`, `IllegalMonitorStateException`, `NegativeArraySizeException`, `ArrayStoreException`, `StringIndexOutOfBoundsException`
- [x] `InterruptedException`
- [x] `Throwable` extensions (`initCause`, `suppressed`, `getSuppressed`)

---

## 3. `java.util` Package

### `java.util.UUID`
- [x] `UUID(mostSigBits: Long, leastSigBits: Long)`
- [x] `UUID.randomUUID(): UUID`
- [x] `UUID.nameUUIDFromBytes(name: ByteArray): UUID`
- [x] `UUID.fromString(name: String): UUID`

### `java.util.Base64`
- [x] `Base64.getEncoder()` / `getUrlEncoder()` / `getMimeEncoder()`
- [x] `Base64.getDecoder()` / `getUrlDecoder()` / `getMimeDecoder()`

### `java.util.Random`
- [x] `Random()` / `Random(seed: Long)`
- [x] `nextInt()` / `nextLong()` / `nextDouble()` / `nextBoolean()`

### `java.util.StringJoiner`
- [x] `StringJoiner(delimiter)` / `StringJoiner(delimiter, prefix, suffix)`
- [x] `add(CharSequence)` / `merge(StringJoiner)` / `length()` / `setEmptyValue(CharSequence)`

### `java.util.StringTokenizer`
- [x] `StringTokenizer(str)` / `StringTokenizer(str, delim)` / `StringTokenizer(str, delim, returnDelims)`
- [x] `hasMoreTokens()` / `nextToken()` / `countTokens()` / `hasMoreElements()` / `nextElement()`

### `java.util.BitSet`
- [x] `BitSet()` / `BitSet(nbits)`
- [x] `get(bitIndex)` / `get(fromIndex, toIndex)`
- [x] `set(bitIndex)` / `set(bitIndex, value)` / `set(fromIndex, toIndex)` / `set(fromIndex, toIndex, value)`
- [x] `clear(bitIndex)` / `clear(fromIndex, toIndex)` / `clear()`
- [x] `flip(bitIndex)` / `flip(fromIndex, toIndex)`
- [x] `and(set)` / `or(set)` / `xor(set)` / `andNot(set)` / `intersects(set)`
- [x] `cardinality()` / `isEmpty` / `length()` / `size()` / `toByteArray()` / `toLongArray()`
- [x] `nextSetBit(fromIndex)` / `nextClearBit(fromIndex)` / `previousSetBit(fromIndex)` / `previousClearBit(fromIndex)`
- [x] `BitSet.valueOf(bytes)` / `BitSet.valueOf(longs)`

### `java.util.Arrays`
- [x] `Arrays.asList(vararg a: T)`
- [x] `Arrays.sort(array)` / `Arrays.sort(array, fromIndex, toIndex)` / `Arrays.sort(array, comparator)` (all primitive and object arrays)
- [x] `Arrays.binarySearch(array, key)` (all primitive and object arrays)
- [x] `Arrays.equals(a, b)` / `Arrays.deepEquals(a, b)`
- [x] `Arrays.fill(array, value)` / `Arrays.fill(array, fromIndex, toIndex, value)`
- [x] `Arrays.copyOf(array, newLength)` / `Arrays.copyOfRange(array, from, to)`
- [x] `Arrays.hashCode(array)` / `Arrays.deepHashCode(array)`
- [x] `Arrays.toString(array)` / `Arrays.deepToString(array)`
- [x] `Arrays.spliterator(array)` / `Arrays.stream(array)` (all primitive and object arrays)

### `java.util.Collections`
- [x] `emptyList()`, `emptySet()`, `emptyMap()`, `singletonList()`, `singleton()`, `singletonMap()`
- [x] `unmodifiableCollection()`, `unmodifiableList()`, `unmodifiableSet()`, `unmodifiableMap()`
- [x] `synchronizedCollection()`, `synchronizedList()`, `synchronizedSet()`, `synchronizedMap()`
- [x] `sort(list)`, `reverse(list)`, `shuffle(list)`, `swap(list, i, j)`, `fill(list, obj)`
- [x] `min(coll)`, `max(coll)`, `nCopies(n, obj)`, `reverseOrder()`, `frequency(c, o)`, `disjoint(c1, c2)`, `addAll(c, ...elements)`
- [x] `enumeration(c)`, `list(e)`, `newSetFromMap(map)`

### `java.util.Optional`
- [x] `Optional<T>` (`of`, `ofNullable`, `empty`, `get`, `isPresent`, `ifPresent`, `filter`, `map`, `flatMap`, `orElse`, `orElseGet`, `orElseThrow`)
- [x] `OptionalInt` / `OptionalLong` / `OptionalDouble`

### `java.util.Properties`
- [x] `Properties()` / `Properties(defaults)` (extends `Hashtable<Any, Any>`, implements `MutableMap<Any, Any>`, `Cloneable`, `Serializable`)
- [x] `getProperty()` / `setProperty()` / `stringPropertyNames()` / `propertyNames()` / `clone()`
- [x] `load(InputStream)` / `load(Reader)` / `store(OutputStream)` / `store(Writer)`

### `java.util.Date`
- [x] `Date()` / `Date(time: Long)`
- [x] `getTime()` / `setTime()` / `toInstant()` / `Date.from(instant)`

### `java.util.Calendar` & `GregorianCalendar`
- [x] `Calendar` / `GregorianCalendar` (fields, get, set, add, getTime, setTime)

### `java.util.TimeZone`
- [x] `TimeZone.getDefault()`, `setDefault(zone)`
- [x] `TimeZone.getTimeZone(id)` (UTC, GMT, offsets, standard aliases)
- [x] `getID()`, `getRawOffset()`, `getOffset(date)`, `getDisplayName()`, `getAvailableIDs()`

### `java.util.Locale`
- [x] `Locale(language)` / `Locale(language, country)` / `Locale(language, country, variant)`
- [x] `Locale.US`, `UK`, `FRANCE`, `GERMANY`, `ITALY`, `JAPAN`, `KOREA`, `CHINA`, `SIMPLIFIED_CHINESE`, `TRADITIONAL_CHINESE`, `ENGLISH`, `ROOT`, `DEFAULT`
- [x] `getLanguage()`, `getCountry()`, `getVariant()`, `getDisplayName()`, `getDisplayLanguage()`, `getDisplayCountry()`, `toLanguageTag()`, `forLanguageTag()`, `getDefault()`, `setDefault()`

### `java.util.Currency`
- [x] `Currency.getInstance(code)` (`USD`/`EUR`/`CNY`/`JPY` 等常见 ISO 4217)
- [x] `getCurrencyCode()` / `getSymbol()` / `getDefaultFractionDigits()`

### `java.util.Formatter`
- [x] `Formatter()` / `Formatter(Appendable)` (`format` 支持 `%s` `%d` `%x` `%f` `%n` `%%`，`close`/`flush`)
- [x] `Formattable` & `FormattableFlags`
- [x] `FormatterClosedException` & `IllegalFormatException` (`DuplicateFormatFlagsException`, `FormatFlagsConversionMismatchException`, `IllegalFormatCodePointException`, `IllegalFormatConversionException`, `IllegalFormatFlagsException`, `IllegalFormatPrecisionException`, `IllegalFormatWidthException`, `MissingFormatArgumentException`, `MissingFormatWidthException`, `UnknownFormatConversionException`)

### `java.util.Timer` & `TimerTask`
- [x] `Timer.schedule(task, delay)` / `schedule(task, delay, period)` / `cancel()`

### `java.util.Scanner`
- [x] `Scanner(String)` / `Scanner(InputStream)` / `Scanner(File)` / `Scanner(Path)` / `Scanner(Readable)`
- [x] `hasNext()`, `next()`, `hasNextLine()`, `nextLine()`, `hasNextInt()`, `nextInt()`, `hasNextLong()`, `nextLong()`, `hasNextDouble()`, `nextDouble()`, `hasNextBoolean()`, `nextBoolean()`
- [x] `useDelimiter(Pattern/String)`, `delimiter()`, `close()`
- [x] `InputMismatchException`

### `java.lang.Cloneable`
- [x] `java.lang.Cloneable` & `CloneNotSupportedException`

### `java.util.Deque`, `TreeMap`, `TreeSet`, Legacy Collections & Spliterators
- [x] `Queue<E>`, `AbstractQueue<E>`, `Deque<E>`
- [x] `ArrayDeque<E>` (`addFirst`, `addLast`, `offerFirst`, `offerLast`, `removeFirst`, `removeLast`, `pollFirst`, `pollLast`, `getFirst`, `getLast`, `peekFirst`, `peekLast`, `pop`, `push`, `descendingIterator`, `clone`)
- [x] `AbstractSequentialList<E>` & `LinkedList<E>` (implements `MutableList<E>`, `Deque<E>`, `Cloneable`, `Serializable`)
- [x] `Dictionary<K, V>`, `Hashtable<K, V>`, `Vector<E>`, `Stack<E>` (`push`, `pop`, `peek`, `empty`, `search`)
- [x] `PriorityQueue<E>` (binary min-heap, natural ordering & custom `Comparator`, `offer`, `poll`, `peek`, `remove`)
- [x] `SortedMap<K, V>`, `NavigableMap<K, V>`, `TreeMap<K, V>` (`lowerEntry`, `floorEntry`, `ceilingEntry`, `higherEntry`, `firstEntry`, `lastEntry`, `pollFirstEntry`, `pollLastEntry`, `descendingMap`, `subMap`, `headMap`, `tailMap`)
- [x] `SortedSet<E>`, `NavigableSet<E>`, `TreeSet<E>`
- [x] `LinkedHashMap<K, V>` (including access-order constructor)
- [x] `IdentityHashMap<K, V>` (reference-equality `===` keys and values)
- [x] `WeakHashMap<K, V>`
- [x] `EnumMap<K, V>` & `EnumSet<E>` (natural enum ordinal ordering, `noneOf`, `of`, `copyOf`, `clone`)
- [x] `Objects` (`equals`, `deepEquals`, `hashCode`, `hash`, `toString`, `compare`, `requireNonNull`, `isNull`, `nonNull`)
- [x] `EventListener`, `EventListenerProxy<T>`, `EventObject`, `Observer`, `Observable`
- [x] `PrimitiveIterator<T, T_CONS>`, `PrimitiveIterator.OfInt`, `PrimitiveIterator.OfLong`, `PrimitiveIterator.OfDouble`
- [x] `Spliterator<T>`, `Spliterator.OfPrimitive`, `Spliterator.OfInt`, `Spliterator.OfLong`, `Spliterator.OfDouble`, `Spliterators`
- [x] `IntSummaryStatistics`, `LongSummaryStatistics`, `DoubleSummaryStatistics`
- [x] `EmptyStackException`, `NoSuchElementException`, `ConcurrentModificationException`, `TooManyListenersException`, `MissingResourceException`, `InvalidPropertiesFormatException`, `IllformedLocaleException`

### `java.util.stream`
- [x] `BaseStream<T, S>`, `Stream<T>`, `Stream.Builder<T>` (`filter`, `map`, `mapToInt`/`Long`/`Double`, `flatMap`, `distinct`, `sorted`, `peek`, `limit`, `skip`, `forEach`, `toArray`, `reduce`, `collect`, `min`, `max`, `count`, `anyMatch`, `allMatch`, `noneMatch`, `findFirst`, `findAny`, `concat`, `iterate`, `generate`)
- [x] `IntStream` & `IntStream.Builder`, `LongStream` & `LongStream.Builder`, `DoubleStream` & `DoubleStream.Builder` (`range`, `rangeClosed`, `sum`, `min`, `max`, `average`, `summaryStatistics`, `boxed`, `asLongStream`, `asDoubleStream`)
- [x] `Collector<T, A, R>`, `Collector.Characteristics`, `Collectors` (`toList`, `toSet`, `toCollection`, `toMap`, `joining`, `groupingBy`, `partitioningBy`, `counting`, `summingInt`/`Long`/`Double`, `averagingInt`/`Long`/`Double`, `summarizingInt`/`Long`/`Double`, `reducing`, `mapping`, `collectingAndThen`, `minBy`, `maxBy`)
- [x] `StreamSupport` (`stream`, `intStream`, `longStream`, `doubleStream`)

### `java.util.regex`
- [x] `Pattern` (`compile`, `matcher`, `matches`, `split`, `quote`)
- [x] `Matcher` (`matches`, `find`, `lookingAt`, `group`, `start`, `end`, `replaceAll`, `replaceFirst`, `appendReplacement`, `appendTail`, `region`)
- [x] `MatchResult` & `PatternSyntaxException`

### `java.util.logging`
- [x] `Level` (`ALL`, `FINEST`, `FINER`, `FINE`, `CONFIG`, `INFO`, `WARNING`, `SEVERE`, `OFF`, `parse`)
- [x] `Logger` (`getLogger`, `getAnonymousLogger`, `getGlobal`, `log`, `info`, `warning`, `severe`, `fine`, `isLoggable`, `setLevel`, `getLevel`)
- [x] `LogRecord` (`level`, `message`, `loggerName`, `millis`, `thrown`)
- [x] `Handler`, `Formatter`, `SimpleFormatter`

### `java.util.concurrent.atomic`
- [x] `AtomicInteger(initialValue: Int)` (`get`, `set`, `lazySet`, `getAndSet`, `compareAndSet`, `weakCompareAndSet`, `getAndUpdate`, `updateAndGet`, `getAndAccumulate`, `accumulateAndGet`, `addAndGet`, `getAndAdd`)
- [x] `AtomicLong(initialValue: Long)` (`get`, `set`, `lazySet`, `getAndSet`, `compareAndSet`, `weakCompareAndSet`, `getAndUpdate`, `updateAndGet`, `getAndAccumulate`, `accumulateAndGet`, `addAndGet`, `getAndAdd`)
- [x] `AtomicBoolean(initialValue: Boolean)` (`get`, `set`, `lazySet`, `getAndSet`, `compareAndSet`, `weakCompareAndSet`)
- [x] `AtomicReference<V>(initialValue: V)` (`get`, `set`, `lazySet`, `getAndSet`, `compareAndSet`, `weakCompareAndSet`, `getAndUpdate`, `updateAndGet`, `getAndAccumulate`, `accumulateAndGet`)
- [x] `AtomicIntegerArray`, `AtomicLongArray`, `AtomicReferenceArray<E>`
- [x] `AtomicStampedReference<V>` & `AtomicMarkableReference<V>`
- [x] `LongAdder` / `DoubleAdder` (`add`, `increment`, `decrement`, `sum`, `reset`, `sumThenReset`)

### `java.util.concurrent`
- [x] `TimeUnit` (`NANOSECONDS` → `DAYS`, conversion & unit helpers)
- [x] `CountDownLatch(count: Int)` (`await`, `countDown`, `getCount`)
- [x] `Semaphore(permits, fair)` (`acquire`, `release`, `tryAcquire`, `availablePermits`, `drainPermits`)
- [x] `CyclicBarrier(parties, action)` (`await`, `reset`, `isBroken`, `getNumberWaiting`)
- [x] `Phaser` (`register`, `bulkRegister`, `arrive`, `arriveAndDeregister`, `arriveAndAwaitAdvance`, `awaitAdvance`, `forceTermination`)
- [x] `Exchanger<V>` (`exchange(x)`, `exchange(x, timeout, unit)`)
- [x] `Callable<V>` (`call`) & `ThreadFactory` (`newThread`)
- [x] `Future<V>` (`cancel`, `isCancelled`, `isDone`, `get`)
- [x] `RunnableFuture<V>` & `FutureTask<V>`
- [x] `CompletableFuture<T>` & `CompletionStage<T>` (`completedFuture`, `supplyAsync`, `runAsync`, `thenApply`, `thenAccept`, `thenRun`, `thenCombine`, `thenCompose`, `exceptionally`, `handle`, `whenComplete`, `allOf`, `anyOf`, `join`, `getNow`)
- [x] `Executor`, `ExecutorService`, `AbstractExecutorService`, `ThreadPoolExecutor`, `ScheduledExecutorService`, `ScheduledThreadPoolExecutor`, `Executors` (`newFixedThreadPool`, `newCachedThreadPool`, `newSingleThreadExecutor`, `newScheduledThreadPool`, `newWorkStealingPool`, `unconfigurableExecutorService`, `unconfigurableScheduledExecutorService`)
- [x] `ForkJoinTask<V>`, `RecursiveAction`, `RecursiveTask<V>`, `CountedCompleter<T>` (`fork`, `join`, `invoke`, `invokeAll`, `complete`, `quietlyComplete`, `tryComplete`, `propagateCompletion`)
- [x] `ForkJoinPool` & `ForkJoinPool.ManagedBlocker` (`commonPool()`, `invoke`, `submit`, `execute`, `managedBlock`, `getCommonPoolParallelism`, `isQuiescent`, `awaitQuiescence`)
- [x] `CompletionService<V>` & `ExecutorCompletionService<V>` (`submit`, `take`, `poll`)
- [x] `Delayed` & `ScheduledFuture<V>` (`schedule`, `scheduleAtFixedRate`, `scheduleWithFixedDelay`)
- [x] `ThreadLocalRandom` (`current()`, bounded & range `nextInt`, `nextLong`, `nextDouble`)
- [x] `BlockingQueue<E>`, `BlockingDeque<E>`, `TransferQueue<E>`, `LinkedTransferQueue<E>`, `LinkedBlockingQueue<E>`, `LinkedBlockingDeque<E>`, `ArrayBlockingQueue<E>`, `PriorityBlockingQueue<E>`, `DelayQueue<E>`, `SynchronousQueue<E>`
- [x] `ConcurrentHashMap<K, V>`, `ConcurrentSkipListMap<K, V>`, `ConcurrentSkipListSet<E>`, `CopyOnWriteArrayList<E>`, `CopyOnWriteArraySet<E>`, `ConcurrentLinkedQueue<E>`, `ConcurrentLinkedDeque<E>`
- [x] `ExecutionException`, `TimeoutException`, `CancellationException`, `CompletionException`, `RejectedExecutionException`, `BrokenBarrierException`

### `java.util.function`
- [x] `Function<T, R>`, `BiFunction<T, U, R>`, `UnaryOperator<T>`, `BinaryOperator<T>`
- [x] `Consumer<T>`, `BiConsumer<T, U>`, `ObjIntConsumer<T>`, `ObjLongConsumer<T>`, `ObjDoubleConsumer<T>`
- [x] `Predicate<T>`, `BiPredicate<T, U>`, `Supplier<T>`, `BooleanSupplier`
- [x] `IntSupplier`, `LongSupplier`, `DoubleSupplier`, `IntConsumer`, `LongConsumer`, `DoubleConsumer`
- [x] `IntPredicate`, `LongPredicate`, `DoublePredicate`, `IntFunction<R>`, `LongFunction<R>`, `DoubleFunction<R>`
- [x] `ToIntFunction<T>`, `ToLongFunction<T>`, `ToDoubleFunction<T>`, `ToIntBiFunction<T, U>`, `ToLongBiFunction<T, U>`, `ToDoubleBiFunction<T, U>`
- [x] `IntUnaryOperator`, `LongUnaryOperator`, `DoubleUnaryOperator`, `IntBinaryOperator`, `LongBinaryOperator`, `DoubleBinaryOperator`
- [x] `IntToLongFunction`, `IntToDoubleFunction`, `LongToIntFunction`, `LongToDoubleFunction`, `DoubleToIntFunction`, `DoubleToLongFunction`

### `java.util.concurrent.locks`
- [x] `Lock` (`lock`, `unlock`, `tryLock`, `newCondition`) & `ReadWriteLock` (`readLock`, `writeLock`)
- [x] `Condition` (`await`, `signal`, `signalAll`)
- [x] `ReentrantLock` & `ReentrantReadWriteLock` (`ReadLock`, `WriteLock`)
- [x] `LockSupport` (`park`, `parkNanos`, `parkUntil`, `unpark`, `getBlocker`)
- [x] `StampedLock` (`writeLock`, `readLock`, `tryOptimisticRead`, `validate`, `unlockWrite`, `unlockRead`, `unlock`, `tryConvertToWriteLock`, `tryConvertToReadLock`, `tryConvertToOptimisticRead`, `asReadLock`, `asWriteLock`, `asReadWriteLock`)

### `java.util.zip`
- [x] `Checksum` interface
- [x] `CRC32` (`update`, `getValue`, `reset`)
- [x] `Adler32` (`update`, `getValue`, `reset`)
- [x] `CheckedInputStream` / `CheckedOutputStream` (`checksum`)
- [x] `ZipException`, `DataFormatException`
- [x] `ZipEntry` (`name`, `size`, `compressedSize`, `time`, `crc`, `method`, `comment`, `isDirectory`)
- [x] `Deflater` / `Inflater`（`platform.zlib`：`setInput`、`deflate`/`inflate`、`finish`/`reset`/`end`）
- [x] `DeflaterInputStream` / `DeflaterOutputStream` / `InflaterInputStream` / `InflaterOutputStream`
- [x] `GZIPInputStream` / `GZIPOutputStream`（真实 GZIP 头/CRC/ISIZE；不拼接多 member）
- [x] `ZipInputStream` / `ZipOutputStream`（STORED 与 DEFLATED）
- [x] `ZipFile` (`getEntry`, `getInputStream`, `entries`, `size`, `close`；无 ZIP64/加密)

### `java.util.jar`
- [x] `JarFile` / `JarEntry` / `Manifest` / `Attributes` (`Attributes.Name`) / `JarInputStream` / `JarOutputStream` / `JarException`

---

## 4. `java.net` Package

### `java.net.URI` & `java.net.URL`
- [x] `URI(str: String)`
- [x] `URI.create(str: String): URI`
- [x] `URI.resolve(str: String): URI`
- [x] `URI.toURL(): URL`
- [x] `URL(spec: String)`
- [x] `URL.toURI(): URI`

### `java.net.URLEncoder` & `java.net.URLDecoder`
- [x] `URLEncoder.encode(s: String, enc: String): String`
- [x] `URLDecoder.decode(s: String, enc: String): String`

### `java.net` Socket & Networking
- [x] `InetAddress`, `Inet4Address`, `Inet6Address` (`getByName`, `getAllByName`, `getHostAddress`)
- [x] `SocketAddress` & `InetSocketAddress(host: String, port: Int)`
- [x] `NetworkInterface` & `InterfaceAddress` (`getByName`, `getByIndex`, `getByInetAddress`, `getNetworkInterfaces`, `isUp`, `isLoopback`, `hardwareAddress`, `mtu`, `inetAddresses`, `interfaceAddresses`)
- [x] `SocketOption<T>`, `StandardSocketOptions`, `SocketOptions`
- [x] `Socket()` / `Socket.connect()` / `Socket.close()`
- [x] `Socket.getInputStream()` / `Socket.getOutputStream()`
- [x] `ServerSocket(port: Int)` / `ServerSocket.accept()` / `ServerSocket.close()`
- [x] `DatagramPacket` & `DatagramSocket` (UDP `send`, `receive`, `bind`, `connect`, `soTimeout`, `localPort`, `close`)
- [x] `Proxy(type, sa)` / `Proxy.NO_PROXY`
- [x] `SocketFactory.getDefault()`
- [x] `ServerSocketFactory.getDefault()`
- [x] `ProxySelector.getDefault()` / `setDefault()`
- [x] `Authenticator` & `PasswordAuthentication` (`setDefault()`, `requestPasswordAuthentication()`)

### `java.net` Exceptions
- [x] `SocketException`
- [x] `BindException`
- [x] `ConnectException`
- [x] `NoRouteToHostException`
- [x] `PortUnreachableException`
- [x] `UnknownHostException`
- [x] `SocketTimeoutException`
- [x] `ProtocolException`
- [x] `HttpRetryException`
- [x] `MalformedURLException`
- [x] `URISyntaxException`
- [x] `UnknownServiceException`

### `java.net` HTTP, Cookies & Domain Name
- [x] `HttpURLConnection` (all standard HTTP 1xx-5xx status code constants + abstract class)
- [x] `URLConnection` (abstract base class)
- [x] `CookieHandler`, `CookieManager`, `CookiePolicy`, `CookieStore`, `HttpCookie`
- [x] `IDN.toASCII()` / `IDN.toUnicode()` (RFC 3492 Punycode encoder / decoder)

---

## 5. `java.nio.charset` Package

### `java.nio.charset.Charset` & `StandardCharsets`
- [x] `Charset.name()` / `displayName()` / `forName()` / `defaultCharset()`
- [x] `StandardCharsets.UTF_8` / `US_ASCII` / `ISO_8859_1`
- [x] `StandardCharsets.UTF_16` / `UTF_16BE` / `UTF_16LE`

---

## 6. `java.security` & `javax.crypto` Packages

### `java.security` Core
- [x] `MessageDigest.getInstance(algorithm)` (MD5, SHA-1, SHA-256, SHA-384, SHA-512)
- [x] `SecureRandom` (platform random: `/dev/urandom`, `BCryptGenRandom`)
- [x] `KeyPairGenerator` (`RSA`, `EC` `secp256r1` / P-256)
- [x] `KeyFactory` (`RSA`, `EC`: X.509 `SubjectPublicKeyInfo` & PKCS#8 `PrivateKeyInfo` DER codec, `RSAPublicKeySpec`, `RSAPrivateKeySpec`)
- [x] `Signature` (`MD5withRSA`, `SHA1withRSA`, `SHA256withRSA`, `SHA384withRSA`, `SHA512withRSA`, `NONEwithECDSA`, `SHA256withECDSA`)
- [x] `GeneralSecurityException`, `NoSuchAlgorithmException`, `DigestException`, `KeyException`, `InvalidKeyException`, `InvalidAlgorithmParameterException`, `SignatureException`, `InvalidParameterException`, `InvalidKeySpecException`, `InvalidParameterSpecException`, `KeyStoreException`, `KeyManagementException`

### `java.security` Interfaces, Specs & KeyStore
- [x] `KeyStore.getInstance(type)` / `getDefaultType()` / `load(stream, password)` / `setCertificateEntry` / `getCertificate` / `aliases` / `containsAlias` / `size`
- [x] `Principal` (`getName`, `name`)
- [x] `Key`, `PublicKey`, `PrivateKey`, `KeyPair` (`getAlgorithm`, `getFormat`, `getEncoded`, `encoded`)
- [x] `java.security.interfaces.RSAKey`, `RSAPublicKey`, `RSAPrivateKey`
- [x] `java.security.spec.KeySpec`, `EncodedKeySpec`, `X509EncodedKeySpec`, `PKCS8EncodedKeySpec`, `RSAPublicKeySpec`, `RSAPrivateKeySpec`, `ECGenParameterSpec`, `AlgorithmParameterSpec`
- [x] `java.security.cert.Certificate`, `X509Certificate`, `CertificateFactory`
- [x] `java.security.cert.CertificateException`, `CertificateEncodingException`, `CertificateParsingException`, `CertificateExpiredException`, `CertificateNotYetValidException`

### `javax.crypto` & `javax.crypto.spec`
- [x] `SecretKey` & `SecretKeySpec` (`algorithm`, `format`, `encoded`, `equals`, `hashCode`)
- [x] `IvParameterSpec` (`iv`, `getIV()`) & `GCMParameterSpec` (`tLen`, `iv`)
- [x] `PBEKeySpec` & `SecretKeyFactory` (`PBKDF2WithHmacSHA1`, `PBKDF2WithHmacSHA256`)
- [x] `KeyGenerator` (`AES`, `HmacMD5`, `HmacSHA1`, `HmacSHA256`, `HmacSHA384`, `HmacSHA512`)
- [x] `Mac` (`HmacMD5`, `HmacSHA1`, `HmacSHA256`, `HmacSHA384`, `HmacSHA512`: `init`, `update`, `doFinal`, `reset`, `clone`)
- [x] `Cipher` (`AES/ECB/*`, `AES/CBC/*`, `AES/CTR/*`, `AES/GCM/NoPadding` with GHASH & AAD, `RSA/ECB/PKCS1Padding`)
- [x] `ShortBufferException`, `NoSuchPaddingException`, `IllegalBlockSizeException`, `BadPaddingException`, `AEADBadTagException`

---

## 7. `javax.net.ssl` Package

- [x] `SSLContext.getInstance(protocol): SSLContext`
- [x] `SSLSocketFactory` / `SSLSocketFactory.getDefault()`
- [x] `SSLSocket` (abstract + native socket implementation)
- [x] `HttpsURLConnection` (`getCipherSuite`, `getServerCertificates`, `getLocalCertificates`, `getPeerPrincipal`, `setSSLSocketFactory`, `setHostnameVerifier`)
- [x] `KeyManager` & `X509TrustManager` interfaces
- [x] `HostnameVerifier` interface
- [x] `SSLSession` interface + `ExtendedSSLSession`
- [x] `SNIHostName`
- [x] `SSLException`, `SSLHandshakeException`, `SSLPeerUnverifiedException`, `SSLProtocolException`
- [x] `TrustManagerFactory.getInstance()` / `getDefaultAlgorithm()` / `init(ks)` / `trustManagers`
- [x] `javax.net.SocketFactory` (via `java.net.SocketFactory`)

### `javax.security.auth.x500`
- [x] `X500Principal(name: String)` (`getName()`, `name`, `equals`, `hashCode`, `toString`)

---

## 8. `java.text` Package

- [x] `Format` (`format`, `parseObject`) & `ParseException`
- [x] `FieldPosition` (`field`, `beginIndex`, `endIndex`)
- [x] `ParsePosition` (`index`, `errorIndex`)
- [x] `DateFormat` (`parse`, `format`, `timeZone`, `isLenient`)
- [x] `DateFormatSymbols` (`months`, `shortMonths`, `weekdays`, `shortWeekdays`, `amPmStrings`, `eras`, `zoneStrings`)
- [x] `SimpleDateFormat(pattern, locale)` / `SimpleDateFormat(pattern, formatSymbols)` (`getDateFormatSymbols`, `setDateFormatSymbols`, `toPattern`, `applyPattern`, 支持标准日期时间模式格式化与解析)
- [x] `NumberFormat` (`getInstance`, `getIntegerInstance`, `getCurrencyInstance`, `getPercentInstance`, `format`, `parse`, `groupingUsed`)
- [x] `DecimalFormatSymbols` (`decimalSeparator`, `groupingSeparator`, `minusSign`, `percent`, `perMill`, `zeroDigit`, `infinity`, `naN`, `currencySymbol`, `internationalCurrencySymbol`)
- [x] `DecimalFormat(pattern)` / `DecimalFormat(pattern, symbols)` (`applyPattern`, `toPattern`, `getDecimalFormatSymbols`, `setDecimalFormatSymbols`, `format`, `parse`)
- [x] `ChoiceFormat(pattern)` / `ChoiceFormat(limits, formats)` (`nextDouble`, `previousDouble`, `setChoices`, `toPattern`, `applyPattern`, `format`, `parse`)
- [x] `MessageFormat` (`format("{0}", args)`，`{0} {1}` 占位；缺参保留 `{n}`)
- [x] `Collator` & `CollationKey` (`PRIMARY`, `SECONDARY`, `TERTIARY`, `IDENTICAL`, `NO_DECOMPOSITION`, `CANONICAL_DECOMPOSITION`, `FULL_DECOMPOSITION`, `compare`, `equals`, `getCollationKey`, `toByteArray`)
- [x] `CharacterIterator` & `StringCharacterIterator` (`first`, `last`, `current`, `next`, `previous`, `setIndex`, `beginIndex`, `endIndex`, `index`, `clone`)
- [x] `BreakIterator` (`getCharacterInstance`, `getWordInstance`, `getLineInstance`, `getSentenceInstance`, `first`, `last`, `next`, `previous`, `following`, `preceding`, `isBoundary`)
- [x] `Bidi` (`DIRECTION_LEFT_TO_RIGHT`, `DIRECTION_RIGHT_TO_LEFT`, `DIRECTION_DEFAULT_LEFT_TO_RIGHT`, `DIRECTION_DEFAULT_RIGHT_TO_LEFT`, `isLeftToRight`, `isRightToLeft`, `isMixed`, `baseIsLeftToRight`, `getRunCount`, `getRunStart`, `getRunLimit`, `getRunLevel`, `requiresBidi`, `reorderVisually`)
- [x] `Normalizer.normalize` / `isNormalized` (`NFC`, `NFD`, `NFKC`, `NFKD` 规范分解与组合、变音符 CCC 排序、韩语音节算法分解与组合、全角/连字兼容映射)

---

## 9. `java.time` & `java.time.temporal` Packages

- [x] `Duration` (`ofDays`, `ofHours`, `ofMinutes`, `ofMillis`, `ofSeconds`, `ofNanos`, `toDays`, `toHours`, `toMinutes`, `toMillis`, `toNanos`, `plus*`, `minus*`, `multipliedBy`, `dividedBy`, `abs`, `negated`, `between`)
- [x] `Instant` (`now`, `ofEpochMilli`, `ofEpochSecond`, `toEpochMilli`, `plus*`, `minus*`, `truncatedTo`, `until`, `atOffset`, `atZone`, `parse`, `from`)
- [x] `LocalDate` (`of`, `now`, `ofEpochDay`, `ofYearDay`, `with*`, `plus*`/`minus*`, `until`, `toEpochDay`, `atTime`, `atStartOfDay`, `parse`/`format`)
- [x] `LocalTime` (`of`, `now`, `toSecondOfDay`/`toNanoOfDay`, `with*`, `truncatedTo`, `plus*`/`minus*`, `until`, `atDate`, `atOffset`, ISO parse)
- [x] `LocalDateTime` (`of(date, time)`, `of(y,m,d,h,min,s)`, `with*`, `truncatedTo`, `plus*`/`minus*`, `until`, `atZone`, `atOffset`, `toEpochSecond`, ISO parse)
- [x] `ZoneOffset` (`UTC`, `MIN`, `MAX`, `of(String)`, `ofHours`, `ofHoursMinutes`, `ofTotalSeconds`, `getTotalSeconds`, `id`)
- [x] `ZoneId` (`of("UTC"/"Z"/"GMT"/offset)`, `systemDefault()`, `getId()`, `getAvailableZoneIds()` — 无完整 TZDB/DST)
- [x] `Period` (`of`, `ofWeeks`, `ofDays`/`ofMonths`/`ofYears`, `with*`, `plus*`, `minus*`, `multipliedBy`, `negated`, `normalized`, `toTotalMonths`, `between`)
- [x] `OffsetDateTime` (`of(ldt, offset)`, `now`, `withOffsetSameInstant`, `withOffsetSameLocal`, `truncatedTo`, `plus*`/`minus*`, `until`, `toInstant`, `toLocalDateTime`, `toOffsetTime`, `toEpochSecond`)
- [x] `ZonedDateTime` (`of(ldt, zone)`, `now`, `withZoneSameInstant`, `withZoneSameLocal`, `truncatedTo`, `plus*`/`minus*`, `until`, `toInstant`, `toOffsetDateTime`)
- [x] `DayOfWeek` (`MONDAY`..`SUNDAY`, `of`, `from`, `plus`, `minus`, `adjustInto`)
- [x] `Month` (`JANUARY`..`DECEMBER`, `of`, `from`, `plus`, `minus`, `length`, `minLength`, `maxLength`, `firstDayOfYear`)
- [x] `Year`, `YearMonth`, `MonthDay`, `OffsetTime` (`of`, `now`, `parse`, `from`, `plus*`/`minus*`, `until`, `adjustInto`)
- [x] `Clock` (`systemUTC`, `systemDefaultZone`, `system`, `fixed`, `offset`, `tick`, `tickSeconds`, `tickMinutes`, `millis`, `instant`, `withZone`)
- [x] `Temporal`, `TemporalAccessor`, `TemporalAmount`, `TemporalAdjuster`, `TemporalAdjusters` (`firstDayOfMonth`, `lastDayOfMonth`, `firstDayOfNextMonth`, `firstDayOfYear`, `lastDayOfYear`, `firstDayOfNextYear`, `firstInMonth`, `lastInMonth`, `dayOfWeekInMonth`, `next`, `nextOrSame`, `previous`, `previousOrSame`)
- [x] `TemporalField`, `ChronoField`, `TemporalUnit`, `ChronoUnit`, `ValueRange`, `UnsupportedTemporalTypeException`
- [x] `DateTimeFormatter` (`ISO_LOCAL_DATE`/`TIME`/`DATE_TIME`, `ISO_INSTANT`, `ofPattern` 支持 `y M d H m s`)
- [x] `DateTimeException` / `DateTimeParseException`

---

## 10. `java.math` Package

- [x] `BigInteger` (`add`, `subtract`, `multiply`, `divide`, `remainder`, `pow`, `gcd`, `mod`, `modPow`, `modInverse`, `isProbablePrime`, `probablePrime`, `nextProbablePrime`, bitwise ops, radix conversion)
- [x] `BigDecimal` (`add`, `subtract`, `multiply`, `divide`, `pow`, `scale`, `precision`, `stripTrailingZeros`, `toPlainString`)
- [x] `RoundingMode` (`UP`, `DOWN`, `CEILING`, `FLOOR`, `HALF_UP`, `HALF_DOWN`, `HALF_EVEN`, `UNNECESSARY`)
- [x] `MathContext` (`UNLIMITED`, `DECIMAL32`, `DECIMAL64`, `DECIMAL128`)
- [x] `BigInteger` & `BigDecimal` operator overloads in `package kotlin` (`+`, `-`, `*`, `/`, `%`, `unaryMinus`, `shl`, `shr`, `and`, `or`, `xor`, `inv`)

---

## 11. `java.nio` Package

### `java.nio` Buffers
- [x] `Buffer` / `ByteBuffer` / `ByteOrder`
- [x] `CharBuffer` / `IntBuffer` / `LongBuffer` / `ShortBuffer` / `FloatBuffer` / `DoubleBuffer` (`allocate`/`wrap`，相对+绝对 get/put，`asReadOnlyBuffer`/`duplicate`/`slice`)
- [x] `ByteBuffer.asCharBuffer` / `asIntBuffer` / `asLongBuffer` / `asShortBuffer` / `asFloatBuffer` / `asDoubleBuffer`
- [x] `ByteBuffer` 相对/绝对 `get/put` Char/Short/Int/Long/Float/Double
- [x] `BufferOverflowException` / `BufferUnderflowException` / `ReadOnlyBufferException`

### `java.nio.charset`
- [x] `Charset` (`forName`, `defaultCharset`, `name`, `displayName`, `isRegistered`, `compareTo`, `newEncoder`/`newDecoder`, `encode`/`decode`)
- [x] `StandardCharsets` (`US_ASCII`, `ISO_8859_1`, `UTF_8`, `UTF_16BE`, `UTF_16LE`, `UTF_16`)
- [x] `CharsetEncoder` / `CharsetDecoder` / `CoderResult` / `CodingErrorAction`
- [x] `CharacterCodingException` / `MalformedInputException` / `UnmappableCharacterException` / `UnsupportedCharsetException` / `IllegalCharsetNameException`

### `java.nio.channels`
- [x] `Channel` / `InterruptibleChannel` / `ReadableByteChannel` / `WritableByteChannel` / `ByteChannel` / `SeekableByteChannel` / `ScatteringByteChannel` / `GatheringByteChannel` / `NetworkChannel` / `Channels`
- [x] `SelectableChannel`, `SelectionKey` (`OP_READ`, `OP_WRITE`, `OP_CONNECT`, `OP_ACCEPT`, `attachment`, `interestOps`, `readyOps`), `Selector` (`open`, `select`, `selectNow`, `wakeup`, `keys`, `selectedKeys`)
- [x] `SocketChannel`, `ServerSocketChannel`, `DatagramChannel` (`open`, `bind`, `connect`, `finishConnect`, `accept`, `send`, `receive`, `read`, `write`, `configureBlocking`, `register`, `setOption`, `getOption`, `supportedOptions`)
- [x] `FileChannel` (`open(Path, options)`, sequential & positional `read`/`write`, scatter/gather `read(dsts)`/`write(srcs)`, `position`, `size`, `truncate`, `force`, `transferTo`, `transferFrom`, `lock`, `tryLock`, `close`)
- [x] `FileLock` & `OverlappingFileLockException`
- [x] `Pipe`, `Pipe.SourceChannel`, `Pipe.SinkChannel` (extends `SelectableChannel`, `Pipe.open()`)
- [x] `ClosedChannelException`, `NonReadableChannelException`, `NonWritableChannelException`, `AsynchronousCloseException`, `ClosedByInterruptException`, `AlreadyBoundException`, `AlreadyConnectedException`, `ConnectionPendingException`, `NoConnectionPendingException`, `NotYetBoundException`, `NotYetConnectedException`, `UnresolvedAddressException`, `UnsupportedAddressTypeException`, `CancelledKeyException`, `ClosedSelectorException`, `IllegalBlockingModeException`, `IllegalSelectorException`

### `java.nio.file`
- [x] `Path` (`isAbsolute`, `getRoot`, `getFileName`, `getParent`, `getNameCount`, `getName`, `subpath`, `startsWith`, `endsWith`, `normalize`, `resolve`, `resolveSibling`, `relativize`, `toUri`, `toAbsolutePath`, `toRealPath`, `toFile`)
- [x] `Paths` (`get(first, ...more)`, `get(uri)`)
- [x] `Files` (`exists`, `notExists`, `isDirectory`, `isRegularFile`, `isReadable`, `isWritable`, `isExecutable`, `isSameFile`, `isHidden`, `size`, `createFile`, `createDirectory`, `createDirectories`, `createTempFile`, `createTempDirectory`, `newDirectoryStream`, `delete`, `deleteIfExists`, `copy`, `move`, `newInputStream`, `newOutputStream`, `newBufferedReader`, `newBufferedWriter`, `readAllBytes`, `readAllLines`, `write`)
- [x] `DirectoryStream<T>` & `DirectoryStream.Filter<T>`
- [x] `PathMatcher` & `FileSystem.getPathMatcher` (`glob:` & `regex:`)
- [x] `OpenOption` / `StandardOpenOption` (`READ`, `WRITE`, `APPEND`, `TRUNCATE_EXISTING`, `CREATE`, `CREATE_NEW`, `DELETE_ON_CLOSE`)
- [x] `CopyOption` / `StandardCopyOption` (`REPLACE_EXISTING`, `COPY_ATTRIBUTES`, `ATOMIC_MOVE`)
- [x] `LinkOption` (`NOFOLLOW_LINKS`)
- [x] `FileSystemException` / `NoSuchFileException` / `FileAlreadyExistsException` / `NotDirectoryException` / `DirectoryNotEmptyException`
- [x] `FileSystem` / `FileSystems.getDefault()` (`getPath`, `getPathMatcher`, `getSeparator`, `isOpen`；默认 FS 的 `close()` 抛 `UnsupportedOperationException`)
- [x] `Path.getFileSystem()`
- [x] `FileVisitor` / `SimpleFileVisitor` / `FileVisitResult` / `FileVisitOption` / `Files.walkFileTree`
- [x] `FileTime` (`fromMillis`, `toMillis`, `compareTo`) & `FileAttribute<T>`
- [x] `BasicFileAttributes` / `Files.readAttributes`（size、isDirectory、isRegularFile、isSymbolicLink、lastModifiedTime；mtime 来自 `File.lastModified()`）
- [x] `PosixFilePermission` & `PosixFilePermissions` (`fromString`, `toString`, `asFileAttribute`, `Files.getPosixFilePermissions`, `Files.setPosixFilePermissions`)
- [x] `kotlin.io.path.*` Kotlin extensions (`/` operator, `readText`, `writeText`, `readBytes`, `writeBytes`, `exists`, `notExists`, `isDirectory`, `isRegularFile`, `extension`, `nameWithoutExtension`)

---

## 12. Kotlin-stdlib Extensions for Java APIs (Kotlin 标准库扩展支持)

Kotlin 标准库在 JVM 平台上为大量 Java 8 类型提供了丰富的扩展函数、操作符重载和属性适配。为了让跨平台代码（包括使用方自身的 `commonMain`）能以 100% 的 Kotlin 常用习惯无缝编译运行，`jako-stdlib` 将所有扩展函数直接在 `commonMain` 的标准 `kotlin.*` 包中实现：

> **包名与自动导入说明**：
> - `kotlin.io` 是 Kotlin 语言的默认自动导入包（Default Import）。因此在 `commonMain` 及所有平台上，`File.readText()`、`File.writeText()`、`InputStream.readBytes()` 等扩展无需任何额外的 `import` 即可直接使用，完全看齐 JVM 体验。
> - `kotlin.io.path`、`kotlin.concurrent`、`kotlin.jvm.optionals` 等扩展可通过标准 `import kotlin.io.path.*`、`import kotlin.concurrent.*`、`import kotlin.jvm.optionals.*` 引入。
> - `BigInteger` 与 `BigDecimal` 的操作符重载直接位于 `package kotlin`，在所有代码中均可直接使用操作符表达式（如 `a + b`、`a * b`）。

---

### 12.1 `kotlin.io` (`java.io.*` Extensions)

#### `java.io.File` 读写扩展
- [x] `File.readText(charset: Charset = Charsets.UTF_8): String`
- [x] `File.readText(charsetName: String): String`
- [x] `File.writeText(text: String, charset: Charset = Charsets.UTF_8): Unit`
- [x] `File.writeText(text: String, charsetName: String): Unit`
- [x] `File.appendText(text: String, charset: Charset = Charsets.UTF_8): Unit`
- [x] `File.appendText(text: String, charsetName: String): Unit`
- [x] `File.readBytes(): ByteArray`
- [x] `File.writeBytes(array: ByteArray): Unit`
- [x] `File.appendBytes(array: ByteArray): Unit`
- [x] `File.readLines(charset: Charset = Charsets.UTF_8): List<String>`
- [x] `File.readLines(charsetName: String): List<String>`
- [x] `File.forEachLine(charset: Charset = Charsets.UTF_8, action: (String) -> Unit): Unit`
- [x] `File.forEachLine(charsetName: String, action: (String) -> Unit): Unit`
- [x] `File.useLines(charset: Charset = Charsets.UTF_8, block: (Sequence<String>) -> T): T`
- [x] `File.useLines(charsetName: String, block: (Sequence<String>) -> T): T`

#### `java.io.File` 路径与树操作
- [x] `File.extension: String`（获取文件扩展名）
- [x] `File.nameWithoutExtension: String`（获取去扩展名后的文件名）
- [x] `File.copyTo(target: File, overwrite: Boolean = false, bufferSize: Int = 8192): File`
- [x] `File.deleteRecursively(): Boolean`（递归删除目录与文件）
- [x] `File.copyRecursively(target: File, overwrite: Boolean = false, onError: ...): Boolean`
- [x] `File.walk(direction: FileWalkDirection)` / `File.walkTopDown()` / `File.walkBottomUp()`
- [x] `File.relativeTo(base: File): File` / `relativeToOrNull` / `relativeToOrSelf`
- [x] `File.startsWith(other: File): Boolean` / `File.endsWith(other: File): Boolean`

#### `java.io.File` 流构造快捷扩展
- [x] `File.bufferedReader(charset: Charset = Charsets.UTF_8): BufferedReader`
- [x] `File.bufferedWriter(charset: Charset = Charsets.UTF_8): BufferedWriter`
- [x] `File.reader(charset: Charset = Charsets.UTF_8): InputStreamReader`
- [x] `File.writer(charset: Charset = Charsets.UTF_8): OutputStreamWriter`

#### `java.io.InputStream` 扩展
- [x] `InputStream.readBytes(): ByteArray`
- [x] `InputStream.copyTo(out: OutputStream, bufferSize: Int = 8192): Long`
- [x] `InputStream.bufferedReader(charset: Charset = Charsets.UTF_8): BufferedReader`
- [x] `InputStream.bufferedReader(charsetName: String): BufferedReader`
- [x] `InputStream.reader(charset: Charset = Charsets.UTF_8): InputStreamReader`
- [x] `InputStream.reader(charsetName: String): InputStreamReader`
- [x] `InputStream.buffered(bufferSize: Int = 8192): BufferedInputStream`

#### `java.io.OutputStream` 扩展
- [x] `OutputStream.bufferedWriter(charset: Charset = Charsets.UTF_8): BufferedWriter`
- [x] `OutputStream.bufferedWriter(charsetName: String): BufferedWriter`
- [x] `OutputStream.writer(charset: Charset = Charsets.UTF_8): OutputStreamWriter`
- [x] `OutputStream.writer(charsetName: String): OutputStreamWriter`
- [x] `OutputStream.buffered(bufferSize: Int = 8192): BufferedOutputStream`

#### `java.io.Reader` & `java.io.Writer` 扩展
- [x] `Reader.readText(): String`
- [x] `Reader.readLines(): List<String>`
- [x] `Reader.copyTo(out: Writer, bufferSize: Int = 8192): Long`
- [x] `Reader.forEachLine(action: (String) -> Unit): Unit`
- [x] `Reader.useLines(block: (Sequence<String>) -> T): T`
- [x] `Reader.buffered(bufferSize: Int = 8192): BufferedReader`
- [x] `BufferedReader.lineSequence(): Sequence<String>`
- [x] `BufferedReader.forEachLine(action: (String) -> Unit): Unit`
- [x] `Writer.buffered(bufferSize: Int = 8192): BufferedWriter`

#### `java.io.Closeable` 资源管理
- [x] `inline fun <T : Closeable?, R> T.use(block: (T) -> R): R`（通过 Kotlin stdlib 原生 AutoCloseable/Closeable 支持）

---

### 12.2 `kotlin.io.path` (`java.nio.file.Path` Extensions)

#### 路径拼接与操作符
- [x] `operator fun Path.div(other: String): Path`（支持 `path / "sub"`）
- [x] `operator fun Path.div(other: Path): Path`

#### 内容读写
- [x] `Path.readText(charset: Charset = Charsets.UTF_8): String`
- [x] `Path.readText(charsetName: String): String`
- [x] `Path.writeText(text: CharSequence, charset: Charset = Charsets.UTF_8, vararg options: OpenOption): Unit`
- [x] `Path.writeText(text: CharSequence, charsetName: String, vararg options: OpenOption): Unit`
- [x] `Path.readBytes(): ByteArray`
- [x] `Path.writeBytes(array: ByteArray, vararg options: OpenOption): Unit`

#### 状态判断与元数据
- [x] `Path.exists(vararg options: LinkOption): Boolean`
- [x] `Path.notExists(vararg options: LinkOption): Boolean`
- [x] `Path.isDirectory(vararg options: LinkOption): Boolean`
- [x] `Path.isRegularFile(vararg options: LinkOption): Boolean`
- [x] `Path.extension: String`
- [x] `Path.nameWithoutExtension: String`
- [x] `Path.toFile(): File`

---

### 12.3 `kotlin.concurrent` (Concurrency & Locks Extensions)

#### 锁与读写锁
- [x] `inline fun <T> Lock.withLock(action: () -> T): T`
- [x] `inline fun <T> ReentrantReadWriteLock.read(action: () -> T): T`
- [x] `inline fun <T> ReentrantReadWriteLock.write(action: () -> T): T`

#### 线程创建助手
- [x] `fun thread(start: Boolean = true, isDaemon: Boolean = false, name: String? = null, priority: Int = -1, block: () -> Unit): Thread`

#### 内存可见性注解
- [x] `@Volatile`（映射至 `kotlin.concurrent.Volatile`）

---

### 12.4 `kotlin.jvm` & Language Interop (反射与互操作)

#### Class 与 KClass 桥接
- [x] `val <T : Any> KClass<T>.java: Class<T>`
- [x] `val <T : Any> KClass<T>.javaObjectType: Class<T>`
- [x] `val <T : Any> KClass<T>.javaPrimitiveType: Class<T>?`
- [x] `val <T : Any> Class<T>.kotlin: KClass<T>`
- [x] `val <T : Any> T.javaClass: Class<T>`

#### JVM 兼容注解（Stubs）
- [x] `@JvmField`
- [x] `@JvmStatic`
- [x] `@JvmOverloads`
- [x] `@JvmName(name: String)`
- [x] `@JvmSuppressWildcards(suppress: Boolean = true)`
- [x] `@Synchronized`

#### 同步块与对象监视器 (Monitor)
- [x] `inline fun <R> synchronized(lock: Any, block: () -> R): R`
- [x] `java.lang.Object` (`wait` / `notify` / `notifyAll`)

#### 异常扩展
- [x] `Throwable.initCause(cause: Throwable?): Throwable`
- [x] `val Throwable.suppressed: Array<Throwable>`
- [x] `fun Throwable.getSuppressed(): Array<Throwable>`

---

### 12.5 `kotlin.text` (字符编码、Locale 与正则)

#### 标准字符集常数 (`kotlin.text.Charsets`)
- [x] `Charsets.UTF_8`
- [x] `Charsets.ISO_8859_1`
- [x] `Charsets.US_ASCII`
- [x] `Charsets.UTF_16` / `Charsets.UTF_16BE` / `Charsets.UTF_16LE`
- [x] `Charsets.UTF_32BE` / `Charsets.UTF_32LE`

#### 字节与字符串互转
- [x] `String.toByteArray(charset: Charset = Charsets.UTF_8): ByteArray`
- [x] `String(bytes: ByteArray, charset: Charset): String`
- [x] `String(bytes: ByteArray, offset: Int, length: Int, charset: Charset): String`
- [x] `String(codePoints: IntArray, offset: Int, count: Int): String`

#### 区域敏感大小写与格式化
- [x] `String.lowercase(locale: Locale): String`
- [x] `String.uppercase(locale: Locale): String`
- [x] `String.Companion.format(format: String, vararg args: Any?): String`
- [x] `String.Companion.format(locale: Locale?, format: String, vararg args: Any?): String`

#### 正则互转
- [x] `Pattern.toRegex(): Regex`
- [x] `Regex.toPattern(): Pattern`

---

### 12.6 `java.math` Operator Overloads (`package kotlin` Operator Conventions)

#### `BigInteger` 算术与位运算
- [x] `BigInteger + BigInteger` (`plus`)
- [x] `BigInteger - BigInteger` (`minus`)
- [x] `BigInteger * BigInteger` (`times`)
- [x] `BigInteger / BigInteger` (`div`)
- [x] `BigInteger % BigInteger` (`rem`)
- [x] `-BigInteger` (`unaryMinus`)
- [x] `BigInteger.inc()` / `BigInteger.dec()`
- [x] `BigInteger shl Int` / `BigInteger shr Int`
- [x] `BigInteger and BigInteger` / `BigInteger or BigInteger` / `BigInteger xor BigInteger`
- [x] `BigInteger.inv()`
- [x] `BigInteger.compareTo(other: BigInteger)`

#### `BigDecimal` 算术运算
- [x] `BigDecimal + BigDecimal` (`plus`)
- [x] `BigDecimal - BigDecimal` (`minus`)
- [x] `BigDecimal * BigDecimal` (`times`)
- [x] `BigDecimal / BigDecimal` (`div`)
- [x] `BigDecimal % BigDecimal` (`rem`)
- [x] `-BigDecimal` (`unaryMinus`)
- [x] `BigDecimal.inc()` / `BigDecimal.dec()`
- [x] `BigDecimal.compareTo(other: BigDecimal)`

---

### 12.7 `kotlin.collections` & `java.util` (集合、迭代与容器适配)

#### `Enumeration` 扩展 (`kotlin.collections`)
- [x] `fun <E> Enumeration<E>.iterator(): Iterator<E>`
- [x] `fun <E> Enumeration<E>.toList(): List<E>`

#### `Optional` 扩展 (`kotlin.jvm.optionals`)
- [x] `fun <T : Any> Optional<T>.getOrNull(): T?`
- [x] `fun <T : Any> Optional<T>.getOrDefault(defaultValue: T): T`
- [x] `inline fun <T : Any> Optional<T>.getOrElse(onEmpty: () -> T): T`
- [x] `fun <T : Any> Optional<T>.toList(): List<T>`
- [x] `fun <T : Any> Optional<T>.asSequence(): Sequence<T>`

#### `Stream` 扩展 (`kotlin.streams`)
- [x] `fun <T> Stream<T>.asSequence(): Sequence<T>` / `fun <T> Sequence<T>.asStream(): Stream<T>`
- [x] `fun IntStream.asSequence(): Sequence<Int>` / `fun LongStream.asSequence(): Sequence<Long>` / `fun DoubleStream.asSequence(): Sequence<Double>`
- [x] `fun <T> Stream<T>.toList(): List<T>` / `fun IntStream.toList(): List<Int>` / `fun LongStream.toList(): List<Long>` / `fun DoubleStream.toList(): List<Double>`
- [x] `fun IntStream.toIntArray(): IntArray` / `fun LongStream.toLongArray(): LongArray` / `fun DoubleStream.toDoubleArray(): DoubleArray`

---

### 12.8 Synthetic JavaBean Properties (Kotlin 属性化访问)

Kotlin 编译器在调用 Java API 时会自动将 Getter/Setter 映射为同名属性。在 `jako-stdlib` Native 实现中，这些类均原生具备标准属性或 Getter/Setter 映射：

- **`java.io.File`**:
  - `file.name` (`getName()`)
  - `file.path` (`getPath()`)
  - `file.parent` (`getParent()`)
  - `file.parentFile` (`getParentFile()`)
  - `file.canonicalPath` (`getCanonicalPath()`)
  - `file.canonicalFile` (`getCanonicalFile()`)
  - `file.absolutePath` (`getAbsolutePath()`)
  - `file.absoluteFile` (`getAbsoluteFile()`)
- **`java.lang.Thread`**:
  - `thread.name` (`getName()` / `setName()`)
  - `thread.priority` (`getPriority()` / `setPriority()`)
  - `thread.isDaemon` (`isDaemon()` / `setDaemon()`)
  - `thread.isAlive` (`isAlive()`)
  - `thread.isInterrupted` (`isInterrupted()`)
  - `thread.id` (`getId()`)
- **`java.net.InetAddress`**:
  - `inetAddress.hostName` (`getHostName()`)
  - `inetAddress.hostAddress` (`getHostAddress()`)
  - `inetAddress.canonicalHostName` (`getCanonicalHostName()`)
- **`java.net.Socket`**:
  - `socket.inetAddress` (`getInetAddress()`)
  - `socket.port` (`getPort()`)
  - `socket.localPort` (`getLocalPort()`)
  - `socket.isConnected` (`isConnected()`)
  - `socket.isClosed` (`isClosed()`)
  - `socket.inputStream` (`getInputStream()`)
  - `socket.outputStream` (`getOutputStream()`)
- **`java.net.URI`**:
  - `uri.scheme`, `uri.host`, `uri.port`, `uri.path`, `uri.query`, `uri.authority`, `uri.userInfo`, `uri.fragment`
- **`java.net.URL`**:
  - `url.protocol`, `url.host`, `url.port`, `url.file`, `url.path`, `url.query`, `url.authority`

---

## API Discovery & Verification Method
To extract the complete signature list of any target JDK 8 class:
1. Run `javap -public java.io.TargetClass` against JDK 8.
2. Cross-check nullable vs non-null annotations/specifications in JDK 8 Javadoc.
3. Add entries to this checklist before starting the implementation.
