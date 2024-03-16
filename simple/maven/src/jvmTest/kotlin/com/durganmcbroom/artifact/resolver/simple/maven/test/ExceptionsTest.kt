package com.durganmcbroom.artifact.resolver.simple.maven.test

import com.durganmcbroom.artifact.resolver.MetadataRequestException
import com.durganmcbroom.artifact.resolver.createContext
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMaven
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenArtifactRequest
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenRepositorySettings
import com.durganmcbroom.jobs.job
import com.durganmcbroom.jobs.launch
import com.durganmcbroom.resources.ResourceAlgorithm
import kotlin.test.Test


class ExceptionsTest {
    @Test
    fun `Illegal repository`() {
        val context = SimpleMaven.createContext(
            SimpleMavenRepositorySettings.default(
                "aa",
                preferredHash = ResourceAlgorithm.SHA1
            )
        )

        launch {
            val r =job {
               context.getAndResolve(SimpleMavenArtifactRequest("a:a:a"))().merge()
            }()

            r.exceptionOrNull()!!.printStackTrace()
            check(r.isFailure && r.exceptionOrNull() is MetadataRequestException) {""}
        }
    }
    @Test
    fun `Reposito ry doesnt exist`() {
        val context = SimpleMaven.createContext(
            SimpleMavenRepositorySettings.default(
                "https://doesntexist.yes",
                preferredHash = ResourceAlgorithm.SHA1
            )
        )

        launch {
            val r =job {
                context.getAndResolve(SimpleMavenArtifactRequest("a:a:a"))().merge()
            }()

            r.exceptionOrNull()!!.printStackTrace()
            check(r.isFailure && r.exceptionOrNull() is MetadataRequestException) {""}
        }
    }
    @Test
    fun `Illegal artifact`() {
        val context = SimpleMaven.createContext(SimpleMavenRepositorySettings.mavenCentral())

        launch {
            val r =job {
                context.getAndResolve(SimpleMavenArtifactRequest("a:a:a"))().merge()
            }()

            r.exceptionOrNull()!!.printStackTrace()
            check(r.isFailure && r.exceptionOrNull() is MetadataRequestException) {""}
        }
    }
}