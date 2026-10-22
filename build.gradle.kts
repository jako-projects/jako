plugins {
    kotlin("multiplatform") version "2.4.21" apply false
    id("com.vanniktech.maven.publish") version "0.31.0" apply false
}

subprojects {
    group = providers.gradleProperty("GROUP").get()
    version = providers.gradleProperty("VERSION_NAME").get()
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
