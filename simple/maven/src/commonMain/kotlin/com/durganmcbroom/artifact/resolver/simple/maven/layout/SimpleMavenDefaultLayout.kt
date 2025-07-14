package com.durganmcbroom.artifact.resolver.simple.maven.layout

import com.durganmcbroom.resources.Resource
import com.durganmcbroom.resources.ResourceAlgorithm

public open class SimpleMavenDefaultLayout(
    final override val location: String,
    preferredAlgorithm: ResourceAlgorithm,
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
    ): Resource = (if (version.endsWith("-SNAPSHOT")) {
        snapshotFacet
    } else {
        releaseFacet
    }).resourceOf(
        groupId,
        artifactId,
        version,
        classifier,
        type
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SimpleMavenDefaultLayout) return false

        if (location != other.location) return false

        return true
    }

    override fun hashCode(): Int {
        var result = location.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }
}