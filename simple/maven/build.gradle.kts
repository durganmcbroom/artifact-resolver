plugins {
    kotlin("multiplatform")
//    id("maven-publish")
//    id("org.jetbrains.dokka")
}

group = "com.durganmcbroom"
version = "1.1-SNAPSHOT"

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
                doFirst {
                    options.compilerArgs.addAll(
                        listOf(
                            "--module-path", classpath.asPath,
                            "--add-modules", "arrow.core.jvm,kotlinx.coroutines.core.jvm",
                            "--patch-module", "arrow.core.jvm=arrow-core-jvm-1.1.2.jar",
                            "--patch-module", "kotlinx.coroutines.core.jvm=kotlinx-coroutines-core-jvm-1.6.4.jar"
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
                implementation(kotlin("reflect"))
                implementation(project(":"))

                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val jvmMain by getting {
            dependencies {
                implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.12.6")
                implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.6")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(project(":"))
            }
        }
    }
}


publishing {
    publications.withType<MavenPublication> {
        artifact(tasks["javadocJar"])

        artifactId = "artifact-resolver-$artifactId"

        pom {
            name.set("Artifact Resolver - Simple Maven")
            description.set("An Implementation of the Artifact Resolver API for Maven.")
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