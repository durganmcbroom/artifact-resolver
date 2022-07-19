package com.durganmcbroom.artifact.resolver

public data class Artifact(
    public val meta: ArtifactMeta<*, *>,
    public val children: List<Artifact>
)