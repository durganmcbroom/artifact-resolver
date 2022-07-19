package com.durganmcbroom.artifact.resolver.mock.maven.layout

import com.durganmcbroom.artifact.resolver.CheckedResource
import com.durganmcbroom.artifact.resolver.mock.maven.localResourceOrNull

public expect val mavenLocal: String

public expect val pathSeparator: String

internal object MavenLocalLayout : MockMavenRepositoryLayout {
    override val type: String = "local"

    override fun artifactOf(
        groupId: String,
        artifactId: String,
        version: String,
        classifier: String?,
        type: String
    ): CheckedResource? = (versionedArtifact(
        groupId,
        artifactId,
        version
    ) + pathSeparator + ("$artifactId-$version${classifier?.let { "-$it" } ?: ""}.$type")).let(::localResourceOrNull)

    private fun baseArtifact(group: String, artifact: String): String =
        "$mavenLocal$pathSeparator${group.replace('.', '/')}$pathSeparator$artifact"

    private fun versionedArtifact(g: String, a: String, v: String): String = "${baseArtifact(g, a)}$pathSeparator$v"
}

