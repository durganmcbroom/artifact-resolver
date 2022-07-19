package com.durganmcbroom.artifact.resolver.mock.maven.layout

import com.durganmcbroom.artifact.resolver.CheckedResource
import com.durganmcbroom.artifact.resolver.mock.maven.HashType
import com.durganmcbroom.artifact.resolver.mock.maven.hashedResourceOrNull

public open class DefaultMockMavenLayout(
    public val url: String,
    public val preferredHash: HashType
) : MockMavenRepositoryLayout {
    override val type: String = "default"

    override fun artifactOf(groupId: String, artifactId: String, version: String, classifier: String?, type: String) : CheckedResource? {
        return versionedArtifact(groupId, artifactId, version).resourceAt("${artifactId}-${version}${classifier?.let { "-$it" } ?: ""}.$type", preferredHash)
    }

    protected fun String.resourceAt(resource: String, checksumType: HashType): CheckedResource? =
        hashedResourceOrNull(
            checksumType,
            "$this/$resource",
            run {
               val ending = when(checksumType) {
                    HashType.SHA1 -> "sha1"
                    HashType.MD5 -> "md5"
                }

                "$this/$resource.$ending"
            }
        )

    protected fun baseArtifact(g: String, a: String): String =
        "$url/${g.replace('.', '/')}/$a"

    protected fun versionedArtifact(g: String, a: String, v: String): String = "${baseArtifact(g, a)}/$v"
}