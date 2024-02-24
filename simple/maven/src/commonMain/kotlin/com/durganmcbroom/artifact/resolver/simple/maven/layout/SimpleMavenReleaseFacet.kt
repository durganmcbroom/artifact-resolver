package com.durganmcbroom.artifact.resolver.simple.maven.layout

import com.durganmcbroom.jobs.JobResult
import com.durganmcbroom.resources.Resource
import com.durganmcbroom.resources.ResourceAlgorithm

public open class SimpleMavenReleaseFacet(
    public val url: String,
    public val preferredAlgorithm: ResourceAlgorithm,
    public val requireResourceVerification: Boolean
) : SimpleMavenRepositoryFacet {
    override val type: String = "release"

    override suspend fun resourceOf(
        groupId: String,
        artifactId: String,
        version: String,
        classifier: String?,
        type: String
    ): JobResult<Resource, ResourceRetrievalException> {
        return versionedArtifact(
            groupId,
            artifactId,
            version
        ).resourceAt("${artifactId}-${version}${classifier?.let { "-$it" } ?: ""}.$type", preferredAlgorithm, this.requireResourceVerification)
    }

    protected suspend fun String.resourceAt(
        resource: String,
        algorithm: ResourceAlgorithm,
        requireResourceVerification: Boolean,
    ): JobResult<Resource, ResourceRetrievalException> =
        verifiedResourceOf(
            "$this/$resource",
            run {
                val ending = when (algorithm) {
                    ResourceAlgorithm.SHA1 -> "sha1"
                    ResourceAlgorithm.MD5 -> "md5"
                }

                "$this/$resource.$ending"
            },
            algorithm,
            requireResourceVerification
        )

    protected fun baseArtifact(g: String, a: String): String =
        "$url/${g.replace('.', '/')}/$a"

    protected fun versionedArtifact(g: String, a: String, v: String): String = "${baseArtifact(g, a)}/$v"
}