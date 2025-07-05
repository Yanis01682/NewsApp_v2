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
        maven { url = uri("https://jitpack.io")}
        //maven { url = uri("https://repo.spring.io/plugins-release/") }
        maven { url = uri("https://repository.mulesoft.org/nexus/content/repositories/public/") }
        maven { url = uri("https://dl.bintray.com/bilibili/maven/") }



    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("https://jitpack.io")}
        //maven { url = uri("https://repo.spring.io/plugins-release/") }
        maven { url = uri("https://repository.mulesoft.org/nexus/content/repositories/public/") }
        maven { url = uri("https://dl.bintray.com/bilibili/maven/") }


    }
}

rootProject.name = "zhangzhiyuan"
include(":app")
 