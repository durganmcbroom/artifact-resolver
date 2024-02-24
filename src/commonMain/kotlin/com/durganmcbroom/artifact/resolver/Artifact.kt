package com.durganmcbroom.artifact.resolver

public data class Artifact<T: ArtifactMetadata<*, *>>(
    public val metadata: T,
    public val children: List<Artifact<T>>
)
