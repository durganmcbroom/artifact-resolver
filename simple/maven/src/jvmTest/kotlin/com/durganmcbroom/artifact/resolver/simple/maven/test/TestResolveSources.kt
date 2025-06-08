package com.durganmcbroom.artifact.resolver.simple.maven.test

import com.durganmcbroom.artifact.resolver.ResolutionContext
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMaven
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenArtifactRequest
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenRepositorySettings
import com.durganmcbroom.jobs.launch
import org.junit.jupiter.api.Test

class TestResolveSources {
    @Test
    fun `Test resolve maven sources`() {
        val maven = ResolutionContext(SimpleMaven)

        launch {
            val artifact = maven.getAndResolve(
                SimpleMavenArtifactRequest(
                    "dev.extframework:boot:3.6.2-SNAPSHOT:sources"
                ),
                SimpleMavenRepositorySettings.default(
                    url = "https://maven.extframework.dev/snapshots"
                )
            )().merge()

            artifact.prettyPrint()
        }
    }
}