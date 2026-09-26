import java.util.Base64

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.ahmedalobaedy.timework"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.ahmedalobaedy.timework"
        minSdk = 26
        targetSdk = 37
        versionCode = 7
        versionName = "0.2.5"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        resources.excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
}

dependencies {
    implementation(platform("androidx.compose:compose-bom:2026.09.00"))
    implementation("androidx.core:core-ktx:1.19.1")
    implementation("androidx.activity:activity-compose:1.13.0")
    implementation("androidx.appcompat:appcompat:1.8.0")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.11.0")

    debugImplementation("androidx.compose.ui:ui-tooling")
}


val prepareLauncherIcon by tasks.registering {
    val partsDir = rootProject.file("assets/icon_parts")
    val outputDir = file("src/main/res/mipmap-xxxhdpi")
    val outputIcon = outputDir.resolve("ic_launcher.webp")
    val outputRoundIcon = outputDir.resolve("ic_launcher_round.webp")
    val outputForeground = outputDir.resolve("time_work_foreground.webp")
    val outputLegacy = outputDir.resolve("time_work_legacy.webp")

    inputs.files((1..5).map { partsDir.resolve("part$it.txt") })
    outputs.files(outputIcon, outputRoundIcon, outputForeground, outputLegacy)

    doLast {
        outputDir.mkdirs()
        val encoded = (1..5).joinToString("") {
            partsDir.resolve("part$it.txt").readText().trim()
        }
        val bytes = Base64.getDecoder().decode(encoded)
        outputIcon.writeBytes(bytes)
        outputRoundIcon.writeBytes(bytes)
        outputForeground.writeBytes(bytes)
        outputLegacy.writeBytes(bytes)
    }
}

tasks.named("preBuild").configure {
    dependsOn(prepareLauncherIcon)
}
