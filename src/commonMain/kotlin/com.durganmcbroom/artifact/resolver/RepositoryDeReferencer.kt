package com.durganmcbroom.artifact.resolver

public fun interface RepositoryDeReferencer<in D : ArtifactMeta.Descriptor, O : ArtifactResolutionOptions> {
    public fun deReference(ref: RepositoryReference<*>) : ArtifactRepository<D, O>?
}