package com.durganmcbroom.artifact.resolver.simple.maven.test

import com.durganmcbroom.artifact.resolver.Artifact
import com.durganmcbroom.artifact.resolver.ArtifactMetadata
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenDescriptor
import java.lang.IllegalArgumentException

@JvmOverloads
fun <T: ArtifactMetadata<*, *>> Artifact<T>.prettyPrint(
    indentForDepth: String = "   ",
    acceptStubs: Boolean = true,
    printer: (Artifact<T>) -> String = { it.metadata.descriptor.toString() }
) = prettyPrint(0, indentForDepth, acceptStubs, printer)

private fun <T:ArtifactMetadata<*, *>> Artifact<T>.prettyPrint(
    depth: Int,
    indentForDepth: String,
    acceptStubs: Boolean = true,
    printer: (Artifact<T>) -> String
) {
    val indent = (0 until depth).fold("") { acc, _ -> "$acc$indentForDepth" }

    println("$indent${printer(this)}")
    parents.forEach {
        it.prettyPrint(depth + 1, indentForDepth, acceptStubs, printer)
    }
}

interface ArtifactTreeBuilder {
    fun child(descriptor: String, configure: ArtifactTreeBuilder.() -> Unit = {})
}

fun artifactTree(descriptor: String, configure: ArtifactTreeBuilder.() -> Unit): Artifact<*> {
    class DefaultArtifactTreeBuilder(
        val descriptor: String
    ) : ArtifactTreeBuilder {
        val children = ArrayList<DefaultArtifactTreeBuilder>()
        override fun child(descriptor: String, configure: ArtifactTreeBuilder.() -> Unit) {
            val child = DefaultArtifactTreeBuilder(descriptor)
            children.add(child)
            child.configure()
        }
    }

    val builder = DefaultArtifactTreeBuilder(descriptor).apply(configure)

    fun DefaultArtifactTreeBuilder.toArtifact(): Artifact<*> {
        return Artifact(
            ArtifactMetadata(
                SimpleMavenDescriptor.parseDescription(this.descriptor)!!,
                listOf()
            ),
            children.map { it.toArtifact() } as List<Artifact<ArtifactMetadata<*, *>>>
        )
    }

    return builder.toArtifact()
}

fun Artifact<*>.checkDescriptorsEquals(
    other: Artifact<*>,
) {
    if(this.metadata.descriptor != other.metadata.descriptor) throw IllegalArgumentException("Artifact: '${this.metadata.descriptor}' should be '${other.metadata.descriptor}'")
    check(other.parents.size == this.parents.size) {"Differing number of children at artifact: '${this.metadata.descriptor}"}
    parents.zip(other.parents).forEach {
        it.first.checkDescriptorsEquals(it.second)
    }
}