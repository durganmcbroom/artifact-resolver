package com.durganmcbroom.artifact.resolver

public interface ArtifactRequest<T: ArtifactMetadata.Descriptor> {
    public val descriptor: T
}
