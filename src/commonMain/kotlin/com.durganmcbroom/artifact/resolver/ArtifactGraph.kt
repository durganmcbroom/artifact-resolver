package com.durganmcbroom.artifact.resolver

public interface ArtifactGraph {
    public val graph: Map<ArtifactMeta.Descriptor, Artifact>

    public operator fun get(descriptor: ArtifactMeta.Descriptor) : Artifact? = graph[descriptor]

    public interface GraphController {
        public val graph: ArtifactGraph

        public fun put(artifact: Artifact)
    }
}