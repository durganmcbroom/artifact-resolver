package com.durganmcbroom.artifact.resolver

public class DefaultArtifactGraph private constructor(
    private val _graph : MutableMap<ArtifactMeta.Descriptor, Artifact>
): ArtifactGraph {
    override val graph: Map<ArtifactMeta.Descriptor, Artifact>
        get() = _graph.toMap()

    public class DefaultGraphController : ArtifactGraph.GraphController {
        private val _graph = HashMap<ArtifactMeta.Descriptor, Artifact>()
        override val graph: ArtifactGraph = DefaultArtifactGraph(_graph)

        override fun put(artifact: Artifact) {
            _graph[artifact.meta.desc] = artifact
        }
    }
}