package com.durganmcbroom.artifact.resolver.simple.maven.test

import com.durganmcbroom.artifact.resolver.Artifact
import com.durganmcbroom.artifact.resolver.createContext
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMaven
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenArtifactMetadata
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenArtifactRequest
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenRepositorySettings
import com.durganmcbroom.jobs.Job
import com.durganmcbroom.jobs.JobContext
import com.durganmcbroom.jobs.JobFacetFactory
import com.durganmcbroom.jobs.JobName
import com.durganmcbroom.jobs.async.mapAsync
import com.durganmcbroom.jobs.launch
import com.durganmcbroom.resources.KtorInstance
import com.durganmcbroom.resources.ResourceAlgorithm
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.headers
import io.ktor.client.request.url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.debug.CoroutinesBlockHoundIntegration
import kotlinx.coroutines.runBlocking
import reactor.blockhound.BlockHound
import java.net.URL
import java.util.concurrent.Executors
import kotlin.system.measureTimeMillis
import kotlin.test.Test

class ResolutionTest {
    @Test
    fun `Test basic artifact resolution`() {
        val repository = SimpleMaven.createContext(

        )
        launch {
            val artifact =
                repository.getAndResolve(SimpleMavenArtifactRequest("org.springframework:spring-context:5.3.22"),SimpleMavenRepositorySettings.mavenCentral(
                    preferredHash = ResourceAlgorithm.SHA1
                ))().merge()

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

        )

        launch {
            val artifact =
                context.getAndResolve(SimpleMavenArtifactRequest("com.sparkjava:spark-core:2.9.4"), SimpleMavenRepositorySettings.mavenCentral())().merge()

            artifact.prettyPrint()
        }
    }

    @Test
    fun `Test artifact is found even with terminating slash in repository path`() {
        val context = SimpleMaven.createContext(

        )

        launch {
            val artifact =
                context.getAndResolve(SimpleMavenArtifactRequest("org.jetbrains.kotlin:kotlin-stdlib:1.9.21"),SimpleMavenRepositorySettings.default(
                    url = "https://repo.maven.apache.org/maven2/"
                ))().merge()

            artifact.prettyPrint()
        }
    }

    @Test
    fun `Test large artifact`() {
        BlockHound.install(CoroutinesBlockHoundIntegration())
        val context = SimpleMaven.createContext(

        )

        launch {
            runBlocking {
                // Can take up to a second to get the ktor client setup...
                KtorInstance.client
                val time = System.currentTimeMillis()
                val artifact =
                    context.getAndResolveAsync(SimpleMavenArtifactRequest("dev.extframework.minecraft:minecraft-provider-def:2.0.13-SNAPSHOT"),SimpleMavenRepositorySettings.default(
                        url = "https://maven.extframework.dev/snapshots"
                    ))().merge()

                println(System.currentTimeMillis() - time)

//                artifact.prettyPrint()
            }
        }
    }

    @Test
    fun `Test snapshot artifact resolution`() {
        val context = SimpleMaven.createContext(

        )

        launch {
            val artifact: Artifact<SimpleMavenArtifactMetadata> =
                context.getAndResolve(SimpleMavenArtifactRequest("net.minecrell:ServerListPlus:3.5.0-SNAPSHOT"),SimpleMavenRepositorySettings.default(
                    "https://repo.codemc.io/repository/maven-snapshots",
                    preferredHash = ResourceAlgorithm.SHA1
                ))().merge()

//            runBlocking {
//                artifact.prettyPrint {
//                    it.metadata.descriptor.toString() + " @ " + (it.metadata.jar()?.location ?: "POM")
//                }
//            }


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