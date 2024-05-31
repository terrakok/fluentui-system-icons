import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.compose)
    alias(libs.plugins.android.library)
    `maven-publish`
}

group = "com.microsoft.design"
version = "1.0.0"

kotlin {
    androidTarget {
        publishLibraryVariants("release")
        compilations.all {
            compileTaskProvider {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_1_8)
                    freeCompilerArgs.add("-Xjdk-release=${JavaVersion.VERSION_1_8}")
                }
            }
        }
    }

    jvm()

    js { browser() }
    wasmJs { browser() }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.components.resources)
        }
    }
}

android {
    namespace = group.toString()
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }
    buildFeatures {
        compose = true
    }
}

compose.resources {
    publicResClass = true
    packageOfResClass = "com.microsoft.design.compose.icons"
}

//run it once before first import
tasks.register("copyAndroidVectorIcons", CopyAndroidIcons::class) {
    srcDir = rootProject.projectDir.resolve("../android/library/src/main/res/drawable")
    destDir = project.projectDir.resolve("src/commonMain/composeResources/drawable")
}

abstract class CopyAndroidIcons : DefaultTask() {
    @get:InputDirectory
    abstract val srcDir: DirectoryProperty

    @get:OutputDirectory
    abstract val destDir: DirectoryProperty

    data class IconInfo(val id: String, val type: String, val size: Int, val file: File)

    @TaskAction
    fun run() {
        val out = destDir.get().asFile
        out.deleteRecursively()
        out.mkdirs()

        val icons = srcDir.get().asFile.listFiles().orEmpty()
            .filter { f -> f.isFile && f.extension == "xml" }
            .map { f ->
                f.nameWithoutExtension.split("_").let { parts ->
                    val type = parts.last()
                    val size = parts[parts.size - 2].toInt()
                    IconInfo(f.nameWithoutExtension.substringBefore("_${size}_$type"), type, size, f)
                }
            }
            .filterNot { i -> i.type == "selector" }
            .groupBy { i -> i.id + "_" + i.type }
            .map { (_, values) -> values.maxBy { it.size }.file }
        icons.forEach { file ->
            val icon = file.copyTo(out.resolve(file.name), overwrite = true)
            icon.writeText(
                icon.readText().replace(
                    "android:fillColor=\"@color/fluent_default_icon_tint\"",
                    "android:fillColor=\"#FFFFFF\""
                )
            )
        }
    }
}
