# `jako` Unsupported JDK APIs Blacklist (不支持的 JDK API 清单)

> **文档定位与自动化校验说明**：
> 1. 本文档明确列出 `jako` 在当前目标 JDK 基线（默认 **JDK 8**，`--release 8`）下**明确不支持**的包、类、接口及方法，并说明原因与跨平台替代方案。
> 2. 本文档与 [`API_CHECKLIST.md`](API_CHECKLIST.md) 共同构成 `jako` 的**全量 JDK 符号账本**。每次执行 `./gradlew check`（或 `./gradlew checkApiCompatibility` 及 Git `pre-commit` 钩子）时，自动化校验工具 [`JdkSignatureParityTest`](jako-stdlib/src/jvmTest/kotlin/jako/test/parity/JdkSignatureParityTest.kt) 会通过 `JavaCompiler` (`--release <N>`) 扫描目标 JDK 的全部公开类，确保每一个 JDK 类要么已在 `commonMain` 实现并记录于 `API_CHECKLIST.md`，要么映射至 Kotlin 内建类型，要么明确登记于本黑名单中。
> 3. 当未来升级目标 JDK 版本（如 `./gradlew checkApiCompatibility -Pjako.jdkRelease=11`）时，校验工具会自动对比新版本 JDK 符号表并报告所有新增未分类的 API。

---

## 1. 为什么部分 JDK API 在 Kotlin Native 下不支持？

`jako` 的核心目标是提供通用的跨平台（JVM & Kotlin Native，后续扩展 Node.js/Wasm）基础库。由于 Kotlin Native 采用 **LLVM AOT（Ahead-of-Time）静态编译**为平台原生机器码，不存在 JVM 虚拟机字节码解释器与 JIT，以下几类重度绑定 JVM 运行时内部机制的 API 在架构上不适用于 Native：

| 类别 | 代表性 API | 不支持原因 | 推荐跨平台替代方案 |
| :--- | :--- | :--- | :--- |
| **运行时反射与动态代理** | `java.lang.reflect.Method`、`Field`、`Constructor`、`Proxy`、`Executable` | AOT 编译后不保留完整 Java 字节码方法表，无法在运行时按名称任意调用私有字段/方法或动态生成字节码代理类（注：`java.lang.reflect.Array` 与基础 `Class<T>` 已支持）。 | 使用编译期代码生成（KSP / Kotlin Compiler Plugin）、`kotlinx.serialization` 或显式工厂接口。 |
| **动态类加载器** | `java.lang.ClassLoader`、`URLClassLoader`、`SecureClassLoader`、`ServiceLoader` | Native 产物为单一可执行文件或动态库，不支持在运行时从 `.class` / `.jar` 动态加载字节码。 | 编译期静态链接依赖或显式注册组件。 |
| **JVM 安全管理器** | `java.lang.SecurityManager`、`AccessController`、`Policy`、`*Permission` | JVM 沙箱安全管理器（已在 JDK 17 废弃、JDK 24 移除）依赖 JVM 栈帧权限检查。 | 使用操作系统级进程权限或容器隔离。 |
| **JCE / JSSE SPI 扩展层** | `MessageDigestSpi`、`CipherSpi`、`SignatureSpi`、`SSLContextSpi`、`Provider` | Java 加密体系的动态 Provider 注册机制依赖类加载器与反射实例化。 | 直接使用高层 API：`MessageDigest`、`Cipher`、`Mac`、`Signature`、`KeyFactory`、`KeyPairGenerator`、`SSLContext`。 |
| **基于反射的原子字段更新器** | `AtomicIntegerFieldUpdater`、`AtomicLongFieldUpdater`、`AtomicReferenceFieldUpdater` | 依赖运行时按字段名反射获取对象内存偏移量（`sun.misc.Unsafe`）。 | 直接使用 `AtomicInteger`、`AtomicLong`、`AtomicReference` 或 Kotlin `atomicfu`。 |

---

## 2. 整包不支持的 JDK 8 模块与包 (Excluded Packages)

以下 JDK 8 包不属于通用跨平台核心标准库范围，整包不提供支持：

- **桌面 GUI 与图形图像**：`java.applet.*`、`java.awt.*`、`javax.swing.*`、`javax.imageio.*`、`javax.print.*`、`javax.sound.*`、`javax.accessibility.*`
- **JVM 字节码与虚拟机内部机制**：`java.lang.instrument.*`、`java.lang.invoke.*`、`java.lang.management.*`、`javax.management.*`
- **企业级与远程调用/数据库**：`java.beans.*`、`java.rmi.*`、`java.sql.*`、`javax.sql.*`、`javax.naming.*`、`javax.transaction.*`、`javax.activation.*`、`javax.activity.*`、`javax.ORB.*`、`org.omg.*`
- **编译器与注解处理器API**：`javax.tools.*`、`javax.lang.model.*`、`javax.annotation.processing.*`、`javax.script.*`
- **JDK 内置 XML / JAXP / CORBA**：`javax.xml.*`、`org.w3c.dom.*`、`org.xml.sax.*`（Android 端常用轻量 XML 解析通过 `jako-android` 的 `org.xmlpull.v1` 支持）

---

## 3. 已支持包内的不支持类黑名单 (Unsupported Classes in Tracked Packages)

> **机器可读规则**：以下每个小节按包名列出在当前目标 JDK（默认 JDK 8）中明确不实现的公开类或静态内部类（格式为 `` `fully.qualified.ClassName` ``）。[`JdkSignatureParityTest`](jako-stdlib/src/jvmTest/kotlin/jako/test/parity/JdkSignatureParityTest.kt) 在每次测试及提交检查时会自动解析本节内容。

### `java.io` (12)
- **原因说明**：JVM 特有的控制台交互 (`Console`)、安全权限 (`FilePermission`、`SerializablePermission`)、已废弃的旧流 (`LineNumberInputStream`、`StringBufferInputStream`) 及底层序列化描述符内部钩子。
- **不支持类列表**：
  - `java.io.Console` (CLASS)
  - `java.io.FilePermission` (CLASS)
  - `java.io.IOError` (CLASS)
  - `java.io.LineNumberInputStream` (CLASS)
  - `java.io.ObjectInputStream.GetField` (CLASS)
  - `java.io.ObjectInputValidation` (INTERFACE)
  - `java.io.ObjectOutputStream.PutField` (CLASS)
  - `java.io.ObjectStreamClass` (CLASS)
  - `java.io.ObjectStreamField` (CLASS)
  - `java.io.OptionalDataException` (CLASS)
  - `java.io.SerializablePermission` (CLASS)
  - `java.io.StringBufferInputStream` (CLASS)

### `java.lang` (39)
- **原因说明**：JVM 字节码加载器 (`ClassLoader`、`Package`)、安全管理器 (`SecurityManager`、`RuntimePermission`)、JVM 链接/字节码校验错误 (`LinkageError`、`VerifyError`、`NoClassDefFoundError` 等)、反射异常 (`IllegalAccessException`、`NoSuchMethodException`、`InstantiationException`) 以及需通过 `kotlin.text.StringBuilder` 使用的字符串构建器。
- **不支持类列表**：
  - `java.lang.AbstractMethodError` (CLASS)
  - `java.lang.BootstrapMethodError` (CLASS)
  - `java.lang.Character.Subset` (CLASS)
  - `java.lang.Character.UnicodeBlock` (CLASS)
  - `java.lang.Character.UnicodeScript` (ENUM)
  - `java.lang.ClassCircularityError` (CLASS)
  - `java.lang.ClassFormatError` (CLASS)
  - `java.lang.ClassLoader` (CLASS)
  - `java.lang.ClassValue` (CLASS)
  - `java.lang.Compiler` (CLASS)
  - `java.lang.EnumConstantNotPresentException` (CLASS)
  - `java.lang.ExceptionInInitializerError` (CLASS)
  - `java.lang.IllegalAccessError` (CLASS)
  - `java.lang.IllegalAccessException` (CLASS)
  - `java.lang.IncompatibleClassChangeError` (CLASS)
  - `java.lang.InstantiationError` (CLASS)
  - `java.lang.InstantiationException` (CLASS)
  - `java.lang.InternalError` (CLASS)
  - `java.lang.LinkageError` (CLASS)
  - `java.lang.NoClassDefFoundError` (CLASS)
  - `java.lang.NoSuchFieldError` (CLASS)
  - `java.lang.NoSuchMethodError` (CLASS)
  - `java.lang.NoSuchMethodException` (CLASS)
  - `java.lang.Package` (CLASS)
  - `java.lang.ProcessBuilder.Redirect` (CLASS)
  - `java.lang.ProcessBuilder.Redirect.Type` (ENUM)
  - `java.lang.RuntimePermission` (CLASS)
  - `java.lang.SecurityManager` (CLASS)
  - `java.lang.StrictMath` (CLASS)
  - `java.lang.StringBuffer` (CLASS)
  - `java.lang.StringBuilder` (CLASS)
  - `java.lang.ThreadDeath` (CLASS)
  - `java.lang.ThreadGroup` (CLASS)
  - `java.lang.TypeNotPresentException` (CLASS)
  - `java.lang.UnknownError` (CLASS)
  - `java.lang.UnsatisfiedLinkError` (CLASS)
  - `java.lang.UnsupportedClassVersionError` (CLASS)
  - `java.lang.VerifyError` (CLASS)
  - `java.lang.VirtualMachineError` (CLASS)

### `java.lang.reflect` (28)
- **原因说明**：Kotlin Native 采用 AOT 静态编译，不包含 JVM 字节码元数据、方法表动态分派或运行时动态代理生成，因此除 `java.lang.reflect.Array` 外，其余运行时反射 API (`Method`、`Field`、`Constructor`、`Proxy`、`Executable`、`Parameter`、`Type` 体系等) 均不提供。建议使用编译期代码生成（KSP / Kotlin 编译器插件）或 `kotlinx.serialization` 替代。
- **不支持类列表**：
  - `java.lang.reflect.AccessibleObject` (CLASS)
  - `java.lang.reflect.AnnotatedArrayType` (INTERFACE)
  - `java.lang.reflect.AnnotatedElement` (INTERFACE)
  - `java.lang.reflect.AnnotatedParameterizedType` (INTERFACE)
  - `java.lang.reflect.AnnotatedType` (INTERFACE)
  - `java.lang.reflect.AnnotatedTypeVariable` (INTERFACE)
  - `java.lang.reflect.AnnotatedWildcardType` (INTERFACE)
  - `java.lang.reflect.Constructor` (CLASS)
  - `java.lang.reflect.Executable` (CLASS)
  - `java.lang.reflect.Field` (CLASS)
  - `java.lang.reflect.GenericArrayType` (INTERFACE)
  - `java.lang.reflect.GenericDeclaration` (INTERFACE)
  - `java.lang.reflect.GenericSignatureFormatError` (CLASS)
  - `java.lang.reflect.InvocationHandler` (INTERFACE)
  - `java.lang.reflect.InvocationTargetException` (CLASS)
  - `java.lang.reflect.MalformedParameterizedTypeException` (CLASS)
  - `java.lang.reflect.MalformedParametersException` (CLASS)
  - `java.lang.reflect.Member` (INTERFACE)
  - `java.lang.reflect.Method` (CLASS)
  - `java.lang.reflect.Modifier` (CLASS)
  - `java.lang.reflect.Parameter` (CLASS)
  - `java.lang.reflect.ParameterizedType` (INTERFACE)
  - `java.lang.reflect.Proxy` (CLASS)
  - `java.lang.reflect.ReflectPermission` (CLASS)
  - `java.lang.reflect.Type` (INTERFACE)
  - `java.lang.reflect.TypeVariable` (INTERFACE)
  - `java.lang.reflect.UndeclaredThrowableException` (CLASS)
  - `java.lang.reflect.WildcardType` (INTERFACE)

### `java.net` (21)
- **原因说明**：自定义底层 Socket 实现工厂 (`SocketImpl`、`DatagramSocketImpl`)、协议流处理器扩展 (`URLStreamHandler`、`ContentHandler`)、动态字节码加载 (`URLClassLoader`)、多播套接字 (`MulticastSocket`) 及安全管理器权限类。
- **不支持类列表**：
  - `java.net.CacheRequest` (CLASS)
  - `java.net.CacheResponse` (CLASS)
  - `java.net.ContentHandler` (CLASS)
  - `java.net.ContentHandlerFactory` (INTERFACE)
  - `java.net.DatagramSocketImpl` (CLASS)
  - `java.net.DatagramSocketImplFactory` (INTERFACE)
  - `java.net.FileNameMap` (INTERFACE)
  - `java.net.JarURLConnection` (CLASS)
  - `java.net.MulticastSocket` (CLASS)
  - `java.net.NetPermission` (CLASS)
  - `java.net.ProtocolFamily` (INTERFACE)
  - `java.net.ResponseCache` (CLASS)
  - `java.net.SecureCacheResponse` (CLASS)
  - `java.net.SocketImpl` (CLASS)
  - `java.net.SocketImplFactory` (INTERFACE)
  - `java.net.SocketPermission` (CLASS)
  - `java.net.StandardProtocolFamily` (ENUM)
  - `java.net.URLClassLoader` (CLASS)
  - `java.net.URLPermission` (CLASS)
  - `java.net.URLStreamHandler` (CLASS)
  - `java.net.URLStreamHandlerFactory` (INTERFACE)

### `java.nio` (2)
- **原因说明**：依赖虚拟内存页映射与 JVM GC 堆外内存回收钩子的 `MappedByteBuffer`（建议使用 `FileChannel.read` / `write` 配合 `ByteBuffer`）及内部标记异常。
- **不支持类列表**：
  - `java.nio.InvalidMarkException` (CLASS)
  - `java.nio.MappedByteBuffer` (CLASS)

### `java.nio.channels` (17)
- **原因说明**：操作系统异步 I/O 通道组 (`AsynchronousChannel`、`AsynchronousFileChannel`、`AsynchronousSocketChannel`、`CompletionHandler`，建议使用 `Selector` 多路复用或协程/线程池)、内存映射模式 (`FileChannel.MapMode`) 及多播通道。
- **不支持类列表**：
  - `java.nio.channels.AcceptPendingException` (CLASS)
  - `java.nio.channels.AsynchronousByteChannel` (INTERFACE)
  - `java.nio.channels.AsynchronousChannel` (INTERFACE)
  - `java.nio.channels.AsynchronousChannelGroup` (CLASS)
  - `java.nio.channels.AsynchronousFileChannel` (CLASS)
  - `java.nio.channels.AsynchronousServerSocketChannel` (CLASS)
  - `java.nio.channels.AsynchronousSocketChannel` (CLASS)
  - `java.nio.channels.CompletionHandler` (INTERFACE)
  - `java.nio.channels.FileChannel.MapMode` (CLASS)
  - `java.nio.channels.FileLockInterruptionException` (CLASS)
  - `java.nio.channels.IllegalChannelGroupException` (CLASS)
  - `java.nio.channels.InterruptedByTimeoutException` (CLASS)
  - `java.nio.channels.MembershipKey` (CLASS)
  - `java.nio.channels.MulticastChannel` (INTERFACE)
  - `java.nio.channels.ReadPendingException` (CLASS)
  - `java.nio.channels.ShutdownChannelGroupException` (CLASS)
  - `java.nio.channels.WritePendingException` (CLASS)

### `java.nio.charset` (1)
- **原因说明**：罕见编码器内部故障错误 (`CoderMalfunctionError`)。
- **不支持类列表**：
  - `java.nio.charset.CoderMalfunctionError` (CLASS)

### `java.nio.file` (23)
- **原因说明**：依赖平台特定内核事件通知机制的 `WatchService` / `WatchKey` / `WatchEvent`、安全目录流 (`SecureDirectoryStream`)、文件系统存储卷 (`FileStore`) 及部分细粒度文件系统异常。
- **不支持类列表**：
  - `java.nio.file.AccessDeniedException` (CLASS)
  - `java.nio.file.AccessMode` (ENUM)
  - `java.nio.file.AtomicMoveNotSupportedException` (CLASS)
  - `java.nio.file.ClosedDirectoryStreamException` (CLASS)
  - `java.nio.file.ClosedFileSystemException` (CLASS)
  - `java.nio.file.ClosedWatchServiceException` (CLASS)
  - `java.nio.file.DirectoryIteratorException` (CLASS)
  - `java.nio.file.FileStore` (CLASS)
  - `java.nio.file.FileSystemAlreadyExistsException` (CLASS)
  - `java.nio.file.FileSystemLoopException` (CLASS)
  - `java.nio.file.FileSystemNotFoundException` (CLASS)
  - `java.nio.file.InvalidPathException` (CLASS)
  - `java.nio.file.LinkPermission` (CLASS)
  - `java.nio.file.NotLinkException` (CLASS)
  - `java.nio.file.ProviderMismatchException` (CLASS)
  - `java.nio.file.ProviderNotFoundException` (CLASS)
  - `java.nio.file.ReadOnlyFileSystemException` (CLASS)
  - `java.nio.file.SecureDirectoryStream` (INTERFACE)
  - `java.nio.file.StandardWatchEventKinds` (CLASS)
  - `java.nio.file.WatchEvent` (INTERFACE)
  - `java.nio.file.WatchKey` (INTERFACE)
  - `java.nio.file.WatchService` (INTERFACE)
  - `java.nio.file.Watchable` (INTERFACE)

### `java.nio.file.attribute` (19)
- **原因说明**：Windows ACL 权限视图 (`AclEntry`、`AclFileAttributeView`)、DOS 属性视图 (`DosFileAttributes`)、文件所有者主体查询 (`UserPrincipalLookupService`) 及 `AttributeView` 子接口体系（基础属性与 POSIX 权限已通过 `BasicFileAttributes` 和 `PosixFilePermissions` 支持）。
- **不支持类列表**：
  - `java.nio.file.attribute.AclEntry` (CLASS)
  - `java.nio.file.attribute.AclEntryFlag` (ENUM)
  - `java.nio.file.attribute.AclEntryPermission` (ENUM)
  - `java.nio.file.attribute.AclEntryType` (ENUM)
  - `java.nio.file.attribute.AclFileAttributeView` (INTERFACE)
  - `java.nio.file.attribute.AttributeView` (INTERFACE)
  - `java.nio.file.attribute.BasicFileAttributeView` (INTERFACE)
  - `java.nio.file.attribute.DosFileAttributeView` (INTERFACE)
  - `java.nio.file.attribute.DosFileAttributes` (INTERFACE)
  - `java.nio.file.attribute.FileAttributeView` (INTERFACE)
  - `java.nio.file.attribute.FileOwnerAttributeView` (INTERFACE)
  - `java.nio.file.attribute.FileStoreAttributeView` (INTERFACE)
  - `java.nio.file.attribute.GroupPrincipal` (INTERFACE)
  - `java.nio.file.attribute.PosixFileAttributeView` (INTERFACE)
  - `java.nio.file.attribute.PosixFileAttributes` (INTERFACE)
  - `java.nio.file.attribute.UserDefinedFileAttributeView` (INTERFACE)
  - `java.nio.file.attribute.UserPrincipal` (INTERFACE)
  - `java.nio.file.attribute.UserPrincipalLookupService` (CLASS)
  - `java.nio.file.attribute.UserPrincipalNotFoundException` (CLASS)

### `java.security` (63)
- **原因说明**：JVM 安全管理器与访问控制体系 (`AccessController`、`AccessControlContext`、`Policy`、`ProtectionDomain`、`Permission` 体系)、JCE 动态 Provider SPI 抽象基类 (`*Spi`、`Provider`、`Security`)、代码签名 (`CodeSigner`、`Timestamp`) 及复杂 `KeyStore` 保护参数。
- **不支持类列表**：
  - `java.security.AccessControlContext` (CLASS)
  - `java.security.AccessControlException` (CLASS)
  - `java.security.AccessController` (CLASS)
  - `java.security.AlgorithmConstraints` (INTERFACE)
  - `java.security.AlgorithmParameterGenerator` (CLASS)
  - `java.security.AlgorithmParameterGeneratorSpi` (CLASS)
  - `java.security.AlgorithmParameters` (CLASS)
  - `java.security.AlgorithmParametersSpi` (CLASS)
  - `java.security.AllPermission` (CLASS)
  - `java.security.AuthProvider` (CLASS)
  - `java.security.BasicPermission` (CLASS)
  - `java.security.Certificate` (INTERFACE)
  - `java.security.CodeSigner` (CLASS)
  - `java.security.CodeSource` (CLASS)
  - `java.security.CryptoPrimitive` (ENUM)
  - `java.security.DigestInputStream` (CLASS)
  - `java.security.DigestOutputStream` (CLASS)
  - `java.security.DomainCombiner` (INTERFACE)
  - `java.security.DomainLoadStoreParameter` (CLASS)
  - `java.security.Guard` (INTERFACE)
  - `java.security.GuardedObject` (CLASS)
  - `java.security.Identity` (CLASS)
  - `java.security.IdentityScope` (CLASS)
  - `java.security.KeyFactorySpi` (CLASS)
  - `java.security.KeyPairGeneratorSpi` (CLASS)
  - `java.security.KeyRep` (CLASS)
  - `java.security.KeyStore.Builder` (CLASS)
  - `java.security.KeyStore.CallbackHandlerProtection` (CLASS)
  - `java.security.KeyStore.Entry` (INTERFACE)
  - `java.security.KeyStore.Entry.Attribute` (INTERFACE)
  - `java.security.KeyStore.LoadStoreParameter` (INTERFACE)
  - `java.security.KeyStore.PasswordProtection` (CLASS)
  - `java.security.KeyStore.PrivateKeyEntry` (CLASS)
  - `java.security.KeyStore.ProtectionParameter` (INTERFACE)
  - `java.security.KeyStore.SecretKeyEntry` (CLASS)
  - `java.security.KeyStore.TrustedCertificateEntry` (CLASS)
  - `java.security.KeyStoreSpi` (CLASS)
  - `java.security.MessageDigestSpi` (CLASS)
  - `java.security.NoSuchProviderException` (CLASS)
  - `java.security.PKCS12Attribute` (CLASS)
  - `java.security.Permission` (CLASS)
  - `java.security.PermissionCollection` (CLASS)
  - `java.security.Permissions` (CLASS)
  - `java.security.Policy` (CLASS)
  - `java.security.PolicySpi` (CLASS)
  - `java.security.PrivilegedAction` (INTERFACE)
  - `java.security.PrivilegedActionException` (CLASS)
  - `java.security.PrivilegedExceptionAction` (INTERFACE)
  - `java.security.ProtectionDomain` (CLASS)
  - `java.security.Provider` (CLASS)
  - `java.security.ProviderException` (CLASS)
  - `java.security.SecureClassLoader` (CLASS)
  - `java.security.SecureRandomSpi` (CLASS)
  - `java.security.Security` (CLASS)
  - `java.security.SecurityPermission` (CLASS)
  - `java.security.SignatureSpi` (CLASS)
  - `java.security.SignedObject` (CLASS)
  - `java.security.Signer` (CLASS)
  - `java.security.Timestamp` (CLASS)
  - `java.security.URIParameter` (CLASS)
  - `java.security.UnrecoverableEntryException` (CLASS)
  - `java.security.UnrecoverableKeyException` (CLASS)
  - `java.security.UnresolvedPermission` (CLASS)

### `java.security.cert` (41)
- **原因说明**：PKIX 证书路径构建与吊销校验状态机 (`CertPathBuilder`、`CertPathValidator`、`PKIXParameters`、`X509CRL`、`CertStore` 等) 及 SPI 抽象类（标准 X.509 证书解析已通过 `CertificateFactory` 与 `X509Certificate` 支持）。
- **不支持类列表**：
  - `java.security.cert.CRL` (CLASS)
  - `java.security.cert.CRLException` (CLASS)
  - `java.security.cert.CRLReason` (ENUM)
  - `java.security.cert.CRLSelector` (INTERFACE)
  - `java.security.cert.CertPath` (CLASS)
  - `java.security.cert.CertPathBuilder` (CLASS)
  - `java.security.cert.CertPathBuilderException` (CLASS)
  - `java.security.cert.CertPathBuilderResult` (INTERFACE)
  - `java.security.cert.CertPathBuilderSpi` (CLASS)
  - `java.security.cert.CertPathChecker` (INTERFACE)
  - `java.security.cert.CertPathParameters` (INTERFACE)
  - `java.security.cert.CertPathValidator` (CLASS)
  - `java.security.cert.CertPathValidatorException` (CLASS)
  - `java.security.cert.CertPathValidatorResult` (INTERFACE)
  - `java.security.cert.CertPathValidatorSpi` (CLASS)
  - `java.security.cert.CertSelector` (INTERFACE)
  - `java.security.cert.CertStore` (CLASS)
  - `java.security.cert.CertStoreException` (CLASS)
  - `java.security.cert.CertStoreParameters` (INTERFACE)
  - `java.security.cert.CertStoreSpi` (CLASS)
  - `java.security.cert.Certificate.CertificateRep` (CLASS)
  - `java.security.cert.CertificateFactorySpi` (CLASS)
  - `java.security.cert.CertificateRevokedException` (CLASS)
  - `java.security.cert.CollectionCertStoreParameters` (CLASS)
  - `java.security.cert.Extension` (INTERFACE)
  - `java.security.cert.LDAPCertStoreParameters` (CLASS)
  - `java.security.cert.PKIXBuilderParameters` (CLASS)
  - `java.security.cert.PKIXCertPathBuilderResult` (CLASS)
  - `java.security.cert.PKIXCertPathChecker` (CLASS)
  - `java.security.cert.PKIXCertPathValidatorResult` (CLASS)
  - `java.security.cert.PKIXParameters` (CLASS)
  - `java.security.cert.PKIXReason` (ENUM)
  - `java.security.cert.PKIXRevocationChecker` (CLASS)
  - `java.security.cert.PolicyNode` (INTERFACE)
  - `java.security.cert.PolicyQualifierInfo` (CLASS)
  - `java.security.cert.TrustAnchor` (CLASS)
  - `java.security.cert.X509CRL` (CLASS)
  - `java.security.cert.X509CRLEntry` (CLASS)
  - `java.security.cert.X509CRLSelector` (CLASS)
  - `java.security.cert.X509CertSelector` (CLASS)
  - `java.security.cert.X509Extension` (INTERFACE)

### `java.security.interfaces` (10)
- **原因说明**：DSA 密钥接口 (`DSAKey`、`DSAPublicKey`、`DSAPrivateKey`)、EC 曲线点接口 (`ECPublicKey`、`ECPrivateKey`；EC 密钥生成、签名与编码已通过 `KeyPairGenerator`/`KeyFactory`/`Signature` 支持) 及多重素数 RSA CRT 接口。
- **不支持类列表**：
  - `java.security.interfaces.DSAKey` (INTERFACE)
  - `java.security.interfaces.DSAKeyPairGenerator` (INTERFACE)
  - `java.security.interfaces.DSAParams` (INTERFACE)
  - `java.security.interfaces.DSAPrivateKey` (INTERFACE)
  - `java.security.interfaces.DSAPublicKey` (INTERFACE)
  - `java.security.interfaces.ECKey` (INTERFACE)
  - `java.security.interfaces.ECPrivateKey` (INTERFACE)
  - `java.security.interfaces.ECPublicKey` (INTERFACE)
  - `java.security.interfaces.RSAMultiPrimePrivateCrtKey` (INTERFACE)
  - `java.security.interfaces.RSAPrivateCrtKey` (INTERFACE)

### `java.security.spec` (18)
- **原因说明**：DSA 参数规范 (`DSAParameterSpec` 等)、底层椭圆曲线有限域参数 (`EllipticCurve`、`ECField`、`ECPoint`；标准命名曲线已通过 `ECGenParameterSpec` 支持) 及 PSS/OAEP 高级参数。
- **不支持类列表**：
  - `java.security.spec.DSAGenParameterSpec` (CLASS)
  - `java.security.spec.DSAParameterSpec` (CLASS)
  - `java.security.spec.DSAPrivateKeySpec` (CLASS)
  - `java.security.spec.DSAPublicKeySpec` (CLASS)
  - `java.security.spec.ECField` (INTERFACE)
  - `java.security.spec.ECFieldF2m` (CLASS)
  - `java.security.spec.ECFieldFp` (CLASS)
  - `java.security.spec.ECParameterSpec` (CLASS)
  - `java.security.spec.ECPoint` (CLASS)
  - `java.security.spec.ECPrivateKeySpec` (CLASS)
  - `java.security.spec.ECPublicKeySpec` (CLASS)
  - `java.security.spec.EllipticCurve` (CLASS)
  - `java.security.spec.MGF1ParameterSpec` (CLASS)
  - `java.security.spec.PSSParameterSpec` (CLASS)
  - `java.security.spec.RSAKeyGenParameterSpec` (CLASS)
  - `java.security.spec.RSAMultiPrimePrivateCrtKeySpec` (CLASS)
  - `java.security.spec.RSAOtherPrimeInfo` (CLASS)
  - `java.security.spec.RSAPrivateCrtKeySpec` (CLASS)

### `java.text` (9)
- **原因说明**：富文本属性字符迭代器 (`AttributedString`、`AttributedCharacterIterator`、`Annotation`)、格式化字段常量子类 (`Format.Field` 子类) 及自定义规则排序 (`RuleBasedCollator`，通用排序已通过 `Collator` 支持)。
- **不支持类列表**：
  - `java.text.Annotation` (CLASS)
  - `java.text.AttributedCharacterIterator` (INTERFACE)
  - `java.text.AttributedString` (CLASS)
  - `java.text.CollationElementIterator` (CLASS)
  - `java.text.DateFormat.Field` (CLASS)
  - `java.text.Format.Field` (CLASS)
  - `java.text.MessageFormat.Field` (CLASS)
  - `java.text.NumberFormat.Field` (CLASS)
  - `java.text.RuleBasedCollator` (CLASS)

### `java.time.format` (6)
- **原因说明**：复杂自定义格式化构建器 (`DateTimeFormatterBuilder`) 及本地化样式枚举 (`FormatStyle`、`TextStyle`、`ResolverStyle`；标准模式已通过 `DateTimeFormatter.ofPattern` 支持)。
- **不支持类列表**：
  - `java.time.format.DateTimeFormatterBuilder` (CLASS)
  - `java.time.format.DecimalStyle` (CLASS)
  - `java.time.format.FormatStyle` (ENUM)
  - `java.time.format.ResolverStyle` (ENUM)
  - `java.time.format.SignStyle` (ENUM)
  - `java.time.format.TextStyle` (ENUM)

### `java.time.temporal` (5)
- **原因说明**：ISO 周历/儒略日扩展字段 (`IsoFields`、`JulianFields`、`WeekFields`) 及 `TemporalQuery` 函数式查询接口。
- **不支持类列表**：
  - `java.time.temporal.IsoFields` (CLASS)
  - `java.time.temporal.JulianFields` (CLASS)
  - `java.time.temporal.TemporalQueries` (CLASS)
  - `java.time.temporal.TemporalQuery` (INTERFACE)
  - `java.time.temporal.WeekFields` (CLASS)

### `java.util` (21)
- **原因说明**：依赖类路径动态加载的国际化资源包 (`ResourceBundle`、`ListResourceBundle`、`PropertyResourceBundle`)、SPI 服务发现 (`ServiceLoader`，依赖运行时反射与类加载器)、`Locale.Builder` / `Calendar.Builder` 及 `Spliterators` 抽象辅助基类。
- **不支持类列表**：
  - `java.util.AbstractMap.SimpleEntry` (CLASS)
  - `java.util.AbstractMap.SimpleImmutableEntry` (CLASS)
  - `java.util.Calendar.Builder` (CLASS)
  - `java.util.Formatter.BigDecimalLayoutForm` (ENUM)
  - `java.util.ListResourceBundle` (CLASS)
  - `java.util.Locale.Builder` (CLASS)
  - `java.util.Locale.Category` (ENUM)
  - `java.util.Locale.FilteringMode` (ENUM)
  - `java.util.Locale.LanguageRange` (CLASS)
  - `java.util.PropertyPermission` (CLASS)
  - `java.util.PropertyResourceBundle` (CLASS)
  - `java.util.ResourceBundle` (CLASS)
  - `java.util.ServiceConfigurationError` (CLASS)
  - `java.util.ServiceLoader` (CLASS)
  - `java.util.SimpleTimeZone` (CLASS)
  - `java.util.Spliterators.AbstractDoubleSpliterator` (CLASS)
  - `java.util.Spliterators.AbstractIntSpliterator` (CLASS)
  - `java.util.Spliterators.AbstractLongSpliterator` (CLASS)
  - `java.util.Spliterators.AbstractSpliterator` (CLASS)
  - `java.util.SplittableRandom` (CLASS)
  - `java.util.UnknownFormatFlagsException` (CLASS)

### `java.util.concurrent` (12)
- **原因说明**：线程池拒绝策略静态内部类 (`ThreadPoolExecutor.*Policy`)、`ForkJoinWorkerThread` 自定义工作线程子类化及 `ConcurrentMap` / `ConcurrentNavigableMap` 标记父接口（`ConcurrentHashMap` 与 `ConcurrentSkipListMap` 已直接实现 `MutableMap` / `NavigableMap`）。
- **不支持类列表**：
  - `java.util.concurrent.CompletableFuture.AsynchronousCompletionTask` (INTERFACE)
  - `java.util.concurrent.ConcurrentHashMap.KeySetView` (CLASS)
  - `java.util.concurrent.ConcurrentMap` (INTERFACE)
  - `java.util.concurrent.ConcurrentNavigableMap` (INTERFACE)
  - `java.util.concurrent.ForkJoinPool.ForkJoinWorkerThreadFactory` (INTERFACE)
  - `java.util.concurrent.ForkJoinWorkerThread` (CLASS)
  - `java.util.concurrent.RejectedExecutionHandler` (INTERFACE)
  - `java.util.concurrent.RunnableScheduledFuture` (INTERFACE)
  - `java.util.concurrent.ThreadPoolExecutor.AbortPolicy` (CLASS)
  - `java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy` (CLASS)
  - `java.util.concurrent.ThreadPoolExecutor.DiscardOldestPolicy` (CLASS)
  - `java.util.concurrent.ThreadPoolExecutor.DiscardPolicy` (CLASS)

### `java.util.concurrent.atomic` (5)
- **原因说明**：基于运行时字段反射的原子更新器 (`AtomicIntegerFieldUpdater`、`AtomicLongFieldUpdater`、`AtomicReferenceFieldUpdater`，请直接使用 `AtomicInteger` / `AtomicLong` / `AtomicReference`) 及函数式累加器 (`LongAccumulator`、`DoubleAccumulator`)。
- **不支持类列表**：
  - `java.util.concurrent.atomic.AtomicIntegerFieldUpdater` (CLASS)
  - `java.util.concurrent.atomic.AtomicLongFieldUpdater` (CLASS)
  - `java.util.concurrent.atomic.AtomicReferenceFieldUpdater` (CLASS)
  - `java.util.concurrent.atomic.DoubleAccumulator` (CLASS)
  - `java.util.concurrent.atomic.LongAccumulator` (CLASS)

### `java.util.concurrent.locks` (3)
- **原因说明**：AQS 底层同步器框架基类 (`AbstractQueuedSynchronizer`、`AbstractQueuedLongSynchronizer`、`AbstractOwnableSynchronizer`；高层锁 `ReentrantLock`、`ReentrantReadWriteLock`、`StampedLock`、`Semaphore`、`CountDownLatch` 已基于平台原语直接实现)。
- **不支持类列表**：
  - `java.util.concurrent.locks.AbstractOwnableSynchronizer` (CLASS)
  - `java.util.concurrent.locks.AbstractQueuedLongSynchronizer` (CLASS)
  - `java.util.concurrent.locks.AbstractQueuedSynchronizer` (CLASS)

### `java.util.jar` (1)
- **原因说明**：`Pack200` 压缩工具（已在后续 JDK 14 中被官方移除）。
- **不支持类列表**：
  - `java.util.jar.Pack200` (CLASS)

### `java.util.logging` (11)
- **原因说明**：文件/Socket/控制台/内存日志 Handler 子类 (`FileHandler`、`SocketHandler`、`ConsoleHandler`、`StreamHandler`)、JMX 管理接口 (`LoggingMXBean`) 及全局 `LogManager`。
- **不支持类列表**：
  - `java.util.logging.ConsoleHandler` (CLASS)
  - `java.util.logging.ErrorManager` (CLASS)
  - `java.util.logging.FileHandler` (CLASS)
  - `java.util.logging.Filter` (INTERFACE)
  - `java.util.logging.LogManager` (CLASS)
  - `java.util.logging.LoggingMXBean` (INTERFACE)
  - `java.util.logging.LoggingPermission` (CLASS)
  - `java.util.logging.MemoryHandler` (CLASS)
  - `java.util.logging.SocketHandler` (CLASS)
  - `java.util.logging.StreamHandler` (CLASS)
  - `java.util.logging.XMLFormatter` (CLASS)

### `java.util.zip` (1)
- **原因说明**：已废弃的 `ZipError`。
- **不支持类列表**：
  - `java.util.zip.ZipError` (CLASS)

### `javax.crypto` (14)
- **原因说明**：JCE SPI 抽象类 (`CipherSpi`、`MacSpi`、`KeyGeneratorSpi`、`KeyAgreementSpi`、`SecretKeyFactorySpi`)、密钥协商 (`KeyAgreement`)、密码流包装器 (`CipherInputStream`、`CipherOutputStream`)、加密对象封装 (`SealedObject`、`EncryptedPrivateKeyInfo`) 及司法管辖豁免机制 (`ExemptionMechanism`)。
- **不支持类列表**：
  - `javax.crypto.CipherInputStream` (CLASS)
  - `javax.crypto.CipherOutputStream` (CLASS)
  - `javax.crypto.CipherSpi` (CLASS)
  - `javax.crypto.EncryptedPrivateKeyInfo` (CLASS)
  - `javax.crypto.ExemptionMechanism` (CLASS)
  - `javax.crypto.ExemptionMechanismException` (CLASS)
  - `javax.crypto.ExemptionMechanismSpi` (CLASS)
  - `javax.crypto.KeyAgreement` (CLASS)
  - `javax.crypto.KeyAgreementSpi` (CLASS)
  - `javax.crypto.KeyGeneratorSpi` (CLASS)
  - `javax.crypto.MacSpi` (CLASS)
  - `javax.crypto.NullCipher` (CLASS)
  - `javax.crypto.SealedObject` (CLASS)
  - `javax.crypto.SecretKeyFactorySpi` (CLASS)

### `javax.crypto.spec` (11)
- **原因说明**：不推荐的旧对称算法规范 (`DESKeySpec`、`DESedeKeySpec`、`RC2ParameterSpec`、`RC5ParameterSpec`)、Diffie-Hellman 参数规范 (`DHParameterSpec`) 及 `OAEPParameterSpec` / `PBEParameterSpec`。
- **不支持类列表**：
  - `javax.crypto.spec.DESKeySpec` (CLASS)
  - `javax.crypto.spec.DESedeKeySpec` (CLASS)
  - `javax.crypto.spec.DHGenParameterSpec` (CLASS)
  - `javax.crypto.spec.DHParameterSpec` (CLASS)
  - `javax.crypto.spec.DHPrivateKeySpec` (CLASS)
  - `javax.crypto.spec.DHPublicKeySpec` (CLASS)
  - `javax.crypto.spec.OAEPParameterSpec` (CLASS)
  - `javax.crypto.spec.PBEParameterSpec` (CLASS)
  - `javax.crypto.spec.PSource` (CLASS)
  - `javax.crypto.spec.RC2ParameterSpec` (CLASS)
  - `javax.crypto.spec.RC5ParameterSpec` (CLASS)

### `javax.net.ssl` (25)
- **原因说明**：非阻塞状态机 `SSLEngine`、服务端 TLS 监听 (`SSLServerSocket`、`SSLServerSocketFactory`)、SSL SPI 抽象类 (`SSLContextSpi`、`KeyManagerFactorySpi`、`TrustManagerFactorySpi`) 及握手监听器。
- **不支持类列表**：
  - `javax.net.ssl.CertPathTrustManagerParameters` (CLASS)
  - `javax.net.ssl.HandshakeCompletedEvent` (CLASS)
  - `javax.net.ssl.HandshakeCompletedListener` (INTERFACE)
  - `javax.net.ssl.KeyManagerFactory` (CLASS)
  - `javax.net.ssl.KeyManagerFactorySpi` (CLASS)
  - `javax.net.ssl.KeyStoreBuilderParameters` (CLASS)
  - `javax.net.ssl.ManagerFactoryParameters` (INTERFACE)
  - `javax.net.ssl.SNIMatcher` (CLASS)
  - `javax.net.ssl.SNIServerName` (CLASS)
  - `javax.net.ssl.SSLContextSpi` (CLASS)
  - `javax.net.ssl.SSLEngine` (CLASS)
  - `javax.net.ssl.SSLEngineResult` (CLASS)
  - `javax.net.ssl.SSLKeyException` (CLASS)
  - `javax.net.ssl.SSLParameters` (CLASS)
  - `javax.net.ssl.SSLPermission` (CLASS)
  - `javax.net.ssl.SSLServerSocket` (CLASS)
  - `javax.net.ssl.SSLServerSocketFactory` (CLASS)
  - `javax.net.ssl.SSLSessionBindingEvent` (CLASS)
  - `javax.net.ssl.SSLSessionBindingListener` (INTERFACE)
  - `javax.net.ssl.SSLSessionContext` (INTERFACE)
  - `javax.net.ssl.StandardConstants` (CLASS)
  - `javax.net.ssl.TrustManagerFactorySpi` (CLASS)
  - `javax.net.ssl.X509ExtendedKeyManager` (CLASS)
  - `javax.net.ssl.X509ExtendedTrustManager` (CLASS)
  - `javax.net.ssl.X509KeyManager` (INTERFACE)

### `javax.security.auth.x500` (1)
- **原因说明**：依赖私钥销毁接口的 `X500PrivateCredential`。
- **不支持类列表**：
  - `javax.security.auth.x500.X500PrivateCredential` (CLASS)

---

## 4. 已支持类中的不支持方法与目标平台特定限制 (Unsupported Methods on Supported Classes & Target-Specific Limitations)

> **运行时标准化异常约定**：
> 对于在已支持类中因特定目标平台运行时架构限制而无法完整提供的方法（例如在 `commonMain` 声明但在 `JS/Wasm` 单线程模型下无法执行的 API），`jako` **严禁采用静默空实现（No-op）或伪造错误语义**，而是在调用时统一抛出带标准化诊断前缀的 `UnsupportedOperationException`：
> ```text
> [jako] API '<ClassName>.<methodName>(...)' is not supported on target '<Target>': <Reason>. Recommended alternative: <Alternative>.
> ```

### 4.1 全非 JVM 平台（Native & JS/Wasm）均不包含的重度 JVM 耦合方法

部分已支持的重点类（如 `java.lang.Class`、`java.lang.System`、`java.lang.Thread`、`java.nio.channels.FileChannel`）受限于非 JVM 运行时机制，仅提供核心跨平台方法，不包含以下重度依赖 JVM 字节码/类加载器的方法：

| 所属类 | 不支持的方法 | 原因与替代说明 |
| :--- | :--- | :--- |
| `java.lang.Class<T>` | `getMethods()`、`getDeclaredMethods()`、`getMethod(...)`、`getFields()`、`getDeclaredFields()`、`getField(...)`、`getConstructors()`、`getDeclaredConstructors()`、`getConstructor(...)`、`newInstance()`、`getClassLoader()`、`getResource()`、`getResourceAsStream()`、`getAnnotations()` | 依赖 JVM 运行时反射元数据与 `ClassLoader`。`Class<T>` 在 `jako` 中仅保留类型标识、名称查询（`getName`、`getSimpleName`、`getCanonicalName`）、数组/基本类型判断（`isArray`、`isPrimitive`、`getComponentType`）与安全转换（`isInstance`、`cast`）。 |
| `java.lang.System` | `getSecurityManager()`、`setSecurityManager()`、`load(filename)`、`loadLibrary(libname)`、`runFinalization()`、`inheritedChannel()` | Native 无 JVM `SecurityManager` 与 Finalizer 线程；C 库链接在 Kotlin Native 编译期通过 `cinterop` 完成。 |
| `java.lang.Thread` | `getContextClassLoader()`、`setContextClassLoader()`、`getThreadGroup()`、`stop()`、`suspend()`、`resume()`、`countStackFrames()` | Native 无 `ClassLoader` 与 `ThreadGroup`，且 `stop`/`suspend`/`resume` 为 JDK 已废弃的不安全方法。 |
| `java.nio.channels.FileChannel` | `map(MapMode, position, size): MappedByteBuffer` | 依赖 `MappedByteBuffer` 虚拟内存映射与 JVM DirectBuffer Cleaner 机制；请使用 `FileChannel.read(ByteBuffer, position)` / `write(ByteBuffer, position)`。 |

### 4.2 `JS` / `WasmJs` 单线程平台特定不支持的方法 (Single-Threaded Web Target Limitations)

JavaScript (`jsMain`) 与 WebAssembly (`wasmJsMain`) 运行时采用单线程事件循环与无共享内存对象模型（Web Workers 仅支持 `SharedArrayBuffer` 原始字节共享，不支持共享 Kotlin 对象图）。因此以下依赖抢占式共享内存多线程的方法在 `JS/Wasm` 目标上调用时会通过 `unsupportedOnTarget(...)` 抛出标准化 `UnsupportedOperationException`：

| 所属类 | 目标平台不支持的方法 | 抛出异常信息与推荐跨平台替代方案 |
| :--- | :--- | :--- |
| `java.lang.Thread` | `start()` | `[jako] API 'java.lang.Thread.start()' is not supported on target 'JS/Wasm': JavaScript and WebAssembly runtimes are single-threaded and do not support preemptive shared-memory threads. Recommended alternative: kotlinx.coroutines or java.util.concurrent.Executors / HandlerThread.`（注：`Thread.currentThread()`、`Thread.sleep()`、`ThreadLocal`、可重入 `synchronized` / `ReentrantLock`、同线程内 `ExecutorService` / `ScheduledExecutorService` / `ForkJoinPool` / `Timer` / `android.os.HandlerThread` 均已在 `webMain` 支持。） |

---

## 5. `jako-android` 明确不移植的 Android Framework API (Excluded Android APIs)

除 JDK 标准库外，`jako-android` 模块对以下设计陈旧/糟糕或强绑定 Android 专有内核/渲染管线的 Android API **明确不予移植**：

| 类别与包名 | 明确不移植的 API | 原因说明与推荐跨平台替代方案 |
| :--- | :--- | :--- |
| **设计糟糕的遗留异步工具** (`android.os`) | `AsyncTask`、`CountDownTimer` | **`AsyncTask` 属于设计本身极其糟糕的遗留 API**（易引发内存/生命周期泄漏、线程调度语义僵化、异常处理缺失，Android 官方已于 API 30 彻底废弃），移植毫无意义。请直接使用 **Kotlin Coroutines (`kotlinx.coroutines`)** 或 `java.util.concurrent.Executors` / `Handler`。 |
| **设计糟糕的原生 SQLite / 游标体系** (`android.database`、`android.database.sqlite`、`android.content`) | `SQLiteDatabase`、`SQLiteOpenHelper`、`SQLiteStatement`、`Cursor`、`MatrixCursor`、`AbstractCursor`、`ContentValues` | **Android 原生 `SQLiteDatabase` + `Cursor` + `ContentValues` API 设计陈旧且脆弱**（无类型安全、依赖易错的列索引与状态化游标关闭管理、`ContentValues` 弱类型键值堆砌），移植无实际价值。`Context.getDatabasePath` / `databaseList` / `deleteDatabase` 已提供用于管理数据库文件路径；数据库访问请直接使用 **Room KMP (`androidx.room`)** 或 **SQLDelight**。 |
| **非公开系统属性** (`android.os`) | `SystemProperties` | AOSP `@hide` 内部隐藏 API，底层直接耦合 Android `bionic` `/dev/__properties__` 共享内存，非公开 SDK 标准接口。请使用 `System.getProperty(...)`、`System.getenv(...)` 或 `android.os.Build`。 |
| **Linux `inotify` 文件监听** (`android.os`) | `FileObserver` | 常量与事件语义直接暴露 Linux `inotify` 掩码，无法在 macOS (`FSEvents`/`kqueue`) 与 Windows (`ReadDirectoryChangesW`) 上提供一致语义。 |
| **位图编解码与 Android UI 渲染** (`android.graphics`、`android.view`) | `Bitmap`、`BitmapFactory`、`Canvas`、`Paint`、`Drawable`、`View`、`ViewGroup`、`Window` | 强耦合 Android `libhwui` / `Skia` C++ 渲染管线（纯数据/几何计算类 `Point`、`PointF`、`Rect`、`RectF`、`Matrix`、`Color` 已完整支持）。跨平台 UI 与图像加载请使用 **Compose Multiplatform**、**Skiko** 或 **Coil 3 KMP**。 |
| **Android 富文本 Span 体系** (`android.text`、`android.text.style`) | `Spanned`、`Spannable`、`SpannableString`、`SpannableStringBuilder`、`Editable`、`ForegroundColorSpan`、`StyleSpan`、`ClickableSpan`、`Html` | `Spannable` 及 `android.text.style.*` 下的 Span 实现（依赖 `TextPaint`、`Canvas`、`View`）唯一消费端是 Android 原生 `TextView` / `StaticLayout` 排版引擎；在跨平台场景（Compose Multiplatform 使用 `AnnotatedString`、iOS 使用 `NSAttributedString`）下构造出的 `SpannableString` 没有 UI 消费端，因此不予移植。跨平台富文本请使用 **`androidx.compose.ui.text.AnnotatedString`**。 |
| **Android 四大组件与 Binder IPC** (`android.app`、`android.content`) | `Activity`、`Service`、`BroadcastReceiver`、`ContentProvider`、`ContentResolver`、`Intent`、`Binder`、`IBinder` | 强耦合 Android `Zygote` / `AMS` 进程模型与 Linux `binder` 驱动（应用上下文与环境管理类 `Context`、`ContextWrapper`、`Application`、`SharedPreferences`、`AssetManager`、`Resources`、`PackageManager` 已完整支持）。 |
