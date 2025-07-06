package com.durganmcbroom.artifact.resolver.simple.maven.test

import com.durganmcbroom.artifact.resolver.ArtifactException
import com.durganmcbroom.artifact.resolver.MetadataRequestException
import com.durganmcbroom.artifact.resolver.createContext
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMaven
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenArtifactRequest
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenRepositorySettings
import com.durganmcbroom.resources.ResourceAlgorithm
import com.durganmcbroom.resources.ResourceOpenException
import kotlinx.coroutines.runBlocking
import kotlin.io.path.toPath
import kotlin.test.Test


class ExceptionsTest {
    @Test
    fun `Illegal repository`() {
        val context = SimpleMaven.createContext()
        runBlocking {
            try {
                context.getAndResolveAsync(
                    SimpleMavenArtifactRequest("a:a:a"),
                    SimpleMavenRepositorySettings.default(
                        "aa",
                        preferredHash = ResourceAlgorithm.SHA1
                    )
                )

                throw Exception("There should have been an exception")
            } catch (e: Exception) {
                e.printStackTrace()
                check(e is MetadataRequestException) { "" }
            }
        }
    }

    @Test
    fun `Repository doesnt exist`() {
        val context = SimpleMaven.createContext()
        runBlocking {
            try {
                context.getAndResolveAsync(
                    SimpleMavenArtifactRequest("a:a:a"), SimpleMavenRepositorySettings.default(
                        "https://doesntexist.yes",
                        preferredHash = ResourceAlgorithm.SHA1
                    )
                )

                throw Exception("There should have been an exception")
            } catch (e: Exception) {
                e.printStackTrace()
                check(e is MetadataRequestException && e.cause is ResourceOpenException) { "" }
            }
        }
    }

    @Test
    fun `Artifact doesnt exist`() {
        val context = SimpleMaven.createContext()

        runBlocking {
            try {
                context.getAndResolveAsync(
                    SimpleMavenArtifactRequest("a:a:a"), SimpleMavenRepositorySettings.default(
                        "https://maven.durganmcbroom.com/snapshots",
                        preferredHash = ResourceAlgorithm.SHA1
                    )
                )

                throw Exception("There should have been an exception")
            } catch (e: Exception) {
                e.printStackTrace()

                check(e is ArtifactException.ArtifactNotFound) { "" }
            }
        }
    }

    @Test
    fun `Snapshot Artifact doesnt exist`() {
        val context = SimpleMaven.createContext()

        runBlocking {
            try {
                context.getAndResolveAsync(
                    SimpleMavenArtifactRequest("a:a:a-SNAPSHOT"), SimpleMavenRepositorySettings.default(
                        "https://maven.durganmcbroom.com/snapshots",
                        preferredHash = ResourceAlgorithm.SHA1
                    )
                )

                throw Exception("There should have been an exception")
            } catch (e: Exception) {
                e.printStackTrace()
                check(e is ArtifactException.ArtifactNotFound) { "" }
            }
        }
    }

    @Test
    fun `Snapshot version doesnt exist`() {
        val context = SimpleMaven.createContext()

        runBlocking {
            try {
                context.getAndResolveAsync(
                    SimpleMavenArtifactRequest("dev.extframework:ext-loader:a-SNAPSHOT"),
                    SimpleMavenRepositorySettings.default(
                        "https://maven.durganmcbroom.com/snapshots",
                        preferredHash = ResourceAlgorithm.SHA1
                    )
                )

                throw Exception("There should have been an exception")
            } catch (e: Exception) {
                e.printStackTrace()

                check(e is ArtifactException.ArtifactNotFound) { "" }
            }
        }
    }

    @Test
    fun `Illegal artifact`() {
        val context = SimpleMaven.createContext()

        runBlocking {
            try {
                context.getAndResolveAsync(
                    SimpleMavenArtifactRequest("a:a:a"),
                    SimpleMavenRepositorySettings.local()
                )

                throw Exception("There should have been an exception")
            } catch (e: Exception) {
                e.printStackTrace()
                check(e is ArtifactException.ArtifactNotFound) { "" }
            }
        }
    }

    @Test
    fun `Test local artifact not found exceptions`() {
        val context = SimpleMaven.createContext()

        runBlocking {
            try {
                context.getAndResolveAsync(
                    SimpleMavenArtifactRequest("a:a:a"),
                    SimpleMavenRepositorySettings.local()
                )

                throw Exception("There should have been an exception")
            } catch (e: Exception) {
                e.printStackTrace()

                check(e is ArtifactException.ArtifactNotFound) { "" }
            }
        }
    }

    @Test
    fun `Test local artifacts dependencies are not found`() {
        val context = SimpleMaven.createContext()

        runBlocking {
            try {
                context.getAndResolveAsync(
                    SimpleMavenArtifactRequest("test:artifact-1:1.0"), SimpleMavenRepositorySettings.local(
                        this::class.java.getResource("/test-repo")!!.toURI().toPath().toString()
                    )
                )

                throw Exception("There should have been an exception")
            } catch (e: Exception) {
                e.printStackTrace()

                check(e is ArtifactException.ArtifactNotFound) { "" }
            }
        }
    }

    @Test
    fun `Test poorly assembled pom throws correctly`() {
        val context = SimpleMaven.createContext()

        runBlocking {
            try {
                context.getAndResolveAsync(
                    SimpleMavenArtifactRequest("test:artifact-2:1.0"), SimpleMavenRepositorySettings.local(
                        this::class.java.getResource("/test-repo")!!.toURI().toPath().toString()
                    )
                )

                throw Exception("There should have been an exception")
            } catch (e: Exception) {
                e.printStackTrace()

                check(e is MetadataRequestException) { "" }
            }
        }
    }

    @Test
    fun `Test invalid pom throws correctly`() {
        val context = SimpleMaven.createContext()

        runBlocking {
            try {
                context.getAndResolveAsync(
                    SimpleMavenArtifactRequest("test:artifact-3:1.0"), SimpleMavenRepositorySettings.local(
                        this::class.java.getResource("/test-repo")!!.toURI().toPath().toString()
                    )
                )

                throw Exception("There should have been an exception")
            } catch (e: Exception) {
                e.printStackTrace()

                check(e is MetadataRequestException) { "" }
            }
        }
    }
}