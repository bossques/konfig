plugins {
    kotlin("jvm")
    kotlin("plugin.serialization")
}

group = "io.github.qbosst"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    compileOnly(project(":core"))
    api(libs.kotlinx.serialization.yaml)

    testImplementation(project(":core"))
}
