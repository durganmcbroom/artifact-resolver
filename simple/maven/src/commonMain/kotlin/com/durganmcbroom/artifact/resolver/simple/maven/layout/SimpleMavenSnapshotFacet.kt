package com.durganmcbroom.artifact.resolver.simple.maven.layout

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.continuations.ensureNotNull
import arrow.core.getOrElse
import com.durganmcbroom.artifact.resolver.CheckedResource
import com.durganmcbroom.artifact.resolver.simple.maven.HashType

public class SimpleMavenSnapshotFacet(url: String, preferredHash: HashType) : SimpleMavenReleaseFacet(url, preferredHash) {
    override val type: String = "snapshot"

    override fun resourceOf(
        groupId: String,
        artifactId: String,
        version: String,
        classifier: String?,
        type: String
    ): Either<ResourceRetrievalException, CheckedResource> = either.eager {
        val snapshots = parseSnapshotMetadata(versionMetaOf(groupId, artifactId, version).bind()).bind()
        val snapshotVersion = snapshots[ArtifactAddress(classifier, type)]

        val versionedArtifact = versionedArtifact(groupId, artifactId, version)
        ensureNotNull(snapshotVersion) { ResourceRetrievalException.SnapshotNotFound(classifier, type, versionedArtifact) }

        val s = "${artifactId}-${snapshotVersion}${classifier?.let { "-$it" } ?: ""}.$type"
        val timeStampVersioned = "$versionedArtifact/$snapshotVersion"

        versionedArtifact.resourceAt(s, preferredHash).bind()
//            .getOrElse {
//             Second type of snapshot repository layout.
//            timeStampVersioned.resourceAt(s, preferredHash).bind()
//        }
    }

    protected fun versionMetaOf(g: String, a: String, v: String): Either<ResourceRetrievalException, CheckedResource> =
        versionedArtifact(g, a, v).resourceAt("maven-metadata.xml", preferredHash)
}

internal data class ArtifactAddress(
    val classifier: String?,
    val type: String
)

internal expect fun parseSnapshotMetadata(resource: CheckedResource): Either<ResourceRetrievalException.MetadataParseFailed, Map<ArtifactAddress, String>>
