# `jako` Public Class Test Coverage Ledger (公开类测试覆盖率账本)

> **自动生成说明**：本文件由 `./gradlew dumpTestCoverage`（`JdkSignatureParityTest.testPublicClassCoverageLedger`）自动扫描 `commonMain` 公开类型与 `commonTest` 引用关系生成，请勿手动编辑。

## 1. 总体概览 (Overall Summary)

| 模块 (Module) | 公开类型总数 | 已有直接测试 | 待补测核心类 (Actionable) | 豁免基础接口/标记接口 | 未显式引用异常类 | 核心类覆盖率 |
| :--- | ---: | ---: | ---: | ---: | ---: | ---: |
| `jako-stdlib` | 631 | 523 | **0** | 54 | 54 | 100% (523/523) |
| `jako-android` | 90 | 90 | **0** | 0 | 0 | 100% (90/90) |
| **合计 (Total)** | **721** | **613** | **0** | **54** | **54** | **100% (613/613)** |

## 2. 按包分类统计 (Coverage by Package)

| 模块 | 包名 (Package) | 总类型数 | 已测试 | 待补测核心类 | 待补测清单 |
| :--- | :--- | ---: | ---: | ---: | :--- |
| `jako-android` | `android.app` | 1 | 1 | 0 | ✅ 全部覆盖 |
| `jako-android` | `android.content` | 5 | 5 | 0 | ✅ 全部覆盖 |
| `jako-android` | `android.content.pm` | 5 | 5 | 0 | ✅ 全部覆盖 |
| `jako-android` | `android.content.res` | 4 | 4 | 0 | ✅ 全部覆盖 |
| `jako-android` | `android.graphics` | 7 | 7 | 0 | ✅ 全部覆盖 |
| `jako-android` | `android.net` | 2 | 2 | 0 | ✅ 全部覆盖 |
| `jako-android` | `android.os` | 24 | 24 | 0 | ✅ 全部覆盖 |
| `jako-android` | `android.preference` | 1 | 1 | 0 | ✅ 全部覆盖 |
| `jako-android` | `android.text` | 1 | 1 | 0 | ✅ 全部覆盖 |
| `jako-android` | `android.text.format` | 2 | 2 | 0 | ✅ 全部覆盖 |
| `jako-android` | `android.util` | 24 | 24 | 0 | ✅ 全部覆盖 |
| `jako-android` | `android.webkit` | 2 | 2 | 0 | ✅ 全部覆盖 |
| `jako-android` | `com.bennyhuo.jako` | 2 | 2 | 0 | ✅ 全部覆盖 |
| `jako-android` | `kotlinx.parcelize` | 1 | 1 | 0 | ✅ 全部覆盖 |
| `jako-android` | `org.json` | 5 | 5 | 0 | ✅ 全部覆盖 |
| `jako-android` | `org.xmlpull.v1` | 4 | 4 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `com.bennyhuo.jako.annotations` | 1 | 1 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `java.io` | 69 | 65 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `java.lang` | 29 | 26 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `java.lang.ref` | 5 | 5 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `java.lang.reflect` | 1 | 1 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `java.math` | 4 | 4 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `java.net` | 44 | 39 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `java.nio` | 12 | 11 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `java.nio.channels` | 39 | 20 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `java.nio.charset` | 11 | 10 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `java.nio.file` | 22 | 16 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `java.nio.file.attribute` | 5 | 5 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `java.security` | 21 | 15 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `java.security.cert` | 8 | 6 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `java.security.interfaces` | 3 | 3 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `java.security.spec` | 10 | 6 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `java.text` | 20 | 20 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `java.time` | 18 | 18 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `java.time.format` | 2 | 2 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `java.time.temporal` | 11 | 4 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `java.util` | 89 | 65 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `java.util.concurrent` | 54 | 42 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `java.util.concurrent.atomic` | 11 | 11 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `java.util.concurrent.locks` | 9 | 8 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `java.util.function` | 43 | 40 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `java.util.jar` | 8 | 8 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `java.util.logging` | 6 | 6 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `java.util.regex` | 4 | 4 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `java.util.stream` | 13 | 12 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `java.util.zip` | 19 | 17 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `javax.crypto` | 10 | 10 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `javax.crypto.spec` | 4 | 4 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `javax.net` | 2 | 2 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `javax.net.ssl` | 16 | 12 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `javax.security.auth.x500` | 1 | 1 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `kotlin.io` | 6 | 3 | 0 | ✅ 全部覆盖 |
| `jako-stdlib` | `kotlin.text` | 1 | 1 | 0 | ✅ 全部覆盖 |

## 3. 待补测核心类清单 (Actionable Untested Classes: 0)

🎉 **所有非豁免公开核心类均已在 `commonTest` 中包含直接测试引用！**

## 4. 豁免的抽象/标记接口与基础契约 (Exempt Marker/Base Interfaces: 54)

> 以下接口属于基础父接口或标记接口，已通过其具体实现类间接测试覆盖：

- `java.io.Closeable` (interface)
- `java.io.Flushable` (interface)
- `java.io.Serializable` (interface)
- `java.lang.Cloneable` (interface)
- `java.lang.Object` (class)
- `java.nio.channels.ByteChannel` (interface)
- `java.nio.channels.GatheringByteChannel` (interface)
- `java.nio.channels.InterruptibleChannel` (interface)
- `java.nio.channels.NetworkChannel` (interface)
- `java.nio.channels.ReadableByteChannel` (interface)
- `java.nio.channels.ScatteringByteChannel` (interface)
- `java.nio.channels.SeekableByteChannel` (interface)
- `java.nio.channels.SelectableChannel` (abstract class)
- `java.nio.channels.WritableByteChannel` (interface)
- `java.nio.file.CopyOption` (interface)
- `java.nio.file.OpenOption` (interface)
- `java.security.Principal` (interface)
- `java.security.spec.AlgorithmParameterSpec` (interface)
- `java.security.spec.KeySpec` (interface)
- `java.time.temporal.Temporal` (interface)
- `java.time.temporal.TemporalAccessor` (interface)
- `java.time.temporal.TemporalAdjuster` (interface)
- `java.time.temporal.TemporalAmount` (interface)
- `java.time.temporal.TemporalField` (interface)
- `java.time.temporal.TemporalUnit` (interface)
- `java.util.Deque` (interface)
- `java.util.NavigableMap` (interface)
- `java.util.NavigableSet` (interface)
- `java.util.PrimitiveIterator` (interface)
- `java.util.PrimitiveIterator.OfDouble` (interface)
- `java.util.PrimitiveIterator.OfInt` (interface)
- `java.util.PrimitiveIterator.OfLong` (interface)
- `java.util.Queue` (interface)
- `java.util.SortedSet` (interface)
- `java.util.Spliterator.OfDouble` (interface)
- `java.util.Spliterator.OfInt` (interface)
- `java.util.Spliterator.OfLong` (interface)
- `java.util.Spliterator.OfPrimitive` (interface)
- `java.util.concurrent.BlockingDeque` (interface)
- `java.util.concurrent.BlockingQueue` (interface)
- `java.util.concurrent.CompletionService` (interface)
- `java.util.concurrent.CompletionStage` (interface)
- `java.util.concurrent.ExecutorService` (interface)
- `java.util.concurrent.RunnableFuture` (interface)
- `java.util.concurrent.ScheduledExecutorService` (interface)
- `java.util.concurrent.ScheduledFuture` (interface)
- `java.util.concurrent.TransferQueue` (interface)
- `java.util.concurrent.locks.ReadWriteLock` (interface)
- `java.util.function.BiConsumer` (interface)
- `java.util.function.BiPredicate` (interface)
- `java.util.function.Consumer` (interface)
- `java.util.stream.BaseStream` (interface)
- `java.util.zip.Checksum` (interface)
- `javax.net.ssl.KeyManager` (interface)

