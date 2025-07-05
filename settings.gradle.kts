// settings.gradle.kts

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
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://repository.mulesoft.org/nexus/content/repositories/public/") }
        maven { url = uri("https://dl.bintray.com/bilibili/maven/") }
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        // --- 【核心修正】在这里为google()仓库添加内容过滤器 ---
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        maven { url = uri("https://jitpack.io") }
        maven { url = uri("https://repository.mulesoft.org/nexus/content/repositories/public/") }
        maven { url = uri("https://dl.bintray.com/bilibili/maven/") }
    }
}

rootProject.name = "zhangzhiyuan"
include(":app")