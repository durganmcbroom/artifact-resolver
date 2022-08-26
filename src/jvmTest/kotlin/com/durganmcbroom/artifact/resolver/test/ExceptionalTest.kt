package com.durganmcbroom.artifact.resolver.test

import com.durganmcbroom.artifact.resolver.ArtifactMetadata
import com.durganmcbroom.artifact.resolver.ArtifactException
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