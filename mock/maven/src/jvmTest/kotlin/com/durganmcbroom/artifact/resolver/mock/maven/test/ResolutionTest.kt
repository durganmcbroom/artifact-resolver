package com.durganmcbroom.artifact.resolver.mock.maven.test

import com.durganmcbroom.artifact.resolver.ArtifactResolver
import com.durganmcbroom.artifact.resolver.artifactOf
import com.durganmcbroom.artifact.resolver.mock.maven.MockMaven
import com.durganmcbroom.artifact.resolver.processorFor
import kotlin.test.Test

class ResolutionTest {
    @Test
    fun `Test basic artifact resolution`() {
        val processor = ArtifactResolver(MockMaven).processorFor {
            useMavenCentral()
        }

        val artifact = processor.artifactOf("org.jetbrains:annotations:23.0.0")

        println(artifact)
    }

    @Test
    fun `Test deep artifact resolution`() {
        val processor = ArtifactResolver(MockMaven).processorFor {
            useMavenCentral()
        }

        val artifact = processor.artifactOf("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.12.6") {
            includeScopes("compile", "runtime", "import", "test")
            exclude("stax-ex")
        }

        artifact?.prettyPrint()
    }
}