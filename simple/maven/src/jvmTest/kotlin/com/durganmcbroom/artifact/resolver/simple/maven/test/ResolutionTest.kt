package com.durganmcbroom.artifact.resolver.simple.maven.test

import com.durganmcbroom.artifact.resolver.ArtifactGraph
import com.durganmcbroom.artifact.resolver.artifactOf
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMaven
import com.durganmcbroom.artifact.resolver.resolverFor
import kotlin.test.Test

class ResolutionTest {
    @Test
    fun `Test basic artifact resolution`() {
        val processor = ArtifactGraph(SimpleMaven).resolverFor {
            useMavenCentral()
        }

        val artifact = processor.artifactOf("org.jetbrains:annotations:23.0.0")

        println(artifact)

        org.apache.maven.model.Model
    }

    @Test
    fun `Test deep artifact resolution`() {
        val processor = ArtifactGraph(SimpleMaven).resolverFor {
            useMavenCentral()
        }

        val artifact = processor.artifactOf("org.springframework:spring-context:5.3.22") {
            includeScopes("compile", "runtime", "import")
        }

        artifact?.prettyPrint()
    }
}