package com.durganmcbroom.artifact.resolver.simple.maven.layout

import com.durganmcbroom.jobs.*
import com.durganmcbroom.resources.LocalResource
import com.durganmcbroom.resources.Resource
import com.durganmcbroom.resources.toResource
import java.nio.file.Path

public expect val mavenLocal: String

public expect val pathSeparator: String

public class SimpleMavenLocalLayout(
    private val path: String = mavenLocal,
) : SimpleMavenRepositoryLayout {
    override val name: String = "local"

    override fun resourceOf(
        groupId: String,
        artifactId: String,
        version: String,
        classifier: String?,
        type: String
    ): Job<Resource> = job {
        (versionedArtifact(
            groupId,
            artifactId,
            version
        ) + pathSeparator + ("$artifactId-$version${classifier?.let { "-$it" } ?: ""}.$type")).let {
            Path.of(it).toResource()
        }
    }.mapException { ResourceRetrievalException(cause = it) }

    private fun baseArtifact(group: String, artifact: String): String =
        "$path$pathSeparator${group.replace('.', '/')}$pathSeparator$artifact"

    private fun versionedArtifact(g: String, a: String, v: String): String = "${baseArtifact(g, a)}$pathSeparator$v"
}

