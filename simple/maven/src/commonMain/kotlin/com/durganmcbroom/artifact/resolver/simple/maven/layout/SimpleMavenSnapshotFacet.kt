package com.durganmcbroom.artifact.resolver.simple.maven.layout

import com.durganmcbroom.resources.Resource
import com.durganmcbroom.resources.ResourceAlgorithm

public class SimpleMavenSnapshotFacet(
    url: String,
    preferredAlgorithm: ResourceAlgorithm,
    private val verify: (classifier: String?, type: String) -> Boolean,
) : SimpleMavenReleaseFacet(url, preferredAlgorithm, verify) {
    override val type: String = "snapshot"

    override suspend fun resourceOf(
        groupId: String,
        artifactId: String,
        version: String,
        classifier: String?,
        type: String
    ): Resource {
        val snapshots = parseSnapshotMetadata(versionMetaOf(groupId, artifactId, version))
        val snapshotVersion =
            snapshots[ArtifactAddress(classifier, type)] ?: throw ResourceRetrievalException.SnapshotNotFound(
                classifier,
                type,
                versionedArtifact(groupId, artifactId, version)
            )

        val versionedArtifact = versionedArtifact(groupId, artifactId, version)

        val s = "${artifactId}-${snapshotVersion}${classifier?.let { "-$it" } ?: ""}.$type"

        return versionedArtifact.resourceAt(s, preferredAlgorithm, verify(classifier, type))
    }

    protected suspend fun versionMetaOf(
        g: String,
        a: String,
        v: String
    ): Resource =
        versionedArtifact(g, a, v).resourceAt("maven-metadata.xml", preferredAlgorithm, false)
}

internal data class ArtifactAddress(
    val classifier: String?,
    val type: String
)

internal expect suspend fun parseSnapshotMetadata(resource: Resource): Map<ArtifactAddress, String>
