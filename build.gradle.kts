plugins {
    kotlin("multiplatform") version "1.7.0"
    id("maven-publish")
    id("org.jetbrains.dokka") version "1.6.21"
}

group = "com.durganmcbroom"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {
    explicitApi()

    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "17"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
        }

        java {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(17))
            }
        }

        val main by compilations.getting {
            compileKotlinTask.destinationDirectory.set(compileJavaTaskProvider!!.get().destinationDirectory.asFile.get())

            compileJavaTaskProvider!!.get().run {
                targetCompatibility = "17"
                sourceCompatibility = "17"

                doFirst {
                    options.compilerArgs.addAll(
                        listOf(
                            "--module-path", classpath.asPath,
                            "--add-modules", "arrow.core.jvm",
                            "--patch-module", "arrow.core.jvm=arrow-core-jvm-1.1.2.jar"
                        )
                    )

                    classpath = files()
                }
            }
        }

    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib"))
                implementation("io.arrow-kt:arrow-core:1.1.2")
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
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.dokka")

    val dokkaHtml by tasks.getting(org.jetbrains.dokka.gradle.DokkaTask::class)

    tasks.register<Jar>("javadocJar") {
        dependsOn(dokkaHtml)
        archiveClassifier.set("javadoc")
        from(dokkaHtml.outputDirectory)
    }
    publishing {
        repositories {
            maven {
                name = "github"
                url = uri("https://maven.pkg.github.com/durganmcbroom/artifact-resolver")

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
    val taskName = "publishAllPublicationsToGithubRepository"

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
