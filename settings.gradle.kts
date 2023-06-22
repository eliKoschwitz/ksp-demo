pluginManagement {
    plugins {
        id("com.google.devtools.ksp") version "1.6.20-1.0.5"
        kotlin("jvm") version "1.6.20"
    }
    repositories {
        gradlePluginPortal()
        google()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "ksp-sample"

include(":annotations")
include(":processor")
include(":main-project")
include("untitled")
