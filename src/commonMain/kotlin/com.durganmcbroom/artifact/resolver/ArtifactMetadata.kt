package com.durganmcbroom.artifact.resolver

public open class ArtifactMetadata<D: ArtifactMetadata.Descriptor, T: ArtifactMetadata.TransitiveInfo>(
    public val desc: Descriptor,
    public val resource: CheckedResource?,
    public val transitives: List<T>
) {
    public interface Descriptor {
        public val name: String
    }

    public interface TransitiveInfo {
        public val desc: Descriptor
        public val resolutionCandidates: List<RepositoryReference<*>>
    }
}