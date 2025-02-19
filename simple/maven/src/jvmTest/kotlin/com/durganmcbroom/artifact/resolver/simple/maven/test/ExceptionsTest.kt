package com.durganmcbroom.artifact.resolver.simple.maven.test

import com.durganmcbroom.artifact.resolver.ArtifactException
import com.durganmcbroom.artifact.resolver.MetadataRequestException
import com.durganmcbroom.artifact.resolver.createContext
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMaven
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenArtifactRequest
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenRepositorySettings
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomException
import com.durganmcbroom.jobs.Job
import com.durganmcbroom.jobs.JobContext
import com.durganmcbroom.jobs.JobFacetFactory
import com.durganmcbroom.jobs.async.mapAsync
import com.durganmcbroom.jobs.job
import com.durganmcbroom.jobs.launch
import com.durganmcbroom.resources.KtorInstance
import com.durganmcbroom.resources.ResourceAlgorithm
import com.durganmcbroom.resources.ResourceOpenException
import io.ktor.client.request.get
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import kotlin.io.path.toPath
import kotlin.test.Test


class ExceptionsTest {
    @Test
    fun `Illegal repository`() {
        val context = SimpleMaven.createContext(

        )

        launch {
            val r = job {
                context.getAndResolve(SimpleMavenArtifactRequest("a:a:a"),
                    SimpleMavenRepositorySettings.default(
                        "aa",
                        preferredHash = ResourceAlgorithm.SHA1
                    ))().merge()
            }()

            r.exceptionOrNull()!!.printStackTrace()
            check(r.isFailure && r.exceptionOrNull() is MetadataRequestException) { "" }
        }
    }

    @Test
    fun `Repository doesnt exist`() {
        val context = SimpleMaven.createContext(

        )

        launch {
            val r = job {
                context.getAndResolve(SimpleMavenArtifactRequest("a:a:a"),SimpleMavenRepositorySettings.default(
                    "https://doesntexist.yes",
                    preferredHash = ResourceAlgorithm.SHA1
                ))().merge()
            }()

            r.exceptionOrNull()!!.printStackTrace()
            check(r.isFailure && r.exceptionOrNull() is MetadataRequestException && r.exceptionOrNull()?.cause is ResourceOpenException) { "" }
        }
    }

    @Test
    fun `Artifact doesnt exist`() {
        val context = SimpleMaven.createContext(

        )

        launch {
            val r = job {
                context.getAndResolve(SimpleMavenArtifactRequest("a:a:a"),SimpleMavenRepositorySettings.default(
                    "https://maven.extframework.dev/snapshots",
                    preferredHash = ResourceAlgorithm.SHA1
                ))().merge()
            }()

            println(r.exceptionOrNull()?.message)
            r.exceptionOrNull()!!.printStackTrace()
            check(r.isFailure && r.exceptionOrNull() is ArtifactException.ArtifactNotFound) { "" }
        }
    }

    @Test
    fun `Snapshot Artifact doesnt exist`() {
        val context = SimpleMaven.createContext(

        )

        launch {
            val r = job {
                context.getAndResolve(SimpleMavenArtifactRequest("a:a:a-SNAPSHOT"), SimpleMavenRepositorySettings.default(
                    "https://maven.extframework.dev/snapshots",
                    preferredHash = ResourceAlgorithm.SHA1
                ))().merge()
            }()

            println(r.exceptionOrNull()?.message)
            r.exceptionOrNull()!!.printStackTrace()
            check(r.isFailure && r.exceptionOrNull() is ArtifactException.ArtifactNotFound) { "" }
        }
    }

    @Test
    fun `Snapshot version doesnt exist`() {
        val context = SimpleMaven.createContext(

        )

        launch {
            val r = job {
                context.getAndResolve(SimpleMavenArtifactRequest("dev.extframework:ext-loader:a-SNAPSHOT"), SimpleMavenRepositorySettings.default(
                    "https://maven.extframework.dev/snapshots",
                    preferredHash = ResourceAlgorithm.SHA1
                ))().merge()
            }()

            println(r.exceptionOrNull()?.message)
            r.exceptionOrNull()!!.printStackTrace()
            check(r.isFailure && r.exceptionOrNull() is ArtifactException.ArtifactNotFound) { "" }
        }
    }

    @Test
    fun `Illegal artifact`() {
        val context = SimpleMaven.createContext()

        launch {
            val r = job {
                context.getAndResolve(SimpleMavenArtifactRequest("a:a:a"),SimpleMavenRepositorySettings.mavenCentral())().merge()
            }()

            r.exceptionOrNull()!!.printStackTrace()
            check(r.isFailure && r.exceptionOrNull() is ArtifactException.ArtifactNotFound) { "" }
        }
    }

    @Test
    fun `Test local artifact not found exceptions`() {
        val context = SimpleMaven.createContext(

        )

        launch {
            val r = job {
                context.getAndResolve(SimpleMavenArtifactRequest("a:a:a"),SimpleMavenRepositorySettings.local())().merge()
            }()

            r.exceptionOrNull()!!.printStackTrace()
            check(r.isFailure && r.exceptionOrNull() is ArtifactException.ArtifactNotFound) { "" }
        }
    }

    @Test
    fun `Test local artifacts dependencies are not found`() {
        val context = SimpleMaven.createContext(

        )

        launch {
            val r = job {
                context.getAndResolve(SimpleMavenArtifactRequest("test:artifact-1:1.0"), SimpleMavenRepositorySettings.local(
                    this::class.java.getResource("/test-repo")!!.toURI().toPath().toString()
                ))().merge()
            }()

            r.exceptionOrNull()!!.printStackTrace()
            check(r.isFailure && r.exceptionOrNull() is ArtifactException.ArtifactNotFound)
        }
    }

    @Test
    fun `Test poorly assembled pom throws correctly`() {
        val context = SimpleMaven.createContext(

        )

        launch {
            val r = job {
                context.getAndResolve(SimpleMavenArtifactRequest("test:artifact-2:1.0"),SimpleMavenRepositorySettings.local(
                    this::class.java.getResource("/test-repo")!!.toURI().toPath().toString()
                ))().merge()
            }()

            r.exceptionOrNull()!!.printStackTrace()
            check(r.exceptionOrNull() is MetadataRequestException)
        }
    }

    @Test
    fun `Test invalid pom throws correctly`() {
        val context = SimpleMaven.createContext(

        )

        launch {
            val r = job {
                context.getAndResolve(SimpleMavenArtifactRequest("test:artifact-3:1.0"), SimpleMavenRepositorySettings.local(
                    this::class.java.getResource("/test-repo")!!.toURI().toPath().toString()
                ))().merge()
            }()

            r.exceptionOrNull()!!.printStackTrace()
            check(r.exceptionOrNull() is MetadataRequestException)
        }
    }
}