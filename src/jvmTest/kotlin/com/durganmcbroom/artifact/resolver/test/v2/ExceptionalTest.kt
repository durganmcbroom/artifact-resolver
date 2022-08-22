package com.durganmcbroom.artifact.resolver.test.v2

import com.durganmcbroom.artifact.resolver.v2.ArtifactMetadata
import com.durganmcbroom.artifact.resolver.v2.ArtifactException
import kotlin.test.Test

class ExceptionalTest {
    @Test
    fun `Test Artifact not found message`() {
        val ex = ArtifactException.ArtifactNotFound(
            object : ArtifactMetadata.Descriptor {
                override val name: String = "asdf"
            },
            listOf("Maven", "More maven", "even more maven", "Hopscotch")
        )

        println(ex.message)
    }
}