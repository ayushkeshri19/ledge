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
include(":core:ui")
include(":core:common")
include(":core:network")
include(":core:security")
include(":core:database")
include(":core:datastore")
include(":feature")
include(":feature:sms")
include(":feature:auth")
include(":feature:profile")
include(":feature:dashboard")
include(":feature:onboarding")
include(":feature:dashboard:home")
include(":feature:dashboard:budget")
include(":feature:dashboard:insights")
include(":feature:dashboard:transactions")
