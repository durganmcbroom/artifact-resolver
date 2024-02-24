package com.durganmcbroom.artifact.resolver.simple.maven.layout

import arrow.core.raise.ensureNotNull
import com.durganmcbroom.jobs.JobResult
import com.durganmcbroom.jobs.jobScope
import com.durganmcbroom.resources.Resource
import com.durganmcbroom.resources.ResourceAlgorithm

public class SimpleMavenSnapshotFacet(
    url: String,
    preferredAlgorithm: ResourceAlgorithm,
    requireResourceVerification: Boolean
) :
    SimpleMavenReleaseFacet(url, preferredAlgorithm, requireResourceVerification) {
    override val type: String = "snapshot"

    override suspend fun resourceOf(
        groupId: String,
        artifactId: String,
        version: String,
        classifier: String?,
        type: String
    ): JobResult<Resource, ResourceRetrievalException> = jobScope {
        val snapshots = parseSnapshotMetadata(versionMetaOf(groupId, artifactId, version).bind()).bind()
        val snapshotVersion = snapshots[ArtifactAddress(classifier, type)]

        val versionedArtifact = versionedArtifact(groupId, artifactId, version)
        ensureNotNull(snapshotVersion) {
            ResourceRetrievalException.SnapshotNotFound(
                classifier,
                type,
                versionedArtifact
            )
        }

        val s = "${artifactId}-${snapshotVersion}${classifier?.let { "-$it" } ?: ""}.$type"
//        val timeStampVersioned = "$versionedArtifact/$snapshotVersion"

        versionedArtifact.resourceAt(s, preferredAlgorithm, requireResourceVerification).bind()
//            .getOrElse {
//             Second type of snapshot repository layout.
//            timeStampVersioned.resourceAt(s, preferredHash).bind()
//        }
    }

    protected suspend fun versionMetaOf(g: String, a: String, v: String): JobResult<Resource, ResourceRetrievalException> =
        versionedArtifact(g, a, v).resourceAt("maven-metadata.xml", preferredAlgorithm, requireResourceVerification)
}

internal data class ArtifactAddress(
    val classifier: String?,
    val type: String
)

internal expect suspend fun parseSnapshotMetadata(resource: Resource): JobResult<Map<ArtifactAddress, String>, ResourceRetrievalException.MetadataParseFailed>
