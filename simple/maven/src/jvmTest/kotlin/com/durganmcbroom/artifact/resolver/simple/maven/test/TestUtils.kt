package com.durganmcbroom.artifact.resolver.simple.maven.test

import arrow.core.Either
import com.durganmcbroom.artifact.resolver.Artifact
import com.durganmcbroom.jobs.Failure
import com.durganmcbroom.jobs.Success

@JvmOverloads
fun Artifact<*>.prettyPrint(indentForDepth: String = "   ", acceptStubs: Boolean = true, printer: (Artifact<*>) -> String = {it.metadata.descriptor.toString()}) = prettyPrint(0, indentForDepth, acceptStubs, printer)

private fun Artifact<*>.prettyPrint(depth: Int, indentForDepth: String, acceptStubs: Boolean = true, printer: (Artifact<*>) -> String) {
    val indent = (0 until depth).fold("") { acc, _ -> "$acc$indentForDepth" }

    println("$indent${printer(this)}")
    children.forEach {
        it.prettyPrint(depth + 1, indentForDepth, acceptStubs, printer)
//        when (it) {
//            is Success<*> -> it.value.prettyPrint(depth + 1, indentForDepth, acceptStubs, printer)
//            is Failure<*> -> if (acceptStubs) println("$indent$indentForDepth${it.value.request.descriptor} - STUB") else throw IllegalArgumentException("Found stub for artifact: '${it.value.request.descriptor}'")
//            else -> println("Cant happen")
//        }
    }
}