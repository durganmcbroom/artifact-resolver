package com.durganmcbroom.artifact.resolver.simple.maven.layout

import arrow.core.Either
import com.durganmcbroom.artifact.resolver.CheckedResource
import com.durganmcbroom.artifact.resolver.simple.maven.localResourceOf

public expect val mavenLocal: String

public expect val pathSeparator: String

internal class SimpleMavenLocalLayout(
    private val path: String = mavenLocal,
) : SimpleMavenRepositoryLayout {
    override val name: String = "local"

    override fun resourceOf(
        groupId: String,
        artifactId: String,
        version: String,
        classifier: String?,
        type: String
    ): Either<ResourceRetrievalException, CheckedResource> = (versionedArtifact(
        groupId,
        artifactId,
        version
    ) + pathSeparator + ("$artifactId-$version${classifier?.let { "-$it" } ?: ""}.$type")).let(::localResourceOf)

    private fun baseArtifact(group: String, artifact: String): String =
        "$path$pathSeparator${group.replace('.', '/')}$pathSeparator$artifact"

    private fun versionedArtifact(g: String, a: String, v: String): String = "${baseArtifact(g, a)}$pathSeparator$v"
}

