package com.durganmcbroom.artifact.resolver

public data class Artifact<T: ArtifactMetadata<*, *>>(
    public val metadata: T,
    public val parents: List<Artifact<T>>
)
