package com.durganmcbroom.artifact.resolver

public open class ArtifactGraphConfig<
        D : ArtifactMetadata.Descriptor,
        O : ArtifactResolutionOptions>(
    deReferencer: RepositoryDeReferencer<D, O>? = null,
) : Lockable() {
    public var deReferencer: RepositoryDeReferencer<D, O> by nullableOrLateInitLocking(deReferencer)
    public var graph: ArtifactGraph.GraphController by lockingOr { object : ArtifactGraph.GraphController {
        override val graph: MutableMap<ArtifactMetadata.Descriptor, Artifact> = HashMap()
    }}
}