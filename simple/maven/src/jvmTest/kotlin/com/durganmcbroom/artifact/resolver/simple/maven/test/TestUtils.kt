package com.durganmcbroom.artifact.resolver.simple.maven.test

import arrow.core.Either
import com.durganmcbroom.artifact.resolver.Artifact

@JvmOverloads
fun Artifact.prettyPrint(indentForDepth: String = "   ", printer: (Artifact) -> String = {it.metadata.descriptor.toString()}) = prettyPrint(0, indentForDepth, printer)

private fun Artifact.prettyPrint(depth: Int, indentForDepth: String, printer: (Artifact) -> String) {
    val indent = (0 until depth).fold("") { acc, _ -> "$acc$indentForDepth" }

    println("$indent${printer(this)}")
    children.forEach {
        when (it) {
            is Either.Right -> it.value.prettyPrint(depth + 1, indentForDepth, printer)
            is Either.Left -> println("$indent$indentForDepth${it.value.request.descriptor} - STUB")
            else -> println("Cant happen")
        }
    }
}