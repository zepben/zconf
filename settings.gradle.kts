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
