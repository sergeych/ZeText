import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
    id("org.jetbrains.compose") version "1.0.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.5.31"
}

group = "net.sergeych"
version = "1.0"

repositories {
    google()
    mavenCentral()
//    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://maven.universablockchain.com/")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
    api("org.jetbrains.kotlinx:kotlinx-serialization-core-jvm:1.3.0")
    implementation("com.icodici:crypto:3.14.3")
    implementation(compose.desktop.currentOs)
    implementation(files("src/lib/boss-serialization.jar"))
    implementation("com.github.ajalt.clikt:clikt:3.3.0")

    testImplementation(kotlin("test"))
    testImplementation("org.testng:testng:7.4.0")
}

tasks.test {
    useTestNG()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ZeText"
            packageVersion = "1.0.0"
//            macOS {
//                dockName = "ZeText"
//            }
        }
    }
}