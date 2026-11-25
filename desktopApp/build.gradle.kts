import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.kotlin.compose)
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":shared-core"))
    implementation(project(":shared-ui"))
    implementation(compose.desktop.currentOs)
    implementation(compose.material3)
    implementation(libs.kotlinx.coroutines.swing)
    implementation(libs.sqlite.jdbc)

    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
}

compose.desktop {
    application {
        mainClass = "com.aidomari.app.desktop.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Msi, TargetFormat.Exe, TargetFormat.Deb)
            packageName = "Aidomari"
            packageVersion = "2.0.0"
            description = "Aidomari Flashcard and Study Desktop Application"
            vendor = "Aidomari"
            modules("java.sql", "java.prefs", "java.desktop", "java.naming")

            windows {
                menuGroup = "Aidomari"
                upgradeUuid = "7e4f8c9b-2d41-4c7e-9c12-61a884c3f901"
            }
        }
    }
}
