package com.durganmcbroom.artifact.resolver.simple.maven.test

import arrow.core.Either
import com.durganmcbroom.artifact.resolver.Artifact

fun Artifact.prettyPrint(indentForDepth: String = "   ") = prettyPrint(0, indentForDepth)

private fun Artifact.prettyPrint(depth: Int, indentForDepth: String) {
    val indent = (0 until depth).fold("") { acc, _ -> "$acc$indentForDepth" }

    println("$indent${metadata.descriptor}")
    children.forEach {
        when (it) {
            is Either.Right -> it.value.prettyPrint(depth + 1, indentForDepth)
            is Either.Left -> println("$indent$indentForDepth${it.value.request.descriptor} - STUB")
            else -> println("Cant happen")
        }
    }
}