import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.5.31"
    id("org.jetbrains.compose") version "1.0.0"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.5.31"
}

group = "net.sergeych"
version = "1.0.6"

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
    kotlinOptions {
        jvmTarget = "11"
        freeCompilerArgs = freeCompilerArgs + "-Xopt-in=kotlin.RequiresOptIn"
    }
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "ZeText"
            description = "Tool to protect sensitive build scripts"
            copyright = "© 2021 Sergey S. Chernov"
            vendor = "sergeych.net"

            val iconsRoot = project.file("src/main/resources")
            linux {
                iconFile.set(iconsRoot.resolve("launcher_icons/linux.png"))
                // does not work: it mistakenly says it has a proprietary licens :(
                // while itis mut
//                rpmLicenseType = "MIT"
            }
            macOS {
                bundleID = "net.sergeych.zetext"
                iconFile.set(iconsRoot.resolve("launcher_icons/macos.icns"))
                // we do not add license to the apple distribution as it is
                // MIT and does not need consent on use copiled code (I hope)
                infoPlist {
                    extraKeysRawXml = macExtraPlistKeys
                }
                signing {
                    sign.set(true)
                    identity.set("Sergey Chernov")
                }
                notarization {
                    appleID.set("real.sergeych@gmail.com")
                    password.set("@keychain:zetexttest1")
                    ascProvider.set("Y7FC8DCN73")
                }
            }
            windows {
                iconFile.set(iconsRoot.resolve("launcher_icons/windows.ico"))
            }
        }
    }
}

val macExtraPlistKeys: String
    get() = """
        <key>CFBundleDocumentTypes</key>
        <array>
            <dict>
                <key>CFBundleTypeRole</key>
                <string>Editor</string>
                
                <key>CFBundleTypeExtensions</key>
                <array>
                    <string>ztext</string>
                </array>
                
                <key>CFBundleTypeIconFile</key>
                <string>ZeText.icns</string>
                
                <key>CFBundleTypeMIMETypes</key>
    			<array>
	    			<string>application/octet-stream</string>
		    	</array>

                <key>CFBundleTypeOSTypes</key>
                <array>
                    <string>ZTXT</string>
                </array>
                
                <key>CFBundleTypeName</key>
                <string>binary ZeText</string>
                
                <key>LSHandlerRank</key>
                <string>Owner</string>
            </dict>
        </array>
    """
