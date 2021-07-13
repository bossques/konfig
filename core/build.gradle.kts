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
    api(kotlin("reflect"))
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
}
