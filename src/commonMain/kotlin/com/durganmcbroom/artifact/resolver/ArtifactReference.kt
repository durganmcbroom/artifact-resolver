package com.durganmcbroom.artifact.resolver

public data class ArtifactReference<out M : ArtifactMetadata<*, *>, out S : ArtifactStub<*, *>>(
    public val metadata: M,
    public val children: List<S>
)
