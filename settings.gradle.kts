pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "ledge"
include(":app")
include(":core")
include(":core:database")
include(":core:datastore")
include(":core:common")
include(":core:network")
include(":feature")
include(":feature:auth")
include(":core:ui")
include(":feature:dashboard")
include(":feature:dashboard:home")
include(":feature:dashboard:transactions")
include(":feature:dashboard:budget")
include(":feature:profile")
include(":feature:dashboard:insights")
include(":core:security")
