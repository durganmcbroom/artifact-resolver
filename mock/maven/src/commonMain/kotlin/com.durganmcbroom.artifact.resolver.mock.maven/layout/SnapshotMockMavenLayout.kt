package com.durganmcbroom.artifact.resolver.mock.maven.layout

import com.durganmcbroom.artifact.resolver.CheckedResource
import com.durganmcbroom.artifact.resolver.mock.maven.HashType

public class SnapshotMockMavenLayout(url: String, preferredHash: HashType) : DefaultMockMavenLayout(url, preferredHash) {
    override val type: String = "snapshot"

    override fun artifactOf(
        groupId: String,
        artifactId: String,
        version: String,
        classifier: String?,
        type: String
    ): CheckedResource? {
        val snapshots = parseSnapshotMeta(versionMetaOf(groupId, artifactId, version) ?: return null)
        val artifactVersion = snapshots[ArtifactAddress(classifier, type)] ?: return null

        val s = "${artifactId}-${artifactVersion}${classifier?.let { "-$it" } ?: ""}.$type"
        return versionedArtifact(groupId, artifactId, version).resourceAt(s, preferredHash)
    }

    protected fun versionMetaOf(g: String, a: String, v: String): CheckedResource? =
        versionedArtifact(g, a, v).resourceAt("maven-metadata.xml", preferredHash)
}

internal data class ArtifactAddress(
    val classifier: String?,
    val type: String
)

internal expect fun parseSnapshotMeta(resource: CheckedResource): Map<ArtifactAddress, String>