package com.durganmcbroom.artifact.resolver.simple.maven.test

import arrow.core.Either
import com.durganmcbroom.artifact.resolver.ResolutionContext
import com.durganmcbroom.artifact.resolver.createContext
import com.durganmcbroom.artifact.resolver.simple.maven.HashType
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMaven
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenArtifactRequest
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenRepositorySettings
import kotlin.test.Test

class ResolutionTest {
    @Test
    fun `Test basic artifact resolution`() {
        val repository = SimpleMaven.createNew(
            SimpleMavenRepositorySettings.mavenCentral(
                preferredHash = HashType.SHA1
            )
        )
        val either = ResolutionContext(
            repository,
            repository.stubResolver,
            SimpleMaven.artifactComposer
        ).getAndResolve(SimpleMavenArtifactRequest("org.springframework:spring-context:5.3.22"))

        check(either is Either.Right)

        either.value.prettyPrint()
    }

    @Test
    fun `Test pretty artifact resolution`() {
        val context = SimpleMaven.createContext(
            SimpleMavenRepositorySettings.mavenCentral(
                preferredHash = HashType.SHA1
            )
        )

        val either =
            context.getAndResolve(SimpleMavenArtifactRequest("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.12.6"))

        check(either is Either.Right)

        either.value.prettyPrint()
    }

    @Test
    fun `Test arrow-core artifact resolution`() {
        val context = SimpleMaven.createContext(
            SimpleMavenRepositorySettings.mavenCentral(
                preferredHash = HashType.SHA1
            )
        )

        val either =
            context.getAndResolve(SimpleMavenArtifactRequest(
                "io.arrow-kt:arrow-core:1.1.2",
                includeScopes = setOf("compile", "runtime", "import")
            ))

        check(either is Either.Right)

        either.value.prettyPrint(
            acceptStubs = false
        )
    }

    @Test
    fun `Test snapshot artifact resolution`() {
        val context = SimpleMaven.createContext(
            SimpleMavenRepositorySettings.default(
                "https://repo.codemc.io/repository/maven-snapshots",
                preferredHash = HashType.SHA1
            )
        )
        val either = context.getAndResolve(SimpleMavenArtifactRequest("net.minecrell:ServerListPlus:3.5.0-SNAPSHOT"))

        check(either is Either.Right)

        either.value.prettyPrint {
            it.metadata.descriptor.toString() + " @ " + (it.metadata.resource?.location ?: "POM")
        }
    }
//
//    @Test
//    fun `Test spigot artifact resolution`() {
//        val context = SimpleMaven.createContext(
//            SimpleMavenRepositorySettings.default(
//                "https://hub.spigotmc.org/nexus/content/repositories/snapshots",
//                preferredHash = HashType.MD5
//            )
//        )
//        val either = context.getAndResolve(SimpleMavenArtifactRequest("org.spigotmc:spigot-api:1.10-R0.1-SNAPSHOT"))
//
//        check(either is Either.Right)
//
//        either.value.prettyPrint {
//            it.metadata.descriptor.toString() + " @ " + (it.metadata.resource?.location ?: "POM")
//        }
//    }

//    fun `Test basic artifact resolution`() {
//        val processor = ArtifactGraph(SimpleMaven).resolverFor {
//            useMavenCentral()
//        }
//
//        val artifact = processor.artifactOf("org.jetbrains:annotations:23.0.0")
//
//        println(artifact)
//
//        org.apache.maven.model.Model
//    }
//
//    @Test
//    fun `Test deep artifact resolution`() {
//        val processor = ArtifactGraph(SimpleMaven).resolverFor {
//            useMavenCentral()
//        }
//
//        val artifact = processor.artifactOf("org.springframework:spring-context:5.3.22") {
//            includeScopes("compile", "runtime", "import")
//        }
//
//        artifact?.prettyPrint()
//    }
}