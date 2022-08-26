package com.durganmcbroom.artifact.resolver.simple.maven.layout

import arrow.core.Either
import com.durganmcbroom.artifact.resolver.CheckedResource
import com.durganmcbroom.artifact.resolver.simple.maven.HashType
import com.durganmcbroom.artifact.resolver.simple.maven.hashedResourceOf


public open class SimpleMavenReleaseFacet(
    public val url: String,
    public val preferredHash: HashType
) : SimpleMavenRepositoryFacet {
    override val type: String = "release"

    override fun resourceOf(groupId: String, artifactId: String, version: String, classifier: String?, type: String) : Either<ResourceRetrievalException, CheckedResource> {
        return versionedArtifact(groupId, artifactId, version).resourceAt("${artifactId}-${version}${classifier?.let { "-$it" } ?: ""}.$type", preferredHash)
    }

    protected fun String.resourceAt(resource: String, checksumType: HashType): Either<ResourceRetrievalException, CheckedResource> =
        hashedResourceOf(
            checksumType,
            "$this/$resource",
            run {
                val ending = when(checksumType) {
                    HashType.SHA1 -> "sha1"
                    HashType.SHA256 -> "sha256"
                    HashType.SHA512 -> "sha512"
                    HashType.MD5 -> "md5"
                }

                "$this/$resource.$ending"
            }
        )

    protected fun baseArtifact(g: String, a: String): String =
        "$url/${g.replace('.', '/')}/$a"

    protected fun versionedArtifact(g: String, a: String, v: String): String = "${baseArtifact(g, a)}/$v"
}