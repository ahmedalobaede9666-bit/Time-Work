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
        versionCode = 2
        versionName = "0.2.0"
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
    val sourceFile = rootProject.file("assets/time_work_icon.webp.b64")
    val outputFile = file("src/main/res/drawable-nodpi/time_work_icon.webp")

    inputs.file(sourceFile)
    outputs.file(outputFile)

    doLast {
        outputFile.parentFile.mkdirs()
        val encoded = sourceFile.readText().trim()
        outputFile.writeBytes(Base64.getDecoder().decode(encoded))
    }
}

tasks.named("preBuild").configure {
    dependsOn(prepareLauncherIcon)
}
