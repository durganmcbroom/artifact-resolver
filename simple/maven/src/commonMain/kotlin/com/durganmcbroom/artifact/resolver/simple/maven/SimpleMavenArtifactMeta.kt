package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.ArtifactMetadata
import com.durganmcbroom.artifact.resolver.CheckedResource
import com.durganmcbroom.artifact.resolver.RepositoryReference

public class SimpleMavenArtifactMeta(
    desc: SimpleMavenDescriptor,
    resource: CheckedResource?,
    transitives: List<SimpleMavenTransitiveInfo>
) : ArtifactMetadata<SimpleMavenDescriptor, SimpleMavenTransitiveInfo>(desc, resource, transitives)

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

public data class SimpleMavenTransitiveInfo(
    override val desc: SimpleMavenDescriptor,
    override val resolutionCandidates: List<RepositoryReference<*>>,
    val scope: String
) : ArtifactMetadata.TransitiveInfo