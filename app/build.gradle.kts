
plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.java.zhangzhiyuan"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.java.zhangzhiyuan"
        minSdk = 28
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        viewBinding = true
    }
    packaging {
        resources {
            excludes += "/META-INF/DEPENDENCIES"
        }
    }

    dependencies {

        implementation("org.apache.commons:commons-beanutils:1.9.4")
        implementation("org.apache.commons:commons-collections:3.2.1")
        implementation("org.apache.commons:commons-logging:1.2")

        implementation("org.apache.httpcomponents:httpclient:4.5.13") // 升级版本
        implementation("org.apache.httpcomponents:httpcore:4.4.15")   // 升级版本

        //implementation("commons-beanutils:commons-beanutils-core:1.8.3")
        implementation("androidx.core:core-ktx:1.9.0") // 版本号可能略有不同
        implementation("androidx.appcompat:appcompat:1.6.1")
        implementation("com.google.android.material:material:1.11.0")
        implementation("androidx.constraintlayout:constraintlayout:2.1.4")
        implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
        implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
        implementation("androidx.navigation:navigation-fragment-ktx:2.7.7")
        implementation("androidx.navigation:navigation-ui-ktx:2.7.7")
        // 用于网络请求
        implementation("com.squareup.retrofit2:retrofit:2.9.0")
        implementation("com.squareup.retrofit2:converter-gson:2.9.0")
        // 用于解析JSON
        implementation("com.google.code.gson:gson:2.9.0")
        // 用于加载和显示网络图片
        implementation("com.github.bumptech.glide:glide:4.12.0")
        implementation(libs.swiperefreshlayout)
        annotationProcessor("com.github.bumptech.glide:compiler:4.12.0")

        testImplementation("junit:junit:4.13.2")
        androidTestImplementation("androidx.test.ext:junit:1.1.5")
        androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")


        // Room 用于本地数据库，保存GLM生成的新闻摘要
        val room_version = "2.6.1"
        implementation("androidx.room:room-runtime:$room_version")
        annotationProcessor("androidx.room:room-compiler:$room_version")



        implementation("com.fasterxml.jackson.core:jackson-databind:2.13.0")
//示例代码所含有的
        testImplementation(platform("org.junit:junit-bom:5.10.0"))
        testImplementation("org.junit.jupiter:junit-jupiter")

        implementation("cn.bigmodel.openapi:oapi-java-sdk:release-V4-2.0.2")




        implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")



    }

}
