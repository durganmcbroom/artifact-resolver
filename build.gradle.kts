plugins {
    kotlin("multiplatform") version "1.9.21"
    id("maven-publish")
    id("org.jetbrains.dokka") version "1.9.10"
}

kotlin {
    explicitApi()

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        jvmToolchain(8)
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }

        java {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(8))
            }
        }

        val main by compilations.getting {
            compileJavaTaskProvider!!.get().run {
                targetCompatibility = "1.8"
                sourceCompatibility = "1.8"
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
                implementation(kotlin("reflect"))
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.multiplatform")
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.dokka")

    group = "com.durganmcbroom"
    version = "1.5.1-SNAPSHOT"

    repositories {
        mavenCentral()
        maven {
            url = uri("https://maven.durganmcbroom.com/snapshots")
        }
    }

    val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)

    tasks.register<Jar>("javadocJar") {
        dependsOn(dokkaHtml)
        archiveClassifier.set("javadoc")
        from(dokkaHtml.outputDirectory)
    }

    kotlin {
        sourceSets {
            val commonMain by getting {
                dependencies {
                    implementation("com.durganmcbroom:resource-api:1.2.2-SNAPSHOT")
                }
            }
        }
    }

    publishing {
        repositories {
            maven {
                url = uri("https://maven.durganmcbroom.com/snapshots")

                credentials {
                    username = project.findProperty("maven.user") as String?
                    password = project.findProperty("maven.key") as String?
                }
                authentication {
                    create<BasicAuthentication>("basic")
                }
            }
        }
    }
}

val publishAll by tasks.registering {
    val taskName = "publish"

    dependsOn(project(":simple-maven").tasks[taskName])
    dependsOn(tasks[taskName])
}

val publishAllLocally by tasks.registering {
    val taskName = "publishToMavenLocal"

    dependsOn(project(":simple-maven").tasks[taskName])
    dependsOn(tasks[taskName])
}

publishing {
    publications.withType<MavenPublication> {
        artifact(tasks["javadocJar"])

        pom {
            name.set("Artifact Resolver")
            description.set("An API for parsing, loading, and resolving artifacts and dependencies from remote repositories.")
            url.set("https://github.com/durganmcbroom/artifact-resolver")

            packaging = "jar"

            developers {
                developer {
                    id.set("durganmcbroom")
                    name.set("Durgan McBroom")
                }
            }

            withXml {
                val repositoriesNode = asNode().appendNode("repositories")
                val yakclientRepositoryNode = repositoriesNode.appendNode("repository")
                yakclientRepositoryNode.appendNode("id", "durganmcbroom")
                yakclientRepositoryNode.appendNode("url", "https://maven.durganmcbroom.com/snapshots")
            }

            licenses {
                license {
                    name.set("MIT License")
                    url.set("https://opensource.org/licenses/MIT")
                }
            }

            scm {
                connection.set("scm:git:git://github.com/durganmcbroom/artifact-resolver")
                developerConnection.set("scm:git:ssh://github.com:durganmcbroom/artifact-resolver.git")
                url.set("https://github.com/durganmcbroom/artifact-resolver")
            }
        }
    }
}
