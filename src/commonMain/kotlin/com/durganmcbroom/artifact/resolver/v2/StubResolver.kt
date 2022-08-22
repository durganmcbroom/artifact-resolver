package com.durganmcbroom.artifact.resolver.v2

import arrow.core.Either

public interface StubResolver<T: ArtifactStub<*, *>, out A: ArtifactReference<*, T>> {
    public val factory: RepositoryFactory<*, *, *>

    public fun resolve(stub: T) : Either<ArtifactException, A>
}