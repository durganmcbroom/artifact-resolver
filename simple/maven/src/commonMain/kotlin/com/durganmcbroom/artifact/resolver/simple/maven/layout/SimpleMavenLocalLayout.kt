package com.durganmcbroom.artifact.resolver.simple.maven.layout

import com.durganmcbroom.jobs.JobResult
import com.durganmcbroom.jobs.success
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

    override suspend fun resourceOf(
        groupId: String,
        artifactId: String,
        version: String,
        classifier: String?,
        type: String
    ): JobResult<Resource, ResourceRetrievalException> = (versionedArtifact(
        groupId,
        artifactId,
        version
    ) + pathSeparator + ("$artifactId-$version${classifier?.let { "-$it" } ?: ""}.$type")).let {
        Path.of(it).toResource().mapLeft { e ->
            ResourceRetrievalException(cause = e)
        }
    }

    private fun baseArtifact(group: String, artifact: String): String =
        "$path$pathSeparator${group.replace('.', '/')}$pathSeparator$artifact"

    private fun versionedArtifact(g: String, a: String, v: String): String = "${baseArtifact(g, a)}$pathSeparator$v"
}

