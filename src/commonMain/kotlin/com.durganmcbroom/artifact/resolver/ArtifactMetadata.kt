package com.durganmcbroom.artifact.resolver

public open class ArtifactMetadata<D: ArtifactMetadata.Descriptor, C: ArtifactMetadata.ChildInfo>(
    public val desc: D,
    public val resource: CheckedResource?,
    public val children: List<C>
) {
    public interface Descriptor {
        public val name: String
    }

    public interface ChildInfo {
        public val desc: Descriptor
        public val resolutionCandidates: List<RepositoryReference<*>>
    }
}