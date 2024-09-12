/*
 * Copyright (c) Zeppelin Bend Pty Ltd (Zepben) 2024 - All Rights Reserved.
 * Unauthorized use, copy, or distribution of this file or its contents, via any medium is strictly prohibited.
 */

//pluginManagement {
//    repositories {
//        mavenCentral()
//        gradlePluginPortal()
//    }
//}


// the below section can be removed for the above block once koTest 6.0 is released
// see https://github.com/kotest/kotest/issues/4177
@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    pluginManagement {
        repositories {
            specialRepositories()
            gradlePluginPortal()
        }
    }
    repositories {
        mavenCentral()
        specialRepositories()
    }
}

fun RepositoryHandler.specialRepositories() {
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") {
        name = "MavenCentralSnapshots"
        mavenContent { snapshotsOnly() }
    }
}

rootProject.name = "zconf"
