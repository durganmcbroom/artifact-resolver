package com.durganmcbroom.artifact.resolver.simple.maven.layout

import com.durganmcbroom.resources.Resource
import com.durganmcbroom.resources.toResource
import kotlin.io.path.Path

public expect val mavenLocal: String

public expect val pathSeparator: String

public open class SimpleMavenLocalLayout(
    final override val location: String = mavenLocal,
) : SimpleMavenRepositoryLayout {
    override val name: String = "local"

    override suspend fun resourceOf(
        groupId: String,
        artifactId: String,
        version: String,
        classifier: String?,
        type: String
    ): Resource = (versionedArtifact(
        groupId,
        artifactId,
        version
    ) + pathSeparator + ("$artifactId-$version${classifier?.let { "-$it" } ?: ""}.$type")).let {
        Path(it).toResource()
    }


    private fun baseArtifact(group: String, artifact: String): String =
        "${location.removeSuffix(pathSeparator)}$pathSeparator${group.replace('.', '/')}$pathSeparator$artifact"

    private fun versionedArtifact(g: String, a: String, v: String): String = "${baseArtifact(g, a)}$pathSeparator$v"
}

