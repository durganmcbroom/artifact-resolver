package com.durganmcbroom.artifact.resolver

public data class Artifact(
    public val metadata: ArtifactMetadata<*, *>,
    public val children: List<Artifact>
)