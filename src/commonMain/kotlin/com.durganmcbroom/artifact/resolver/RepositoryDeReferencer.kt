package com.durganmcbroom.artifact.resolver

public fun interface RepositoryDeReferencer<in D : ArtifactMetadata.Descriptor, in O : ArtifactResolutionOptions> {
    public fun deReference(ref: RepositoryReference<*>) : ArtifactRepository<D, O>?
}