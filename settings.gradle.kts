/*
 * This file was generated by the Gradle 'init' task.
 *
 * The settings file is used to specify which projects to include in your build.
 *
 * Detailed information about configuring a multi-project build in Gradle can be found
 * in the user manual at https://docs.gradle.org/6.6.1/userguide/multi_project_builds.html
 */

rootProject.name = "InventoryProfilesNext"
include("common")
include("platforms:fabric-1.17")

//include("platforms:forge")

pluginManagement {
    repositories {
        maven(url = "https://maven.fabricmc.net") {
            name = "Fabric"
        }
        maven("https://dl.bintray.com/kotlin/kotlin-eap")

        google()
        gradlePluginPortal()
    }
}

plugins {
    id("com.gradle.enterprise") version "3.4.1"
}

gradleEnterprise {
    buildScan {
        termsOfServiceUrl = "https://gradle.com/terms-of-service"
        termsOfServiceAgree = "yes"
    }
}
