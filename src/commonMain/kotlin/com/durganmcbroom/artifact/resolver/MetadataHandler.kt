package com.durganmcbroom.artifact.resolver

import com.durganmcbroom.jobs.JobResult


public interface MetadataHandler<S: RepositorySettings, D: ArtifactMetadata.Descriptor, M: ArtifactMetadata<D, *>> {
    public val settings: S

    public fun parseDescriptor(desc: String) : JobResult<D, MetadataRequestException.DescriptorParseFailed>

    public suspend fun requestMetadata(desc: D) : JobResult<M, MetadataRequestException>
}
