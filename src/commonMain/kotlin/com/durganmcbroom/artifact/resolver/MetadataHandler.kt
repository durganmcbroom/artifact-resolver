package com.durganmcbroom.artifact.resolver

import arrow.core.Either

public interface MetadataHandler<S: RepositorySettings, D: ArtifactMetadata.Descriptor, M: ArtifactMetadata<D, *>> {
    public val settings: S

    public fun parseDescriptor(desc: String) : Either<MetadataRequestException.DescriptorParseFailed, D>

    public fun requestMetadata(desc: D) : Either<MetadataRequestException, M>
}
