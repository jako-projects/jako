import org.gradle.api.tasks.testing.AbstractTestTask
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.TestDescriptor
import org.gradle.api.tasks.testing.TestResult
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    kotlin("multiplatform") version "2.4.21" apply false
    id("com.vanniktech.maven.publish") version "0.31.0" apply false
}

subprojects {
    group = providers.gradleProperty("GROUP").get()
    version = providers.gradleProperty("VERSION_NAME").get()

    tasks.withType<Test>().configureEach {
        systemProperty(
            "java.security.properties",
            rootProject.file("gradle/test-java.security").absolutePath
        )
    }

    tasks.withType<AbstractTestTask>().configureEach {
        testLogging {
            events(TestLogEvent.FAILED)
            exceptionFormat = TestExceptionFormat.FULL
            showExceptions = true
            showCauses = true
            showStackTraces = true
        }
        afterTest(KotlinClosure2<TestDescriptor, TestResult, Unit>({ desc, result ->
            if (result.resultType == TestResult.ResultType.FAILURE) {
                val msg = result.exception?.stackTraceToString()
                    ?.lineSequence()
                    ?.take(20)
                    ?.joinToString("%0A")
                    ?.replace("\r", "")
                    ?: "Test failed"
                println("::error title=Test Failed: ${desc.className}.${desc.name}::$msg")
            }
        }))
    }
}

tasks.register("checkApiCompatibility") {
    group = "verification"
    description = "Verifies target JDK API parity (--release <N>, default 8), API_CHECKLIST.md & UNSUPPORTED_APIS.md sync, and backward compatibility against api/*.api."
    dependsOn(":jako-stdlib:jvmTest")
}

tasks.register<Exec>("dumpApiBaseline") {
    group = "verification"
    description = "Regenerates api/jako-stdlib.api and api/jako-android.api canonical public API baseline snapshots."
    workingDir = rootDir
    commandLine(
        "./gradlew",
        ":jako-stdlib:jvmTest",
        "--tests", "jako.test.parity.JdkSignatureParityTest.testBackwardCompatibilityAgainstBaseline",
        "-Pjako.updateApiBaseline=true",
        "--rerun-tasks"
    )
}

tasks.register<Exec>("dumpTestCoverage") {
    group = "verification"
    description = "Scans commonMain public declarations vs commonTest references and regenerates TEST_COVERAGE.md."
    workingDir = rootDir
    commandLine(
        "./gradlew",
        ":jako-stdlib:jvmTest",
        "--tests", "jako.test.parity.JdkSignatureParityTest.testPublicClassCoverageLedger",
        "-Pjako.updateTestCoverage=true",
        "--rerun-tasks"
    )
}
