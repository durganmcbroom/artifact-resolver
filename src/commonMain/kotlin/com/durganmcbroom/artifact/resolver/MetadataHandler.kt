package com.durganmcbroom.artifact.resolver

import com.durganmcbroom.jobs.Job


public interface MetadataHandler<S: RepositorySettings, D: ArtifactMetadata.Descriptor, M: ArtifactMetadata<D, *>> {
    public val settings: S

//    public fun parseDescriptor(desc: String) : Result<D>

    public fun requestMetadata(desc: D) : Job<M>
}
