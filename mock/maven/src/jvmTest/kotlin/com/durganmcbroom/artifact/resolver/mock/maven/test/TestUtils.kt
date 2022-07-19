package com.durganmcbroom.artifact.resolver.mock.maven.test

import com.durganmcbroom.artifact.resolver.Artifact

fun Artifact.prettyPrint(indentForDepth: String = "   ") = prettyPrint(0, indentForDepth)

private fun Artifact.prettyPrint(depth: Int, indentForDepth: String) {
    val indent = (0 until depth).fold("") { acc, _ -> "$acc$indentForDepth" }

    println("$indent${meta.desc}")
    children.forEach { it.prettyPrint(depth + 1, indentForDepth) }
}