/*
 * Copyright (c) Zeppelin Bend Pty Ltd (Zepben) 2026 - All Rights Reserved.
 * Unauthorized use, copy, or distribution of this file or its contents, via any medium is strictly prohibited.
 */

import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

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
    val linuxTargets = listOf(linuxX64(), linuxArm64())
    val macosTargets = listOf(macosX64(), macosArm64())
    val windowsTargets = listOf(mingwX64())
    val allTargets = linuxTargets + macosTargets + windowsTargets

    allTargets.forEach { target ->
        target.binaries {
            executable { entryPoint = "$group.main" }
        }
    }

    sourceSets {
        val nativeMain by creating {
            dependencies {
                implementation(libs.kotlinxSerializationJson)
                implementation(libs.kotlinxIoCore)
                implementation(libs.clikt)
                implementation(libs.kotlinLogger)
            }
        }
        val linuxMain by creating { dependsOn(nativeMain) }
        val macosMain by creating { dependsOn(nativeMain) }
        val windowsMain by creating { dependsOn(nativeMain) }

        val nativeTest by creating {
            dependencies {
                implementation(libs.koTestFramework)
                implementation(libs.koTestAssertions)
            }
        }

        linuxTargets.forEach { it.compilations["main"].defaultSourceSet.dependsOn(linuxMain) }
        macosTargets.forEach { it.compilations["main"].defaultSourceSet.dependsOn(macosMain) }
        windowsTargets.forEach { it.compilations["main"].defaultSourceSet.dependsOn(windowsMain) }
        allTargets.forEach { it.compilations["test"].defaultSourceSet.dependsOn(nativeTest) }
    }
}

val hostOs: String = System.getProperty("os.name")
val isArm64: Boolean = System.getProperty("os.arch") == "aarch64"
val hostTargetName: String = when {
    hostOs.startsWith("Windows") -> "mingwX64"
    hostOs == "Mac OS X" && isArm64 -> "macosArm64"
    hostOs == "Mac OS X" -> "macosX64"
    isArm64 -> "linuxArm64"
    else -> "linuxX64"
}

// Only compile for the host platform
afterEvaluate {
    kotlin.targets.withType<KotlinNativeTarget>().configureEach {
        if (name != hostTargetName) {
            val suffix = name.replaceFirstChar { it.uppercase() }
            tasks.matching { it.name.endsWith(suffix) }.configureEach { enabled = false }
        }
    }
}

// The linkReleaseExecutableNative task should just compile for the host platform
tasks.register("linkReleaseExecutableNative") {
    group = "build"
    description = "Alias for the host-platform release link task (for CI compatibility)."
    dependsOn("linkReleaseExecutable${hostTargetName.replaceFirstChar { it.uppercase() }}")
}
