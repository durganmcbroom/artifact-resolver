package com.durganmcbroom.artifact.resolver.simple.maven.test

import com.durganmcbroom.artifact.resolver.Artifact
import com.durganmcbroom.artifact.resolver.createContext
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMaven
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenArtifactMetadata
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenArtifactRequest
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenRepositorySettings
import com.durganmcbroom.jobs.launch
import com.durganmcbroom.resources.ResourceAlgorithm
import kotlin.test.Test

class ResolutionTest {
    @Test
    fun `Test basic artifact resolution`() {
        val repository = SimpleMaven.createContext(
            SimpleMavenRepositorySettings.mavenCentral(
                preferredHash = ResourceAlgorithm.SHA1
            )
        )
        launch {
            val artifact =
                repository.getAndResolve(SimpleMavenArtifactRequest("org.springframework:spring-context:5.3.22"))().merge()

            artifact.prettyPrint()

            val expectedArtifact = artifactTree("org.springframework:spring-context:5.3.22") {
                child("org.springframework:spring-expression:5.3.22") {
                    child("org.springframework:spring-core:5.3.22") {
                        child("org.springframework:spring-jcl:5.3.22")
                    }
                }
                child("org.springframework:spring-core:5.3.22") {
                    child("org.springframework:spring-jcl:5.3.22")
                }
                child("org.springframework:spring-aop:5.3.22") {
                    child("org.springframework:spring-core:5.3.22") {
                        child("org.springframework:spring-jcl:5.3.22")
                    }
                    child("org.springframework:spring-beans:5.3.22") {
                        child("org.springframework:spring-core:5.3.22") {
                            child("org.springframework:spring-jcl:5.3.22")
                        }
                    }
                }
                child("org.springframework:spring-beans:5.3.22") {
                    child("org.springframework:spring-core:5.3.22") {
                        child("org.springframework:spring-jcl:5.3.22")
                    }
                }
            }
            expectedArtifact.checkDescriptorsEquals(artifact)
        }
    }

    @Test
    fun `Test pretty artifact resolution`() {
        val context = SimpleMaven.createContext(
            SimpleMavenRepositorySettings.mavenCentral()
        )

        launch {
            val artifact =
                context.getAndResolve(SimpleMavenArtifactRequest("org.jetbrains.kotlin:kotlin-stdlib:1.9.21"))().merge()

            artifact.prettyPrint()
        }
    }

    @Test
    fun `Test adsf`() {
        val context = SimpleMaven.createContext(
            SimpleMavenRepositorySettings.default(url = "https://maven.extframework.dev/snapshots", requireResourceVerification = false)
        )

        launch {
            val artifact =
                context.getAndResolve(SimpleMavenArtifactRequest("dev.extframework.minecraft:minecraft-provider-def:1.0-SNAPSHOT"))().merge()

            artifact.prettyPrint()
        }
    }

    @Test
    fun `Test artifact is found even with terminating slash in repository path`() {
        val context = SimpleMaven.createContext(
            SimpleMavenRepositorySettings.default(
                url = "https://repo.maven.apache.org/maven2/"
            )
        )

        launch {
            val artifact =
                context.getAndResolve(SimpleMavenArtifactRequest("org.jetbrains.kotlin:kotlin-stdlib:1.9.21"))().merge()

            artifact.prettyPrint()
        }
    }

    @Test
    fun `Test snapshot artifact resolution`() {
        val context = SimpleMaven.createContext(
            SimpleMavenRepositorySettings.default(
                "https://repo.codemc.io/repository/maven-snapshots",
                preferredHash = ResourceAlgorithm.SHA1
            )
        )

        launch {
            val artifact: Artifact<SimpleMavenArtifactMetadata> =
                context.getAndResolve(SimpleMavenArtifactRequest("net.minecrell:ServerListPlus:3.5.0-SNAPSHOT"))().merge()

            artifact.prettyPrint {
                it.metadata.descriptor.toString() + " @ " + (it.metadata.resource?.location ?: "POM")
            }

            artifactTree("net.minecrell:ServerListPlus:3.5.0-SNAPSHOT") {
                child("com.google.code.gson:gson:2.8.0")
                child("org.ocpsoft.prettytime:prettytime:4.0.6.Final")
                child("org.yaml:snakeyaml:2.0")
                child("com.google.guava:guava:21.0") {
                    child("com.google.code.findbugs:jsr305:1.3.9")
                    child("com.google.errorprone:error_prone_annotations:2.0.15")
                    child("com.google.j2objc:j2objc-annotations:1.1")
                    child("org.codehaus.mojo:animal-sniffer-annotations:1.14")
                }
            }.checkDescriptorsEquals(artifact)
        }
    }
}