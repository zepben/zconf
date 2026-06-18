/*
 * Copyright (c) Zeppelin Bend Pty Ltd (Zepben) 2024 - All Rights Reserved.
 * Unauthorized use, copy, or distribution of this file or its contents, via any medium is strictly prohibited.
 */

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.koTest)
    alias(libs.plugins.googleDevTools)
}

group = "com.zepben.zconf"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()

    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") {
        name = "MavenCentralSnapshots"
        mavenContent { snapshotsOnly() }
    }
}

kotlin {
    val hostOs = System.getProperty("os.name")
    val isArm64 = System.getProperty("os.arch") == "aarch64"
    val isMingwX64 = hostOs.startsWith("Windows")
    val nativeTarget = when {
        hostOs == "Mac OS X" && isArm64 -> macosArm64("native")
        hostOs == "Mac OS X" && !isArm64 -> macosX64("native")
        hostOs == "Linux" && isArm64 -> linuxArm64("native")
        hostOs == "Linux" && !isArm64 -> linuxX64("native")
        isMingwX64 -> mingwX64("native")
        else -> throw GradleException("Host OS is not supported in Kotlin/Native.")
    }

    nativeTarget.apply {
        binaries {
            executable {
                entryPoint = "$group.main"
            }
        }
    }

    sourceSets {
        nativeMain.dependencies {
            implementation(libs.kotlinxSerializationJson)
            implementation(libs.kotlinxIoCore)
            implementation(libs.clikt)
            implementation(libs.kotlinLogger)
        }

        nativeTest.dependencies {
            implementation(libs.koTestFramework)
            implementation(libs.koTestAssertions)
        }
    }
}
