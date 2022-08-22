package com.durganmcbroom.artifact.resolver.v2

import arrow.core.Either


public open class ArtifactMetadata<D: ArtifactMetadata.Descriptor, C: ArtifactMetadata.ChildInfo<D, *, *>>(
    public open val descriptor: D,
    public open val children: List<C>
) {
    public interface Descriptor {
        public val name: String
    }

    public open class ChildInfo<D: Descriptor, R: RepositorySettings, S: RepositoryStub>(
        public open val descriptor: D,
        public open val candidates: List<Either<S, R>>
    )
}
