package com.durganmcbroom.artifact.resolver

public data class ArtifactReference<M : ArtifactMetadata<*, *>, S : ArtifactStub<*, *>>(
    public val metadata: M,
    public val children: List<S>
)
