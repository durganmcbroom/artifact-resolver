package com.durganmcbroom.artifact.resolver.simple.maven.layout

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

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SimpleMavenDefaultLayout) return false

        if (releasesEnabled != other.releasesEnabled) return false
        if (snapshotsEnabled != other.snapshotsEnabled) return false
        if (location != other.location) return false

        return true
    }

    override fun hashCode(): Int {
        var result = releasesEnabled.hashCode()
        result = 31 * result + snapshotsEnabled.hashCode()
        result = 31 * result + location.hashCode()
        return result
    }
}