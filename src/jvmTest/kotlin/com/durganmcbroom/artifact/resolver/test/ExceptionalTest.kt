package com.durganmcbroom.artifact.resolver.test

import com.durganmcbroom.artifact.resolver.ArtifactMetadata
import com.durganmcbroom.artifact.resolver.ArtifactException
import com.durganmcbroom.artifact.resolver.IterableException
import java.lang.IllegalArgumentException
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

    @Test
    fun `Test iterable exception message and stacktrace`() {
        val exception = IterableException(
            "Exceptions have occurred!",
            listOf(
                NullPointerException(),
                IllegalArgumentException("")
            )
        )

        println(exception.message)
        exception.printStackTrace()
    }
}