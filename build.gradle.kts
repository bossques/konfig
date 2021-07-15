plugins {
    kotlin("jvm")
    `maven-publish`
    signing
}

repositories {
    mavenCentral()
}

val projectArtifactId = "konfig"
val projectVersion = "1.0.0"
val projectGroup = "io.github.qbosst"
val projectGithubUrl = "https://github.com/qbosst/Konfig"

group = projectGroup
version = projectVersion

val releaseRepoUrl = "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
val snapshotRepoUrl = "https://s01.oss.sonatype.org/content/repositories/snapshots/"

subprojects {
    apply(plugin = "maven-publish")
    apply(plugin = "signing")
    apply(plugin = "java")
    apply(plugin = "kotlin")

    version = projectVersion
    group = projectGroup

    repositories {
        mavenCentral()
    }

    val sourcesJar by tasks.registering(Jar::class) {
        archiveClassifier.set("sources")
        from(sourceSets.main.get().allSource)
    }

    val javadocJar by tasks.registering(Jar::class) {
        archiveClassifier.set("javadoc")
        from(tasks.javadoc)
    }

    publishing {
        publications {
            create<MavenPublication>("maven") {
                from(components["kotlin"])

                artifact(sourcesJar)
                artifact(javadocJar)

                artifactId = projectArtifactId
                groupId = project.group as String
                version = project.version as String

                pom {
                    name.set("Konfig")
                    description.set("A simple config library for Kotlin that uses kotlinx.serialization")
                    url.set(projectGithubUrl)

                    licenses {
                        name.set("MIT")
                        url.set("https://opensource.org/licenses/MIT")
                    }

                    developers {
                        developer {
                            id.set("boss")
                            name.set("qbosst")
                            url.set("https://github.com/qbosst")
                        }
                    }

                    scm {
                        connection.set("scm:git:git://github.com/qbosst/${projectArtifactId}.git")
                        developerConnection.set("scm:git:ssh://github.com:qbosst/${projectArtifactId}.git")
                        url.set("$projectGithubUrl/tree/master")
                    }
                }
            }
        }

        repositories {
            maven {
                name = "Sonatype"
                url = uri(if((version as String).endsWith("SNAPSHOT")) snapshotRepoUrl else releaseRepoUrl)

                credentials {
                    username = System.getenv("NEXUS_USER")
                    password = System.getenv("NEXUS_PASSWORD")
                }
            }
        }
    }

    signing {
        sign(publishing.publications["maven"])
    }
}