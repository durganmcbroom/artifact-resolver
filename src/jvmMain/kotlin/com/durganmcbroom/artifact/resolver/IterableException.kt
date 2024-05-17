package com.durganmcbroom.artifact.resolver

import java.io.PrintStream
import java.util.*

private fun followPath(top: Throwable): List<Throwable> =
    listOf(top) + (top.cause?.let(::followPath) ?: listOf())

// TODO move into the job api
public actual class IterableException actual constructor(
    message: String,
    public val exceptions: List<Throwable>
) : ArtifactException(
    "Multiple exceptions have occurred: ${
        message
            .replaceFirstChar { it.lowercase(Locale.getDefault()) }
            .removeSuffix(".")
    }." + exceptions
        .withIndex()
        .joinToString(
        separator = ""
    ) { (i, ex) ->
        "\n ${i+1}) " + followPath(ex).joinToString(separator = "  <-  ") {
            it.message?.takeUnless(String::isBlank) ?: it.toString()
        }
    }
)