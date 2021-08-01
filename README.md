# Konfig  

![Build Status](https://badgen.net/github/checks/qbosst/kordex-hybrid-commands/main?icon=github&label=build) ![Release](https://badgen.net/maven/v/metadata-url/https/s01.oss.sonatype.org/service/local/repositories/releases/content/io/github/qbosst/konfig-core/maven-metadata.xml?icon=maven&label=release&color=blue&scale=1) ![Snapshot](https://badgen.net/maven/v/metadata-url/https/s01.oss.sonatype.org/service/local/repositories/snapshots/content/io/github/qbosst/konfig-core/maven-metadata.xml?icon=maven&label=snapshot&color=orange)

Konfig is a simple config library for Kotlin that makes use of [kotlinx.serialization](https://github.com/Kotlin/kotlinx.serialization)

## Quick Start

### Basic Usage

To create your config object, create a new class that inherits from the `Konfig` class, along with some config property values.

```kotlin
class ServerConfig: Konfig<JsonElement>(JsonEngine()) {
	val host: String by required("host-here")
	val username by required<String>("root")
	val password by required("root-password")
}
```
To start using this config, you will need to create an instance of it and call the `init` method.
```kotlin
// create instance
val config = ServerConfig()

// this will generate the config (if it doesn't exist) and load the values from the config file
config.init("server_config.json")
```

If a config file at that path was not found, it will generate the following.
```json
{
	"host": "host-here",
	"username": "root",
	"password": "root-password"
}
```

Just like that, you are free to start using the config!
```kotlin
println(config.host) // host-here
println(config.username) // root
println(config.password) // root-password
```

### Mutable Properties

If you have a value that you want to be able to modify in your config, you will need to define the property as being mutable by creating the property as a `var`
```kotlin
var int: Int by required(0)
```
You can change the value of this property similar to how you reassign variables.
```kotlin
int = 5
```
However, this change has not been saved in the config yet. To save this change you will need to call the `write` method on the config instance.

```kotlin
config.write()
```

### Engines

Since kotlinx.serialization allows you to serialize your objects into different types, konfig also supports this by using serialization engines.

Supported serialization types:

`Json` -> `JsonEngine` **konfig-core**
`Yaml` -> `YamlEngine` **konfig-yaml**


## Installation

### Gradle (Kotlin)
```kotlin
repositories {
    mavenCentral()

	// snapshot repository
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
}
```
---
```kotlin
dependencies {
    implementation("io.github.qbosst:konfig-core:{version}")
}
```

### Gradle (Groovy)

```groovy
repositories {
    mavenCentral() 

	// snapshot repository
    maven { url "https://s01.oss.sonatype.org/content/repositories/snapshots" }
}
```
---
```groovy
dependencies {
    implementation "io.github.qbosst:konfig-core:{version}"
}
```

### Maven

```xml
<!--snapshot repository-->
<repositories>
    <repository>
        <id>s01-snapshots-repo</id>
        <url>https://s01.oss.sonatype.org/content/repositories/snapshots</url>
    </repository>
</repositories>
```
---
```xml
<dependency>
    <groupId>io.github.qbosst</groupId>
    <artifactId>konfig-core</artifactId>
    <version>{version}</version>
</dependency>
```