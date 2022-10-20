package com.durganmcbroom.artifact.resolver.simple.maven.test

import arrow.core.Either
import com.durganmcbroom.artifact.resolver.Artifact

@JvmOverloads
fun Artifact.prettyPrint(indentForDepth: String = "   ", acceptStubs: Boolean = true, printer: (Artifact) -> String = {it.metadata.descriptor.toString()}) = prettyPrint(0, indentForDepth, acceptStubs, printer)

private fun Artifact.prettyPrint(depth: Int, indentForDepth: String, acceptStubs: Boolean = true, printer: (Artifact) -> String) {
    val indent = (0 until depth).fold("") { acc, _ -> "$acc$indentForDepth" }

    println("$indent${printer(this)}")
    children.forEach {
        when (it) {
            is Either.Right -> it.value.prettyPrint(depth + 1, indentForDepth, acceptStubs, printer)
            is Either.Left -> if (acceptStubs) println("$indent$indentForDepth${it.value.request.descriptor} - STUB") else throw IllegalArgumentException("Found stub for artifact: '${it.value.request.descriptor}'")
            else -> println("Cant happen")
        }
    }
}