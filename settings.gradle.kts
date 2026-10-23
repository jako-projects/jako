pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "jako"

include(":jako-compiler-plugin")
include(":jako-gradle-plugin")
include(":jako-stdlib")
include(":jako-android")

