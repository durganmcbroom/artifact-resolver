package com.durganmcbroom.artifact.resolver.simple.maven.layout

import arrow.core.Either
import arrow.core.left
import com.durganmcbroom.artifact.resolver.CheckedResource
import com.durganmcbroom.artifact.resolver.simple.maven.HashType

public open class SimpleMavenDefaultLayout(
    public val url: String,
    preferredHash: HashType,
    public val releasesEnabled: Boolean,
    public val snapshotsEnabled: Boolean,
) : SimpleMavenRepositoryLayout {
    override val name: String = "default@$url"

    private val releaseFacet = SimpleMavenReleaseFacet(url, preferredHash)
    private val snapshotFacet = SimpleMavenSnapshotFacet(url, preferredHash)

    override fun resourceOf(
        groupId: String,
        artifactId: String,
        version: String,
        classifier: String?,
        type: String
    ): Either<ResourceRetrievalException, CheckedResource> =
        (if (version.endsWith("-SNAPSHOT") && snapshotsEnabled) snapshotFacet else if (releasesEnabled) releaseFacet else null)?.resourceOf(
            groupId,
            artifactId,
            version,
            classifier,
            type
        ) ?: ResourceRetrievalException.NoEnabledFacet("$groupId:$artifactId:$version:${classifier?.let { "-$it" } ?: ""}:$type", this).left()


}