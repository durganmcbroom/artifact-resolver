package com.durganmcbroom.artifact.resolver.simple.maven.layout

import com.durganmcbroom.jobs.async.asyncJob
import com.durganmcbroom.resources.Resource
import com.durganmcbroom.resources.ResourceAlgorithm

public open class SimpleMavenDefaultLayout(
    final override val location: String,
    preferredAlgorithm: ResourceAlgorithm,
    public val releasesEnabled: Boolean,
    public val snapshotsEnabled: Boolean,
    verify: (classifier: String?, type: String) -> Boolean,
) : SimpleMavenRepositoryLayout {
    override val name: String = "default@$location"

    private val releaseFacet = SimpleMavenReleaseFacet(location, preferredAlgorithm, verify)
    private val snapshotFacet = SimpleMavenSnapshotFacet(location, preferredAlgorithm, verify)

    override suspend fun resourceOf(
        groupId: String,
        artifactId: String,
        version: String,
        classifier: String?,
        type: String
    ): Resource =
        (if (version.endsWith("-SNAPSHOT") && snapshotsEnabled) {
            snapshotFacet
        } else if (releasesEnabled) {
            releaseFacet
        } else {
            null
        })?.resourceOf(
            groupId,
            artifactId,
            version,
            classifier,
            type
        ) ?: throw ResourceRetrievalException.NoEnabledFacet(
            "$groupId:$artifactId:$version:${classifier?.let { "-$it" } ?: ""}:$type",
            this@SimpleMavenDefaultLayout
        )
}