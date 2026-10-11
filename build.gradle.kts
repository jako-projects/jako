plugins {
    kotlin("multiplatform") version "2.4.21" apply false
    id("com.vanniktech.maven.publish") version "0.31.0" apply false
}

subprojects {
    group = providers.gradleProperty("GROUP").get()
    version = providers.gradleProperty("VERSION_NAME").get()
}
