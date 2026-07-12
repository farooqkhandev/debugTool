plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.kotlin.plugin.compose)
    `maven-publish`
}

android {
    namespace = "com.quadlogixs.debugtool.hooks"
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

group = "com.github.farooqkhandev"
version = findProperty("debugtool.publish.version") as String? ?: "1.0.0"

publishing {
    publications {
        register<MavenPublication>("release") {
            groupId = "com.github.farooqkhandev"
            artifactId = "debugtool-hooks"
            version = project.version.toString()

            afterEvaluate {
                from(components["release"])
            }

            pom {
                name.set("debugTool Hooks")
                description.set(
                    "Always-safe debugTool hooks for runtime flags and OkHttp wiring " +
                        "(safe in release when full debugtool is absent)",
                )
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
    implementation(libs.androidx.core.ktx)
    implementation(libs.okhttp)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.lifecycle)
}
