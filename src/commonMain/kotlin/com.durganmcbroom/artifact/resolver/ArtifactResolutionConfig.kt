package com.durganmcbroom.artifact.resolver

public open class ArtifactResolutionConfig<
        D : ArtifactMeta.Descriptor,
        O : ArtifactResolutionOptions>(
    deReferencer: RepositoryDeReferencer<D, O>? = null,
) : Lockable() {
    public var deReferencer: RepositoryDeReferencer<D, O> by nullableOrLateInitLocking(deReferencer)
    public var graph: ArtifactGraph.GraphController by lockingOr { DefaultArtifactGraph.DefaultGraphController() }
}