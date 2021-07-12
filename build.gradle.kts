plugins {
    kotlin("jvm") version "1.5.20"
    kotlin("plugin.serialization") version "1.5.20"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    api(kotlin("reflect"))
    api("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
}
