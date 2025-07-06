package com.durganmcbroom.artifact.resolver.simple.maven.test

import com.durganmcbroom.artifact.resolver.ResolutionContext
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMaven
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenArtifactRequest
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenRepositorySettings
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Test

class TestResolveSources {
    @Test
    fun `Test resolve maven sources`() {
        val maven = ResolutionContext(SimpleMaven)

        runBlocking {
            val artifact = maven.getAndResolveAsync(
                SimpleMavenArtifactRequest(
                    "dev.extframework:boot:3.6.2-SNAPSHOT:sources"
                ),
                SimpleMavenRepositorySettings.default(
                    url = "https://maven.durganmcbroom.com/snapshots"
                )
            )

            artifact.prettyPrint()
        }
    }
}