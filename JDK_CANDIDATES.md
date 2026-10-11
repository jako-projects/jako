# JDK 8 Class Porting Candidates & Priority Matrix

> **Status**: All 155 candidate classes below are implemented in `jako-stdlib` 1.0.0.
> See [`API_CHECKLIST.md`](API_CHECKLIST.md) for method-level tracking.

This document provides an **exhaustive classification and priority matrix** for JDK 8 standard library classes candidate for `jako-stdlib` porting.

## 📊 Priority Distribution & Summary

**Total Cataloged JDK Classes**: `155` classes

| Priority Level | Class Count | Key Focus Modules | Description |
|----------------|-------------|-------------------|-------------|
| **P0** | `17` | java.io, java.nio.charset | 核心基础接口与基础抽象 - 必须实现 |
| **P1** | `67` | java.io, java.math, java.net, java.nio, java.nio.charset, java.nio.file, java.security, java.text, java.time, java.time.format, java.util, java.util.concurrent.atomic, java.util.regex | 高频核心类 - 优先排期开发 |
| **P2** | `64` | java.io, java.net, java.nio, java.nio.channels, java.nio.charset, java.nio.file, java.nio.file.attribute, java.text, java.util, java.util.concurrent, java.util.concurrent.locks, java.util.regex, java.util.zip | 中频扩展与实用工具 - 按需支持 |
| **P3** | `7` | java.io, java.security, java.util, java.util.jar | 低频或已有 Kotlin 替代方案 |

---

## 🎯 P0 - Critical Foundation (核心基础接口与基础抽象 - 必须实现)

Total: **17** classes

| Class Name | Module | Usage Frequency | Status | Rationale & Use Case |
|------------|--------|-----------------|--------|----------------------|
| `java.io.File` | java.io (File & Core) | Very High | ✅ `Implemented` | Essential file and path abstraction required by virtually all file operations. |
| `java.io.InputStream` | java.io (Streams) | Very High | ✅ `Implemented` | Base abstract class for all byte input streams. |
| `java.io.OutputStream` | java.io (Streams) | Very High | ✅ `Implemented` | Base abstract class for all byte output streams. |
| `java.io.Reader` | java.io (Readers & Writers) | Very High | ✅ `Implemented` | Base abstract class for character input streams. |
| `java.io.Writer` | java.io (Readers & Writers) | Very High | ✅ `Implemented` | Base abstract class for character output streams. |
| `java.io.Closeable` | java.io (Interfaces) | Very High | ✅ `Implemented` | Interface for objects that can be closed (used in `.use {}` blocks). |
| `java.io.Flushable` | java.io (Interfaces) | High | ✅ `Implemented` | Interface for streams that can be flushed. |
| `java.io.Serializable` | java.io (Interfaces) | Very High | ✅ `Implemented` | Marker interface for serialization compatibility. |
| `java.io.ByteArrayInputStream` | java.io (Memory Streams) | High | ✅ `Implemented` | In-memory byte array stream reading. |
| `java.io.ByteArrayOutputStream` | java.io (Memory Streams) | High | ✅ `Implemented` | In-memory byte array stream buffering and writing. |
| `java.io.BufferedReader` | java.io (Buffered Streams) | Very High | ✅ `Implemented` | Buffered character reader providing efficient `readLine()` functionality. |
| `java.io.BufferedWriter` | java.io (Buffered Streams) | High | ✅ `Implemented` | Buffered character writer for efficient text output. |
| `java.io.InputStreamReader` | java.io (Encoding Bridges) | Very High | ✅ `Implemented` | Bridge from byte streams to character streams using specified charset. |
| `java.io.OutputStreamWriter` | java.io (Encoding Bridges) | Very High | ✅ `Implemented` | Bridge from character streams to byte streams using specified charset. |
| `java.io.IOException` | java.io (Exceptions) | Very High | ✅ `Implemented` | Base class for exceptions produced by failed or interrupted I/O operations. |
| `java.io.FileNotFoundException` | java.io (Exceptions) | Very High | ✅ `Implemented` | Signals that an attempt to open the file specified by a pathname has failed. |
| `java.nio.charset.StandardCharsets` | java.nio.charset | Very High | ✅ `Implemented` | Constants for standard charsets (UTF_8, US_ASCII, ISO_8859_1). |


## 🎯 P1 - High Demand (高频核心类 - 优先排期开发)

Total: **67** classes

| Class Name | Module | Usage Frequency | Status | Rationale & Use Case |
|------------|--------|-----------------|--------|----------------------|
| `java.io.StringReader` | java.io (Memory Streams) | High | ✅ `Implemented` | Character stream whose source is a string. |
| `java.io.StringWriter` | java.io (Memory Streams) | High | ✅ `Implemented` | Character stream that collects output in a string buffer. |
| `java.io.BufferedInputStream` | java.io (Buffered Streams) | High | ✅ `Implemented` | Adds buffering capability to another input stream. |
| `java.io.BufferedOutputStream` | java.io (Buffered Streams) | High | ✅ `Implemented` | Adds buffering capability to another output stream. |
| `java.io.FileInputStream` | java.io (File Streams) | High | ✅ `Implemented` | Obtains input bytes from a file in a file system. |
| `java.io.FileOutputStream` | java.io (File Streams) | High | ✅ `Implemented` | Output stream for writing data to a File. |
| `java.io.FileReader` | java.io (File Streams) | High | ✅ `Implemented` | Convenience class for reading character files. |
| `java.io.FileWriter` | java.io (File Streams) | High | ✅ `Implemented` | Convenience class for writing character files. |
| `java.io.DataInput` | java.io (Data Streams) | High | ✅ `Implemented` | Interface for reading bytes from a binary stream and reconstructing primitive data. |
| `java.io.DataOutput` | java.io (Data Streams) | High | ✅ `Implemented` | Interface for converting primitive values to bytes and writing to binary stream. |
| `java.io.DataInputStream` | java.io (Data Streams) | High | ✅ `Implemented` | Lets an application read primitive Java data types from an underlying input stream. |
| `java.io.DataOutputStream` | java.io (Data Streams) | High | ✅ `Implemented` | Lets an application write primitive Java data types to an output stream. |
| `java.io.PrintStream` | java.io (Formatted Streams) | High | ✅ `Implemented` | Adds functionality to print representations of data values conveniently. |
| `java.io.PrintWriter` | java.io (Formatted Streams) | High | ✅ `Implemented` | Prints formatted representations of objects to a text-output stream. |
| `java.io.FileFilter` | java.io (Filters) | High | ✅ `Implemented` | Filter for abstract pathnames used by File.listFiles(). |
| `java.io.FilenameFilter` | java.io (Filters) | High | ✅ `Implemented` | Filter for file names used by File.list(). |
| `java.io.EOFException` | java.io (Exceptions) | High | ✅ `Implemented` | Signals that an end of file or end of stream has been reached unexpectedly during input. |
| `java.io.UnsupportedEncodingException` | java.io (Exceptions) | High | ✅ `Implemented` | Character encoding is not supported. |
| `java.nio.file.Path` | java.nio.file (Path & Files) | Very High | ✅ `Implemented` | Modern object used to locate a file in a file system. |
| `java.nio.file.Paths` | java.nio.file (Path & Files) | Very High | ✅ `Implemented` | Static factory methods to create Path instances. |
| `java.nio.file.Files` | java.nio.file (Path & Files) | Very High | ✅ `Implemented` | Static operations on files, directories, or other types of files (readAllBytes, copy, move). |
| `java.nio.file.FileSystem` | java.nio.file (System) | High | ✅ `Implemented` | Provides an interface to a file system and factory for Path objects. |
| `java.nio.file.FileSystems` | java.nio.file (System) | High | ✅ `Implemented` | Factory methods for file systems. |
| `java.nio.file.StandardOpenOption` | java.nio.file (Options) | High | ✅ `Implemented` | Defines standard options for opening files (READ, WRITE, CREATE, APPEND). |
| `java.nio.file.StandardCopyOption` | java.nio.file (Options) | High | ✅ `Implemented` | Defines standard options for copying/moving files (REPLACE_EXISTING, ATOMIC_MOVE). |
| `java.nio.charset.Charset` | java.nio.charset | Very High | ✅ `Implemented` | Named mapping between sequences of 16-bit Unicode code units and byte sequences. |
| `java.nio.Buffer` | java.nio (Buffers) | High | ✅ `Implemented` | Base container class for data of a specific primitive type. |
| `java.nio.ByteBuffer` | java.nio (Buffers) | Very High | ✅ `Implemented` | A byte buffer for binary stream & channel I/O. |
| `java.nio.ByteOrder` | java.nio (Buffers) | High | ✅ `Implemented` | Typesafe enumeration for byte orders (BIG_ENDIAN, LITTLE_ENDIAN). |
| `java.math.BigDecimal` | java.math | Very High | ✅ `Implemented` | Immutable, arbitrary-precision signed decimal numbers, critical for monetary calculations. |
| `java.math.BigInteger` | java.math | Very High | ✅ `Implemented` | Immutable arbitrary-precision integers. |
| `java.math.MathContext` | java.math | High | ✅ `Implemented` | Encapsulates context settings for numerical operations (precision, RoundingMode). |
| `java.math.RoundingMode` | java.math | High | ✅ `Implemented` | Rounding behavior enum for numerical operations. |
| `java.util.UUID` | java.util (Identifiers & Encoding) | Very High | ✅ `Implemented` | Universally Unique Identifier representation and generator. |
| `java.util.Base64` | java.util (Identifiers & Encoding) | Very High | ✅ `Implemented` | Base64 encoder and decoder (RFC 4648 / RFC 2045). |
| `java.util.Base64.Encoder` | java.util (Identifiers & Encoding) | High | ✅ `Implemented` | Encoder for Base64 data scheme. |
| `java.util.Base64.Decoder` | java.util (Identifiers & Encoding) | High | ✅ `Implemented` | Decoder for Base64 data scheme. |
| `java.util.Random` | java.util (Utilities) | High | ✅ `Implemented` | Pseudorandom number generator. |
| `java.util.Properties` | java.util (Utilities) | High | ✅ `Implemented` | Persistent set of properties (key/value pairs) reading and writing. |
| `java.util.StringJoiner` | java.util (Parsing) | High | ✅ `Implemented` | Constructs a sequence of characters separated by a delimiter. |
| `java.util.Formatter` | java.util (Formatting) | High | ✅ `Implemented` | An interpreter for printf-style format strings. |
| `java.util.Locale` | java.util (Locale) | High | ✅ `Implemented` | Represents a specific geographical, political, or cultural region. |
| `java.util.Optional` | java.util (Optional) | Very High | ✅ `Implemented` | Container object which may or may not contain a non-null value. |
| `java.text.SimpleDateFormat` | java.text (Formatting) | Very High | ✅ `Implemented` | Concrete class for formatting and parsing dates in a locale-sensitive manner. |
| `java.util.regex.Pattern` | java.util.regex | Very High | ✅ `Implemented` | Compiled representation of a regular expression. |
| `java.util.regex.Matcher` | java.util.regex | Very High | ✅ `Implemented` | Engine that performs match operations on a character sequence. |
| `java.net.URI` | java.net (URI & URL) | Very High | ✅ `Implemented` | Uniform Resource Identifier reference parser and constructor. |
| `java.net.URL` | java.net (URI & URL) | Very High | ✅ `Implemented` | Uniform Resource Locator representation. |
| `java.net.URLEncoder` | java.net (URL Encoding) | Very High | ✅ `Implemented` | Utility for HTML form / URL query parameter percent-encoding. |
| `java.net.URLDecoder` | java.net (URL Encoding) | Very High | ✅ `Implemented` | Utility for HTML form / URL query parameter decoding. |
| `java.net.Socket` | java.net (Sockets) | Very High | ✅ `Implemented` | Core client TCP socket abstraction. |
| `java.net.ServerSocket` | java.net (Sockets) | High | ✅ `Implemented` | Server TCP socket abstraction. |
| `java.net.InetAddress` | java.net (Sockets) | Very High | ✅ `Implemented` | IP address & hostname resolution for network connections. |
| `java.net.InetSocketAddress` | java.net (Sockets) | High | ✅ `Implemented` | IP socket address (IP + port) abstraction. |
| `java.net.Proxy` | java.net (Sockets) | High | ✅ `Implemented` | Network proxy setting abstraction. |
| `java.time.Instant` | java.time (Core) | Very High | ✅ `Implemented` | An instantaneous point on the time-line. |
| `java.time.LocalDate` | java.time (Core) | Very High | ✅ `Implemented` | A date without a time-zone in the ISO-8601 calendar system. |
| `java.time.LocalTime` | java.time (Core) | Very High | ✅ `Implemented` | A time without a time-zone in the ISO-8601 calendar system. |
| `java.time.LocalDateTime` | java.time (Core) | Very High | ✅ `Implemented` | A date-time without a time-zone in the ISO-8601 calendar system. |
| `java.time.ZonedDateTime` | java.time (Core) | High | ✅ `Implemented` | A date-time with a time-zone in the ISO-8601 calendar system. |
| `java.time.OffsetDateTime` | java.time (Core) | High | ✅ `Implemented` | A date-time with an offset from UTC/Greenwich. |
| `java.time.Duration` | java.time (Core) | Very High | ✅ `Implemented` | A time-based amount of time, such as '34.5 seconds'. |
| `java.time.Period` | java.time (Core) | High | ✅ `Implemented` | A date-based amount of time in the ISO-8601 calendar system. |
| `java.time.ZoneId` | java.time (Core) | High | ✅ `Implemented` | A time-zone ID, such as Europe/Paris. |
| `java.time.ZoneOffset` | java.time (Core) | High | ✅ `Implemented` | A time-zone offset from UTC/Greenwich, such as +02:00. |
| `java.time.format.DateTimeFormatter` | java.time.format | Very High | ✅ `Implemented` | Formatter for printing and parsing date-time objects. |
| `java.util.concurrent.atomic.AtomicInteger` | java.util.concurrent.atomic | Very High | ✅ `Implemented` | An int value that may be updated atomically. |
| `java.util.concurrent.atomic.AtomicLong` | java.util.concurrent.atomic | Very High | ✅ `Implemented` | A long value that may be updated atomically. |
| `java.util.concurrent.atomic.AtomicBoolean` | java.util.concurrent.atomic | Very High | ✅ `Implemented` | A boolean value that may be updated atomically. |
| `java.util.concurrent.atomic.AtomicReference` | java.util.concurrent.atomic | Very High | ✅ `Implemented` | An object reference that may be updated atomically. |
| `java.security.MessageDigest` | java.security (Hashing & Random) | Very High | ✅ `Implemented` | Provides applications the functionality of a message digest algorithm (MD5, SHA-1, SHA-256). |
| `java.security.SecureRandom` | java.security (Hashing & Random) | Very High | ✅ `Implemented` | Provides a cryptographically strong random number generator. |



## 🎯 P2 - Medium Priority (中频扩展与实用工具 - 按需支持)

Total: **64** classes

| Class Name | Module | Usage Frequency | Status | Rationale & Use Case |
|------------|--------|-----------------|--------|----------------------|
| `java.io.CharArrayReader` | java.io (Memory Streams) | Low-Medium | ✅ `Implemented` | In-memory char array reader. |
| `java.io.CharArrayWriter` | java.io (Memory Streams) | Low-Medium | ✅ `Implemented` | In-memory char array writer. |
| `java.io.RandomAccessFile` | java.io (Advanced) | Medium | ✅ `Implemented` | Random access file reading and writing capabilities. |
| `java.io.PushbackInputStream` | java.io (Advanced) | Medium | ✅ `Implemented` | Adds unread functionality to input streams for parsers. |
| `java.io.PushbackReader` | java.io (Advanced) | Medium | ✅ `Implemented` | Adds unread functionality to character readers for parsers. |
| `java.io.SequenceInputStream` | java.io (Advanced) | Low-Medium | ✅ `Implemented` | Concatenates multiple input streams into a single logical stream. |
| `java.io.ObjectInputStream` | java.io (Serialization) | Medium | ✅ `Implemented` | Deserializes primitive data and objects previously written using an ObjectOutputStream. |
| `java.io.ObjectOutputStream` | java.io (Serialization) | Medium | ✅ `Implemented` | Serializes Java primitives and graphs of objects to an OutputStream. |
| `java.io.InterruptedIOException` | java.io (Exceptions) | Medium | ✅ `Implemented` | Signals that an I/O operation has been interrupted. |
| `java.nio.file.LinkOption` | java.nio.file (Options) | Medium | ✅ `Implemented` | Defines options for handling symbolic links. |
| `java.nio.file.FileVisitor / SimpleFileVisitor` | java.nio.file (Traversal) | Medium | ✅ `Implemented` | Visitor pattern for recursively walking file trees. |
| `java.nio.file.FileVisitResult` | java.nio.file (Traversal) | Medium | ✅ `Implemented` | Return type for FileVisitor methods (CONTINUE, TERMINATE, SKIP_SUBTREE). |
| `java.nio.file.attribute.BasicFileAttributes` | java.nio.file.attribute | Medium | ✅ `Implemented` | Basic attributes associated with a file (creationTime, lastAccessTime, size). |
| `java.nio.file.attribute.FileTime` | java.nio.file.attribute | Medium | ✅ `Implemented` | Represents the value of a file's time stamp attribute. |
| `java.nio.file.attribute.PosixFilePermissions` | java.nio.file.attribute | Medium | ✅ `Implemented` | Utility methods for manipulating POSIX file permissions. |
| `java.nio.charset.CharsetEncoder` | java.nio.charset | Medium | ✅ `Implemented` | Engine that transforms sixteen-bit Unicode characters into bytes. |
| `java.nio.charset.CharsetDecoder` | java.nio.charset | Medium | ✅ `Implemented` | Engine that transforms bytes in a specific charset into sixteen-bit Unicode characters. |
| `java.nio.CharBuffer` | java.nio (Buffers) | Medium | ✅ `Implemented` | Character buffer for character sequence manipulation. |
| `java.nio.IntBuffer` | java.nio (Buffers) | Medium | ✅ `Implemented` | Int buffer abstraction. |
| `java.nio.LongBuffer` | java.nio (Buffers) | Medium | ✅ `Implemented` | Long buffer abstraction. |
| `java.nio.channels.FileChannel` | java.nio.channels | Medium-High | ✅ `Implemented` | A channel for reading, writing, mapping, and manipulating a file. |
| `java.nio.channels.ReadableByteChannel` | java.nio.channels | Medium | ✅ `Implemented` | Channel that can read bytes. |
| `java.nio.channels.WritableByteChannel` | java.nio.channels | Medium | ✅ `Implemented` | Channel that can write bytes. |
| `java.util.BitSet` | java.util (Utilities) | Medium | ✅ `Implemented` | Vector of bits that grows as needed. |
| `java.util.Scanner` | java.util (Parsing) | Medium-High | ✅ `Implemented` | Simple text scanner to parse primitive types and strings using regular expressions. |
| `java.util.StringTokenizer` | java.util (Parsing) | Medium | ✅ `Implemented` | Legacy utility for breaking strings into tokens. |
| `java.util.Date` | java.util (Date & Time Legacy) | High | ✅ `Implemented` | Legacy date representation widely used in existing Java 8 codebases. |
| `java.util.Calendar` | java.util (Date & Time Legacy) | High | ✅ `Implemented` | Legacy abstract class for converting between a point in time and calendar fields. |
| `java.util.TimeZone` | java.util (Date & Time Legacy) | Medium | ✅ `Implemented` | Time zone offsets and Daylight Saving Time calculations. |
| `java.util.Currency` | java.util (Locale) | Medium | ✅ `Implemented` | Represents a currency identified by ISO 4217 code. |
| `java.util.OptionalInt / OptionalLong / OptionalDouble` | java.util (Optional) | Medium | ✅ `Implemented` | Primitive specialized optional containers. |
| `java.text.Format` | java.text (Formatting) | Medium | ✅ `Implemented` | Abstract base class for formatting locale-sensitive information. |
| `java.text.DateFormat` | java.text (Formatting) | High | ✅ `Implemented` | Abstract class for date/time formatting sub-classes. |
| `java.text.NumberFormat` | java.text (Formatting) | High | ✅ `Implemented` | Abstract base class for all number formats. |
| `java.text.DecimalFormat` | java.text (Formatting) | High | ✅ `Implemented` | Concrete subclass of NumberFormat for formatting decimal numbers. |
| `java.text.MessageFormat` | java.text (Formatting) | Medium | ✅ `Implemented` | Produces concatenated messages in a language-neutral way. |
| `java.util.regex.PatternSyntaxException` | java.util.regex | Medium | ✅ `Implemented` | Unchecked exception thrown to indicate a syntax error in a regex pattern. |
| `java.util.zip.ZipFile` | java.util.zip (ZIP Archives) | High | ✅ `Implemented` | Used to read entries from a ZIP file. |
| `java.util.zip.ZipEntry` | java.util.zip (ZIP Archives) | High | ✅ `Implemented` | Class to represent a ZIP file entry. |
| `java.util.zip.ZipInputStream` | java.util.zip (ZIP Archives) | High | ✅ `Implemented` | Input stream filter for reading files in the ZIP file format. |
| `java.util.zip.ZipOutputStream` | java.util.zip (ZIP Archives) | High | ✅ `Implemented` | Output stream filter for writing files in the ZIP file format. |
| `java.util.zip.GZIPInputStream` | java.util.zip (GZIP Streams) | High | ✅ `Implemented` | Input stream filter for reading compressed data in GZIP format. |
| `java.util.zip.GZIPOutputStream` | java.util.zip (GZIP Streams) | High | ✅ `Implemented` | Output stream filter for writing compressed data in GZIP format. |
| `java.util.zip.Deflater` | java.util.zip (Algorithms) | Medium | ✅ `Implemented` | General zlib compression algorithm engine. |
| `java.util.zip.Inflater` | java.util.zip (Algorithms) | Medium | ✅ `Implemented` | General zlib decompression algorithm engine. |
| `java.util.zip.CRC32` | java.util.zip (Checksums) | Medium-High | ✅ `Implemented` | Computes CRC-32 checksum of a data stream. |
| `java.util.zip.Adler32` | java.util.zip (Checksums) | Medium | ✅ `Implemented` | Computes Adler-32 checksum of a data stream. |
| `java.net.URLConnection` | java.net (HTTP & Connections) | Medium | ✅ `Implemented` | Abstract class for links between application and URL. |
| `java.net.HttpURLConnection` | java.net (HTTP & Connections) | Medium-High | ✅ `Implemented` | URLConnection with support for HTTP-specific features. |
| `java.net.InetAddress` | java.net (Sockets & Addresses) | Medium | ✅ `Implemented` | Represents an Internet Protocol (IP) address. |
| `java.net.InetSocketAddress` | java.net (Sockets & Addresses) | Medium | ✅ `Implemented` | Implements an IP Socket Address (IP address + port number). |
| `java.net.Socket` | java.net (Sockets & Addresses) | Medium | ✅ `Implemented` | Client socket for TCP network communication. |
| `java.net.ServerSocket` | java.net (Sockets & Addresses) | Medium | ✅ `Implemented` | Server socket listening for incoming TCP client connections. |
| `java.util.concurrent.locks.Lock` | java.util.concurrent.locks | High | ✅ `Implemented` | Lock interface for explicit thread synchronization. |
| `java.util.concurrent.locks.ReentrantLock` | java.util.concurrent.locks | High | ✅ `Implemented` | Reentrant mutual exclusion Lock implementation. |
| `java.util.concurrent.locks.ReentrantReadWriteLock` | java.util.concurrent.locks | Medium | ✅ `Implemented` | ReadWriteLock implementation supporting reentrancy. |
| `java.util.concurrent.ConcurrentHashMap` | java.util.concurrent (Collections) | Very High | ✅ `Implemented` | Hash table supporting full concurrency of retrievals and high expected concurrency for updates. |
| `java.util.concurrent.ConcurrentLinkedQueue` | java.util.concurrent (Collections) | Medium-High | ✅ `Implemented` | An unbounded thread-safe queue based on linked nodes. |
| `java.util.concurrent.CopyOnWriteArrayList` | java.util.concurrent (Collections) | Medium-High | ✅ `Implemented` | Thread-safe variant of ArrayList in which all mutative operations are implemented by making a fresh copy. |
| `java.util.concurrent.CountDownLatch` | java.util.concurrent (Synchronizers) | Medium | ✅ `Implemented` | Synchronization aid that allows one or more threads to wait until a set of operations finishes. |
| `java.util.concurrent.Semaphore` | java.util.concurrent (Synchronizers) | Medium | ✅ `Implemented` | Counting semaphore. |
| `java.util.concurrent.CompletableFuture` | java.util.concurrent (Futures) | High | ✅ `Implemented` | A Future that may be explicitly completed (often mapped or bridged to Kotlin Coroutines). |
| `java.util.concurrent.Executor` | java.util.concurrent (Executors) | High | ✅ `Implemented` | An object that executes submitted Runnable tasks. |
| `java.util.concurrent.ExecutorService` | java.util.concurrent (Executors) | High | ✅ `Implemented` | An Executor that provides methods to manage termination and produce Future tasks. |


## 🎯 P3 - Low Priority / Alternatives Exist (低频或已有 Kotlin 替代方案)

Total: **7** classes

| Class Name | Module | Usage Frequency | Status | Rationale & Use Case |
|------------|--------|-----------------|--------|----------------------|
| `java.io.PipedInputStream / PipedOutputStream` | java.io (Pipes) | Low | ✅ `Implemented` | Piped byte streams for inter-thread communication. |
| `java.io.PipedReader / PipedWriter` | java.io (Pipes) | Low | ✅ `Implemented` | Piped character streams for inter-thread communication. |
| `java.util.Timer / TimerTask` | java.util (Timer) | Low-Medium | ✅ `Implemented` | Facility for threads to schedule tasks for future execution. |
| `java.util.jar.JarFile` | java.util.jar (JAR Archives) | Low-Medium | ✅ `Implemented` | Reading content from JAR archive files. |
| `java.util.jar.JarEntry` | java.util.jar (JAR Archives) | Low-Medium | ✅ `Implemented` | Represents an entry in a JAR file. |
| `java.util.jar.Manifest` | java.util.jar (JAR Archives) | Low-Medium | ✅ `Implemented` | JAR manifest file representation. |
| `java.security.KeyStore` | java.security (Keys) | Medium | ✅ `Implemented` | Storage facility for cryptographic keys and certificates. |


---

## 📁 Categorized View by Functional Module

### java.io (Advanced) (4 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.io.RandomAccessFile` | **P2** | Medium | ✅ `Implemented` | Random access file reading and writing capabilities. |
| `java.io.PushbackInputStream` | **P2** | Medium | ✅ `Implemented` | Adds unread functionality to input streams for parsers. |
| `java.io.PushbackReader` | **P2** | Medium | ✅ `Implemented` | Adds unread functionality to character readers for parsers. |
| `java.io.SequenceInputStream` | **P2** | Low-Medium | ✅ `Implemented` | Concatenates multiple input streams into a single logical stream. |


### java.io (Buffered Streams) (4 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.io.BufferedReader` | **P0** | Very High | ✅ `Implemented` | Buffered character reader providing efficient `readLine()` functionality. |
| `java.io.BufferedWriter` | **P0** | High | ✅ `Implemented` | Buffered character writer for efficient text output. |
| `java.io.BufferedInputStream` | **P1** | High | ✅ `Implemented` | Adds buffering capability to another input stream. |
| `java.io.BufferedOutputStream` | **P1** | High | ✅ `Implemented` | Adds buffering capability to another output stream. |


### java.io (Data Streams) (4 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.io.DataInput` | **P1** | High | ✅ `Implemented` | Interface for reading bytes from a binary stream and reconstructing primitive data. |
| `java.io.DataOutput` | **P1** | High | ✅ `Implemented` | Interface for converting primitive values to bytes and writing to binary stream. |
| `java.io.DataInputStream` | **P1** | High | ✅ `Implemented` | Lets an application read primitive Java data types from an underlying input stream. |
| `java.io.DataOutputStream` | **P1** | High | ✅ `Implemented` | Lets an application write primitive Java data types to an output stream. |


### java.io (Encoding Bridges) (2 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.io.InputStreamReader` | **P0** | Very High | ✅ `Implemented` | Bridge from byte streams to character streams using specified charset. |
| `java.io.OutputStreamWriter` | **P0** | Very High | ✅ `Implemented` | Bridge from character streams to byte streams using specified charset. |


### java.io (Exceptions) (5 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.io.IOException` | **P0** | Very High | ✅ `Implemented` | Base class for exceptions produced by failed or interrupted I/O operations. |
| `java.io.FileNotFoundException` | **P0** | Very High | ✅ `Implemented` | Signals that an attempt to open the file specified by a pathname has failed. |
| `java.io.EOFException` | **P1** | High | ✅ `Implemented` | Signals that an end of file or end of stream has been reached unexpectedly during input. |
| `java.io.InterruptedIOException` | **P2** | Medium | ✅ `Implemented` | Signals that an I/O operation has been interrupted. |
| `java.io.UnsupportedEncodingException` | **P1** | High | ✅ `Implemented` | Character encoding is not supported. |


### java.io (File & Core) (1 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.io.File` | **P0** | Very High | ✅ `Implemented` | Essential file and path abstraction required by virtually all file operations. |


### java.io (File Streams) (4 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.io.FileInputStream` | **P1** | High | ✅ `Implemented` | Obtains input bytes from a file in a file system. |
| `java.io.FileOutputStream` | **P1** | High | ✅ `Implemented` | Output stream for writing data to a File. |
| `java.io.FileReader` | **P1** | High | ✅ `Implemented` | Convenience class for reading character files. |
| `java.io.FileWriter` | **P1** | High | ✅ `Implemented` | Convenience class for writing character files. |


### java.io (Filters) (2 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.io.FileFilter` | **P1** | High | ✅ `Implemented` | Filter for abstract pathnames used by File.listFiles(). |
| `java.io.FilenameFilter` | **P1** | High | ✅ `Implemented` | Filter for file names used by File.list(). |


### java.io (Formatted Streams) (2 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.io.PrintStream` | **P1** | High | ✅ `Implemented` | Adds functionality to print representations of data values conveniently. |
| `java.io.PrintWriter` | **P1** | High | ✅ `Implemented` | Prints formatted representations of objects to a text-output stream. |


### java.io (Interfaces) (3 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.io.Closeable` | **P0** | Very High | ✅ `Implemented` | Interface for objects that can be closed (used in `.use {}` blocks). |
| `java.io.Flushable` | **P0** | High | ✅ `Implemented` | Interface for streams that can be flushed. |
| `java.io.Serializable` | **P0** | Very High | ✅ `Implemented` | Marker interface for serialization compatibility. |


### java.io (Memory Streams) (6 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.io.ByteArrayInputStream` | **P0** | High | ✅ `Implemented` | In-memory byte array stream reading. |
| `java.io.ByteArrayOutputStream` | **P0** | High | ✅ `Implemented` | In-memory byte array stream buffering and writing. |
| `java.io.CharArrayReader` | **P2** | Low-Medium | ✅ `Implemented` | In-memory char array reader. |
| `java.io.CharArrayWriter` | **P2** | Low-Medium | ✅ `Implemented` | In-memory char array writer. |
| `java.io.StringReader` | **P1** | High | ✅ `Implemented` | Character stream whose source is a string. |
| `java.io.StringWriter` | **P1** | High | ✅ `Implemented` | Character stream that collects output in a string buffer. |


### java.io (Pipes) (2 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.io.PipedInputStream / PipedOutputStream` | **P3** | Low | ✅ `Implemented` | Piped byte streams for inter-thread communication. |
| `java.io.PipedReader / PipedWriter` | **P3** | Low | ✅ `Implemented` | Piped character streams for inter-thread communication. |


### java.io (Readers & Writers) (2 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.io.Reader` | **P0** | Very High | ✅ `Implemented` | Base abstract class for character input streams. |
| `java.io.Writer` | **P0** | Very High | ✅ `Implemented` | Base abstract class for character output streams. |


### java.io (Serialization) (2 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.io.ObjectInputStream` | **P2** | Medium | ✅ `Implemented` | Deserializes primitive data and objects previously written using an ObjectOutputStream. |
| `java.io.ObjectOutputStream` | **P2** | Medium | ✅ `Implemented` | Serializes Java primitives and graphs of objects to an OutputStream. |


### java.io (Streams) (2 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.io.InputStream` | **P0** | Very High | ✅ `Implemented` | Base abstract class for all byte input streams. |
| `java.io.OutputStream` | **P0** | Very High | ✅ `Implemented` | Base abstract class for all byte output streams. |


### java.math (4 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.math.BigDecimal` | **P1** | Very High | ✅ `Implemented` | Immutable, arbitrary-precision signed decimal numbers, critical for monetary calculations. |
| `java.math.BigInteger` | **P1** | Very High | ✅ `Implemented` | Immutable arbitrary-precision integers. |
| `java.math.MathContext` | **P1** | High | ✅ `Implemented` | Encapsulates context settings for numerical operations (precision, RoundingMode). |
| `java.math.RoundingMode` | **P1** | High | ✅ `Implemented` | Rounding behavior enum for numerical operations. |


### java.net (HTTP & Connections) (2 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.net.URLConnection` | **P2** | Medium | ✅ `Implemented` | Abstract class for links between application and URL. |
| `java.net.HttpURLConnection` | **P2** | Medium-High | ✅ `Implemented` | URLConnection with support for HTTP-specific features. |


### java.net (Sockets & Addresses) (4 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.net.InetAddress` | **P2** | Medium | ✅ `Implemented` | Represents an Internet Protocol (IP) address. |
| `java.net.InetSocketAddress` | **P2** | Medium | ✅ `Implemented` | Implements an IP Socket Address (IP address + port number). |
| `java.net.Socket` | **P2** | Medium | ✅ `Implemented` | Client socket for TCP network communication. |
| `java.net.ServerSocket` | **P2** | Medium | ✅ `Implemented` | Server socket listening for incoming TCP client connections. |


### java.net (URI & URL) (2 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.net.URI` | **P1** | Very High | ✅ `Implemented` | Uniform Resource Identifier reference parser and constructor. |
| `java.net.URL` | **P1** | Very High | ✅ `Implemented` | Uniform Resource Locator representation. |


### java.net (URL Encoding) (2 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.net.URLEncoder` | **P1** | Very High | ✅ `Implemented` | Utility for HTML form / URL query parameter percent-encoding. |
| `java.net.URLDecoder` | **P1** | Very High | ✅ `Implemented` | Utility for HTML form / URL query parameter decoding. |


### java.nio (Buffers) (6 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.nio.Buffer` | **P1** | High | ✅ `Implemented` | Base container class for data of a specific primitive type. |
| `java.nio.ByteBuffer` | **P1** | Very High | ✅ `Implemented` | A byte buffer for binary stream & channel I/O. |
| `java.nio.CharBuffer` | **P2** | Medium | ✅ `Implemented` | Character buffer for character sequence manipulation. |
| `java.nio.IntBuffer` | **P2** | Medium | ✅ `Implemented` | Int buffer abstraction. |
| `java.nio.LongBuffer` | **P2** | Medium | ✅ `Implemented` | Long buffer abstraction. |
| `java.nio.ByteOrder` | **P1** | High | ✅ `Implemented` | Typesafe enumeration for byte orders (BIG_ENDIAN, LITTLE_ENDIAN). |


### java.nio.channels (3 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.nio.channels.FileChannel` | **P2** | Medium-High | ✅ `Implemented` | A channel for reading, writing, mapping, and manipulating a file. |
| `java.nio.channels.ReadableByteChannel` | **P2** | Medium | ✅ `Implemented` | Channel that can read bytes. |
| `java.nio.channels.WritableByteChannel` | **P2** | Medium | ✅ `Implemented` | Channel that can write bytes. |


### java.nio.charset (4 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.nio.charset.StandardCharsets` | **P0** | Very High | ✅ `Implemented` | Constants for standard charsets (UTF_8, US_ASCII, ISO_8859_1). |
| `java.nio.charset.Charset` | **P1** | Very High | ✅ `Implemented` | Named mapping between sequences of 16-bit Unicode code units and byte sequences. |
| `java.nio.charset.CharsetEncoder` | **P2** | Medium | ✅ `Implemented` | Engine that transforms sixteen-bit Unicode characters into bytes. |
| `java.nio.charset.CharsetDecoder` | **P2** | Medium | ✅ `Implemented` | Engine that transforms bytes in a specific charset into sixteen-bit Unicode characters. |


### java.nio.file (Options) (3 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.nio.file.StandardOpenOption` | **P1** | High | ✅ `Implemented` | Defines standard options for opening files (READ, WRITE, CREATE, APPEND). |
| `java.nio.file.StandardCopyOption` | **P1** | High | ✅ `Implemented` | Defines standard options for copying/moving files (REPLACE_EXISTING, ATOMIC_MOVE). |
| `java.nio.file.LinkOption` | **P2** | Medium | ✅ `Implemented` | Defines options for handling symbolic links. |


### java.nio.file (Path & Files) (3 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.nio.file.Path` | **P1** | Very High | ✅ `Implemented` | Modern object used to locate a file in a file system. |
| `java.nio.file.Paths` | **P1** | Very High | ✅ `Implemented` | Static factory methods to create Path instances. |
| `java.nio.file.Files` | **P1** | Very High | ✅ `Implemented` | Static operations on files, directories, or other types of files (readAllBytes, copy, move). |


### java.nio.file (System) (2 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.nio.file.FileSystem` | **P1** | High | ✅ `Implemented` | Provides an interface to a file system and factory for Path objects. |
| `java.nio.file.FileSystems` | **P1** | High | ✅ `Implemented` | Factory methods for file systems. |


### java.nio.file (Traversal) (2 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.nio.file.FileVisitor / SimpleFileVisitor` | **P2** | Medium | ✅ `Implemented` | Visitor pattern for recursively walking file trees. |
| `java.nio.file.FileVisitResult` | **P2** | Medium | ✅ `Implemented` | Return type for FileVisitor methods (CONTINUE, TERMINATE, SKIP_SUBTREE). |


### java.nio.file.attribute (3 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.nio.file.attribute.BasicFileAttributes` | **P2** | Medium | ✅ `Implemented` | Basic attributes associated with a file (creationTime, lastAccessTime, size). |
| `java.nio.file.attribute.FileTime` | **P2** | Medium | ✅ `Implemented` | Represents the value of a file's time stamp attribute. |
| `java.nio.file.attribute.PosixFilePermissions` | **P2** | Medium | ✅ `Implemented` | Utility methods for manipulating POSIX file permissions. |


### java.security (Hashing & Random) (2 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.security.MessageDigest` | **P1** | Very High | ✅ `Implemented` | Provides applications the functionality of a message digest algorithm (MD5, SHA-1, SHA-256). |
| `java.security.SecureRandom` | **P1** | Very High | ✅ `Implemented` | Provides a cryptographically strong random number generator. |


### java.security (Keys) (1 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.security.KeyStore` | **P3** | Medium | ✅ `Implemented` | Storage facility for cryptographic keys and certificates. |


### java.text (Formatting) (6 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.text.Format` | **P2** | Medium | ✅ `Implemented` | Abstract base class for formatting locale-sensitive information. |
| `java.text.DateFormat` | **P2** | High | ✅ `Implemented` | Abstract class for date/time formatting sub-classes. |
| `java.text.SimpleDateFormat` | **P1** | Very High | ✅ `Implemented` | Concrete class for formatting and parsing dates in a locale-sensitive manner. |
| `java.text.NumberFormat` | **P2** | High | ✅ `Implemented` | Abstract base class for all number formats. |
| `java.text.DecimalFormat` | **P2** | High | ✅ `Implemented` | Concrete subclass of NumberFormat for formatting decimal numbers. |
| `java.text.MessageFormat` | **P2** | Medium | ✅ `Implemented` | Produces concatenated messages in a language-neutral way. |


### java.time (Core) (10 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.time.Instant` | **P1** | Very High | ✅ `Implemented` | An instantaneous point on the time-line. |
| `java.time.LocalDate` | **P1** | Very High | ✅ `Implemented` | A date without a time-zone in the ISO-8601 calendar system. |
| `java.time.LocalTime` | **P1** | Very High | ✅ `Implemented` | A time without a time-zone in the ISO-8601 calendar system. |
| `java.time.LocalDateTime` | **P1** | Very High | ✅ `Implemented` | A date-time without a time-zone in the ISO-8601 calendar system. |
| `java.time.ZonedDateTime` | **P1** | High | ✅ `Implemented` | A date-time with a time-zone in the ISO-8601 calendar system. |
| `java.time.OffsetDateTime` | **P1** | High | ✅ `Implemented` | A date-time with an offset from UTC/Greenwich. |
| `java.time.Duration` | **P1** | Very High | ✅ `Implemented` | A time-based amount of time, such as '34.5 seconds'. |
| `java.time.Period` | **P1** | High | ✅ `Implemented` | A date-based amount of time in the ISO-8601 calendar system. |
| `java.time.ZoneId` | **P1** | High | ✅ `Implemented` | A time-zone ID, such as Europe/Paris. |
| `java.time.ZoneOffset` | **P1** | High | ✅ `Implemented` | A time-zone offset from UTC/Greenwich, such as +02:00. |


### java.time.format (1 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.time.format.DateTimeFormatter` | **P1** | Very High | ✅ `Implemented` | Formatter for printing and parsing date-time objects. |


### java.util (Date & Time Legacy) (3 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.util.Date` | **P2** | High | ✅ `Implemented` | Legacy date representation widely used in existing Java 8 codebases. |
| `java.util.Calendar` | **P2** | High | ✅ `Implemented` | Legacy abstract class for converting between a point in time and calendar fields. |
| `java.util.TimeZone` | **P2** | Medium | ✅ `Implemented` | Time zone offsets and Daylight Saving Time calculations. |


### java.util (Formatting) (1 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.util.Formatter` | **P1** | High | ✅ `Implemented` | An interpreter for printf-style format strings. |


### java.util (Identifiers & Encoding) (4 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.util.UUID` | **P1** | Very High | ✅ `Implemented` | Universally Unique Identifier representation and generator. |
| `java.util.Base64` | **P1** | Very High | ✅ `Implemented` | Base64 encoder and decoder (RFC 4648 / RFC 2045). |
| `java.util.Base64.Encoder` | **P1** | High | ✅ `Implemented` | Encoder for Base64 data scheme. |
| `java.util.Base64.Decoder` | **P1** | High | ✅ `Implemented` | Decoder for Base64 data scheme. |


### java.util (Locale) (2 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.util.Locale` | **P1** | High | ✅ `Implemented` | Represents a specific geographical, political, or cultural region. |
| `java.util.Currency` | **P2** | Medium | ✅ `Implemented` | Represents a currency identified by ISO 4217 code. |


### java.util (Optional) (2 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.util.Optional` | **P1** | Very High | ✅ `Implemented` | Container object which may or may not contain a non-null value. |
| `java.util.OptionalInt / OptionalLong / OptionalDouble` | **P2** | Medium | ✅ `Implemented` | Primitive specialized optional containers. |


### java.util (Parsing) (3 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.util.Scanner` | **P2** | Medium-High | ✅ `Implemented` | Simple text scanner to parse primitive types and strings using regular expressions. |
| `java.util.StringTokenizer` | **P2** | Medium | ✅ `Implemented` | Legacy utility for breaking strings into tokens. |
| `java.util.StringJoiner` | **P1** | High | ✅ `Implemented` | Constructs a sequence of characters separated by a delimiter. |


### java.util (Timer) (1 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.util.Timer / TimerTask` | **P3** | Low-Medium | ✅ `Implemented` | Facility for threads to schedule tasks for future execution. |


### java.util (Utilities) (3 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.util.Random` | **P1** | High | ✅ `Implemented` | Pseudorandom number generator. |
| `java.util.Properties` | **P1** | High | ✅ `Implemented` | Persistent set of properties (key/value pairs) reading and writing. |
| `java.util.BitSet` | **P2** | Medium | ✅ `Implemented` | Vector of bits that grows as needed. |


### java.util.concurrent (Collections) (3 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.util.concurrent.ConcurrentHashMap` | **P2** | Very High | ✅ `Implemented` | Hash table supporting full concurrency of retrievals and high expected concurrency for updates. |
| `java.util.concurrent.ConcurrentLinkedQueue` | **P2** | Medium-High | ✅ `Implemented` | An unbounded thread-safe queue based on linked nodes. |
| `java.util.concurrent.CopyOnWriteArrayList` | **P2** | Medium-High | ✅ `Implemented` | Thread-safe variant of ArrayList in which all mutative operations are implemented by making a fresh copy. |


### java.util.concurrent (Executors) (2 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.util.concurrent.Executor` | **P2** | High | ✅ `Implemented` | An object that executes submitted Runnable tasks. |
| `java.util.concurrent.ExecutorService` | **P2** | High | ✅ `Implemented` | An Executor that provides methods to manage termination and produce Future tasks. |


### java.util.concurrent (Futures) (1 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.util.concurrent.CompletableFuture` | **P2** | High | ✅ `Implemented` | A Future that may be explicitly completed (often mapped or bridged to Kotlin Coroutines). |


### java.util.concurrent (Synchronizers) (2 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.util.concurrent.CountDownLatch` | **P2** | Medium | ✅ `Implemented` | Synchronization aid that allows one or more threads to wait until a set of operations finishes. |
| `java.util.concurrent.Semaphore` | **P2** | Medium | ✅ `Implemented` | Counting semaphore. |


### java.util.concurrent.atomic (4 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.util.concurrent.atomic.AtomicInteger` | **P1** | Very High | ✅ `Implemented` | An int value that may be updated atomically. |
| `java.util.concurrent.atomic.AtomicLong` | **P1** | Very High | ✅ `Implemented` | A long value that may be updated atomically. |
| `java.util.concurrent.atomic.AtomicBoolean` | **P1** | Very High | ✅ `Implemented` | A boolean value that may be updated atomically. |
| `java.util.concurrent.atomic.AtomicReference` | **P1** | Very High | ✅ `Implemented` | An object reference that may be updated atomically. |


### java.util.concurrent.locks (3 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.util.concurrent.locks.Lock` | **P2** | High | ✅ `Implemented` | Lock interface for explicit thread synchronization. |
| `java.util.concurrent.locks.ReentrantLock` | **P2** | High | ✅ `Implemented` | Reentrant mutual exclusion Lock implementation. |
| `java.util.concurrent.locks.ReentrantReadWriteLock` | **P2** | Medium | ✅ `Implemented` | ReadWriteLock implementation supporting reentrancy. |


### java.util.jar (JAR Archives) (3 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.util.jar.JarFile` | **P3** | Low-Medium | ✅ `Implemented` | Reading content from JAR archive files. |
| `java.util.jar.JarEntry` | **P3** | Low-Medium | ✅ `Implemented` | Represents an entry in a JAR file. |
| `java.util.jar.Manifest` | **P3** | Low-Medium | ✅ `Implemented` | JAR manifest file representation. |


### java.util.regex (3 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.util.regex.Pattern` | **P1** | Very High | ✅ `Implemented` | Compiled representation of a regular expression. |
| `java.util.regex.Matcher` | **P1** | Very High | ✅ `Implemented` | Engine that performs match operations on a character sequence. |
| `java.util.regex.PatternSyntaxException` | **P2** | Medium | ✅ `Implemented` | Unchecked exception thrown to indicate a syntax error in a regex pattern. |


### java.util.zip (Algorithms) (2 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.util.zip.Deflater` | **P2** | Medium | ✅ `Implemented` | General zlib compression algorithm engine. |
| `java.util.zip.Inflater` | **P2** | Medium | ✅ `Implemented` | General zlib decompression algorithm engine. |


### java.util.zip (Checksums) (2 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.util.zip.CRC32` | **P2** | Medium-High | ✅ `Implemented` | Computes CRC-32 checksum of a data stream. |
| `java.util.zip.Adler32` | **P2** | Medium | ✅ `Implemented` | Computes Adler-32 checksum of a data stream. |


### java.util.zip (GZIP Streams) (2 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.util.zip.GZIPInputStream` | **P2** | High | ✅ `Implemented` | Input stream filter for reading compressed data in GZIP format. |
| `java.util.zip.GZIPOutputStream` | **P2** | High | ✅ `Implemented` | Output stream filter for writing compressed data in GZIP format. |


### java.util.zip (ZIP Archives) (4 classes)

| Class Name | Priority | Usage Frequency | Status | Rationale |
|------------|----------|-----------------|--------|-----------|
| `java.util.zip.ZipFile` | **P2** | High | ✅ `Implemented` | Used to read entries from a ZIP file. |
| `java.util.zip.ZipEntry` | **P2** | High | ✅ `Implemented` | Class to represent a ZIP file entry. |
| `java.util.zip.ZipInputStream` | **P2** | High | ✅ `Implemented` | Input stream filter for reading files in the ZIP file format. |
| `java.util.zip.ZipOutputStream` | **P2** | High | ✅ `Implemented` | Output stream filter for writing files in the ZIP file format. |

