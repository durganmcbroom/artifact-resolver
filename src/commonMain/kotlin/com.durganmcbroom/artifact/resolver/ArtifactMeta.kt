package com.durganmcbroom.artifact.resolver

public open class ArtifactMeta<D: ArtifactMeta.Descriptor, T: ArtifactMeta.Transitive>(
    public val desc: Descriptor,
    public val resource: CheckedResource?,
    public val transitives: List<T>
) {
    public interface Descriptor {
        public val name: String
    }

    public interface Transitive {
        public val desc: Descriptor
        public val resolutionCandidates: List<RepositoryReference<*>>
    }
}