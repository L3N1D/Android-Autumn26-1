pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    // Позволяет Gradle самому скачать JDK 17 для toolchain, если её нет локально
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "FootballCsvDemo"
