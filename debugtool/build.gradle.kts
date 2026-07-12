plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.plugin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt.android)
    `maven-publish`
}

android {
    namespace = "com.quadlogixs.debugtool"
    compileSdk = libs.versions.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.minSdk.get().toInt()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = libs.versions.jvmTarget.get()
    }

    buildFeatures {
        compose = true
    }

    publishing {
        singleVariant("release") {
            withSourcesJar()
        }
    }
}

// JitPack / Maven coordinates (see https://jitpack.io)
group = "com.github.farooqkhandev"
version = findProperty("debugtool.publish.version") as String? ?: "1.0.0"

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.github.farooqkhandev"
            artifactId = "debugtool"
            version = project.version.toString()

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("debugTool")
                description.set("Standalone Android debug menu library for debug builds only")
                url.set("https://github.com/farooqkhandev/debugTool")
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                developers {
                    developer {
                        id.set("farooqkhandev")
                        name.set("Farooq Khan")
                    }
                }
                scm {
                    connection.set("scm:git:github.com/farooqkhandev/debugTool.git")
                    developerConnection.set("scm:git:ssh://github.com/farooqkhandev/debugTool.git")
                    url.set("https://github.com/farooqkhandev/debugTool")
                }
            }
        }
    }

    // Only register GitHub Packages when credentials exist (JitPack has none).
    val gprUser = findProperty("gpr.user") as String? ?: System.getenv("GITHUB_ACTOR")
    val gprKey = findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
    if (!gprUser.isNullOrBlank() && !gprKey.isNullOrBlank()) {
        repositories {
            maven {
                name = "GitHubPackages"
                url = uri("https://maven.pkg.github.com/farooqkhandev/debugTool")
                credentials {
                    username = gprUser
                    password = gprKey
                }
            }
        }
    }
}

dependencies {
    // Host apps must add debugtool-hooks themselves (implementation).
    // compileOnly keeps hooks off the published POM so consumers never get a
    // second/transitive hooks AAR (no exclude{} needed; avoids JitPack group clashes).
    compileOnly(project(":debugtool-hooks"))

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.lifecycle)
    implementation(libs.androidx.compose.view.model)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.hilt.navigation)
    implementation(libs.androidx.compose.preview)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.kotlinx.serialization)
    implementation(libs.gson)
    implementation(libs.timber)
    implementation(libs.androidx.metrics.performance)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)
    implementation(libs.okhttp)
    implementation(libs.logging.interceptor.v4100)

    // Always ship the real Chucker API. This library is consumed via debugImplementation only;
    // publishing release+library-no-op makes host debug builds collide with their own chucker:library.
    api(libs.chucker)

    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.firestore)

    implementation("androidx.security:security-crypto:1.1.0-alpha05")

    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.barcode.scanning)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.guava)
    implementation(libs.maps.compose)
    implementation(libs.play.services.maps)
    implementation(libs.play.services.location)

    debugImplementation(libs.ui.tooling)
}
