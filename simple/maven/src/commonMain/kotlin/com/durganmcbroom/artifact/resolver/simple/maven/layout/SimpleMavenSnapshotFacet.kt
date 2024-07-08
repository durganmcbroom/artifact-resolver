package com.durganmcbroom.artifact.resolver.simple.maven.layout

import com.durganmcbroom.jobs.*
import com.durganmcbroom.resources.Resource
import com.durganmcbroom.resources.ResourceAlgorithm

public class SimpleMavenSnapshotFacet(
    url: String,
    preferredAlgorithm: ResourceAlgorithm,
    requireResourceVerification: Boolean
) : SimpleMavenReleaseFacet(url, preferredAlgorithm, requireResourceVerification) {
    override val type: String = "snapshot"

    override fun resourceOf(
        groupId: String,
        artifactId: String,
        version: String,
        classifier: String?,
        type: String
    ): Job<Resource> = job(JobName("Find resource: '$groupId:$artifactId:$version:$classifier'")) {
        val snapshots = parseSnapshotMetadata(versionMetaOf(groupId, artifactId, version)().merge())().merge()
        val snapshotVersion =
            snapshots[ArtifactAddress(classifier, type)] ?: throw ResourceRetrievalException.SnapshotNotFound(
                classifier,
                type,
                versionedArtifact(groupId, artifactId, version)
            )

        val versionedArtifact = versionedArtifact(groupId, artifactId, version)

        val s = "${artifactId}-${snapshotVersion}${classifier?.let { "-$it" } ?: ""}.$type"

        versionedArtifact.resourceAt(s, preferredAlgorithm, requireResourceVerification)().merge()
    }

    protected fun versionMetaOf(
        g: String,
        a: String,
        v: String
    ): Job<Resource> =
        versionedArtifact(g, a, v).resourceAt("maven-metadata.xml", preferredAlgorithm, requireResourceVerification)
}

internal data class ArtifactAddress(
    val classifier: String?,
    val type: String
)

internal expect fun parseSnapshotMetadata(resource: Resource): Job<Map<ArtifactAddress, String>>
