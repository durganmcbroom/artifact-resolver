package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.ArtifactMetadata
import com.durganmcbroom.resources.Resource

public open class SimpleMavenArtifactMetadata(
    desc: SimpleMavenDescriptor,
    public val resource: Resource?,
    parents: List<SimpleMavenParentInfo>
) : ArtifactMetadata<SimpleMavenDescriptor, SimpleMavenParentInfo>(desc, parents)

public data class SimpleMavenDescriptor(
    val group: String,
    val artifact: String,
    val version: String,
    val classifier: String?
) : ArtifactMetadata.Descriptor {
    override val name: String
        get() = toString()

    public companion object {
        public fun parseDescription(name: String): SimpleMavenDescriptor? =
            name.split(':').takeIf { it.size == 3 || it.size == 4 }
                ?.let { SimpleMavenDescriptor(it[0], it[1], it[2], it.getOrNull(3)) }
    }

    override fun toString(): String = "$group:$artifact:$version${classifier?.let { ":$it" } ?: ""}"
}

public data class SimpleMavenParentInfo(
    override val request: SimpleMavenArtifactRequest,
    override val candidates: List<SimpleMavenRepositorySettings>,
    val scope: String,
) : ArtifactMetadata.ParentInfo<SimpleMavenArtifactRequest, SimpleMavenRepositorySettings>(request, candidates)