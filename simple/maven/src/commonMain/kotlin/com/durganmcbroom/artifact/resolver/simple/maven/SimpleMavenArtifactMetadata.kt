package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.ArtifactMetadata
import com.durganmcbroom.artifact.resolver.ArtifactStub
import com.durganmcbroom.artifact.resolver.CheckedResource

public typealias SimpleMavenArtifactStub = ArtifactStub<SimpleMavenArtifactRequest, SimpleMavenRepositoryStub>

public open class SimpleMavenArtifactMetadata(
    desc: SimpleMavenDescriptor,
    resource: CheckedResource?,
    children: List<SimpleMavenChildInfo>
) : ArtifactMetadata<SimpleMavenDescriptor, SimpleMavenChildInfo>(desc, resource, children)

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

public data class SimpleMavenChildInfo(
    override val descriptor: SimpleMavenDescriptor,
    override val candidates: List<SimpleMavenRepositoryStub>,
    val scope: String,
) : ArtifactMetadata.ChildInfo<SimpleMavenDescriptor, SimpleMavenRepositoryStub>(descriptor, candidates)