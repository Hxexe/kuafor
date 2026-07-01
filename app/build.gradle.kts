import java.util.Base64
import java.util.zip.ZipFile

plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.google.devtools.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

// Decode debug.keystore from base64 if it is missing
val keystoreFile = file("${rootDir}/debug.keystore")
val base64File = file("${rootDir}/debug.keystore.base64")
if (!keystoreFile.exists() && base64File.exists()) {
  try {
    val base64Content = base64File.readText().trim()
    val decodedBytes = Base64.getDecoder().decode(base64Content)
    keystoreFile.writeBytes(decodedBytes)
    logger.lifecycle("Successfully decoded debug.keystore from debug.keystore.base64")
  } catch (e: Exception) {
    logger.error("Failed to decode debug.keystore", e)
  }
}

android {
  namespace = "com.example"
  compileSdk = 35

  defaultConfig {
    applicationId = "com.aistudio.kuaforapp.gxfwqa"
    minSdk = 21
    targetSdk = 35
    versionCode = 1
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  signingConfigs {
    val debugKeystoreFile = file("${rootDir}/debug.keystore")
    create("release") {
      val keystorePath = System.getenv("KEYSTORE_PATH")
      if (keystorePath != null && file(keystorePath).exists()) {
        storeFile = file(keystorePath)
        storePassword = System.getenv("STORE_PASSWORD")
        keyAlias = "upload"
        keyPassword = System.getenv("KEY_PASSWORD")
      } else {
        // Fallback to debug keystore so it is always signed and installable in previews
        storeFile = debugKeystoreFile
        storePassword = "android"
        keyAlias = "androiddebugkey"
        keyPassword = "android"
      }
      enableV1Signing = true
      enableV2Signing = true
    }
    create("debugConfig") {
      storeFile = debugKeystoreFile
      storePassword = "android"
      keyAlias = "androiddebugkey"
      keyPassword = "android"
      enableV1Signing = true
      enableV2Signing = true
    }
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
      signingConfig = signingConfigs.getByName("release")
    }
    debug {
      signingConfig = signingConfigs.getByName("debugConfig")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
}

// Configure the Secrets Gradle Plugin to use .env and .env.example files
// to match the convention used in Web projects.
secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

// Some unused dependencies are commented out below instead of being removed.
// This makes it easy to add them back in the future if needed.
dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(platform(libs.firebase.bom))
  // implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  // implementation(libs.androidx.camera.camera2)
  // implementation(libs.androidx.camera.core)
  // implementation(libs.androidx.camera.lifecycle)
  // implementation(libs.androidx.camera.view)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  // implementation(libs.androidx.datastore.preferences)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)
  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  // implementation(libs.firebase.ai)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  // implementation(libs.play.services.location)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  "ksp"(libs.androidx.room.compiler)
  "ksp"(libs.moshi.kotlin.codegen)
  implementation(libs.supabase.postgrest)
  implementation(libs.supabase.auth)
  implementation(libs.supabase.realtime)
  implementation(libs.ktor.client.okhttp)
}

tasks.register<Copy>("copyApkToOutputs") {
  dependsOn("assembleRelease")
  from(layout.buildDirectory.file("outputs/apk/release/app-release.apk"))
  into(rootProject.layout.projectDirectory.dir(".build-outputs"))
  rename { "kuaforum_v1.0_signed.apk" }
}

tasks.register<Copy>("copyApkToDownload") {
  dependsOn("assembleRelease")
  from(layout.buildDirectory.file("outputs/apk/release/app-release.apk"))
  into(rootProject.layout.projectDirectory.dir("APK_DOWNLOAD"))
  rename { "kuaforum_v1.0_signed.apk" }
}

tasks.register<Copy>("copyDebugApkToOutputs") {
  dependsOn("assembleDebug")
  from(layout.buildDirectory.file("outputs/apk/debug/app-debug.apk"))
  into(rootProject.layout.projectDirectory.dir(".build-outputs"))
  rename { "kuaforum_v1.0_debug.apk" }
}

tasks.register<Copy>("copyDebugApkToDownload") {
  dependsOn("assembleDebug")
  from(layout.buildDirectory.file("outputs/apk/debug/app-debug.apk"))
  into(rootProject.layout.projectDirectory.dir("APK_DOWNLOAD"))
  rename { "kuaforum_v1.0_debug.apk" }
}

tasks.register("copyApks") {
  dependsOn("copyApkToOutputs", "copyApkToDownload", "copyDebugApkToOutputs", "copyDebugApkToDownload")
  doLast {
    val signedApk = file("${rootDir}/APK_DOWNLOAD/kuaforum_v1.0_signed.apk")
    val debugApk = file("${rootDir}/APK_DOWNLOAD/kuaforum_v1.0_debug.apk")
    println("\n=======================================================")
    println("WORKSPACE APK FILE SIZE AUDIT:")
    if (signedApk.exists()) {
      println(" - ${signedApk.name}: ${signedApk.length()} bytes (${String.format("%.2f", signedApk.length().toDouble() / (1024 * 1024))} MB)")
    } else {
      println(" - ERROR: ${signedApk.name} does not exist!")
    }
    if (debugApk.exists()) {
      println(" - ${debugApk.name}: ${debugApk.length()} bytes (${String.format("%.2f", debugApk.length().toDouble() / (1024 * 1024))} MB)")
    } else {
      println(" - ERROR: ${debugApk.name} does not exist!")
    }
    println("=======================================================\n")
  }
}




