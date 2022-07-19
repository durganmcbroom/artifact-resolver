package com.durganmcbroom.artifact.resolver.mock.maven

import com.durganmcbroom.artifact.resolver.ArtifactMeta
import com.durganmcbroom.artifact.resolver.CheckedResource
import com.durganmcbroom.artifact.resolver.RepositoryReference

public class MavenArtifactMeta(
    desc: Descriptor,
    resource: CheckedResource?,
    transitives: List<MavenTransitive>
) : ArtifactMeta<MavenDescriptor, MavenTransitive>(desc, resource, transitives)

public data class MavenDescriptor(
    val group: String,
    val artifact: String,
    val version: String,
    val classifier: String?
) : ArtifactMeta.Descriptor {
    override val name: String by ::artifact

    public companion object {
        public fun parseDescription(name: String): MavenDescriptor? =
            name.split(':').takeIf { it.size == 3 || it.size == 4 }
                ?.let { MavenDescriptor(it[0], it[1], it[2], it.getOrNull(3)) }
    }

    override fun toString(): String = "$group:$artifact:$version${classifier?.let { ":$it" } ?: ""}"
}

public data class MavenTransitive(
    override val desc: MavenDescriptor,
    override val resolutionCandidates: List<RepositoryReference<*>>,
    val scope: String
) : ArtifactMeta.Transitive