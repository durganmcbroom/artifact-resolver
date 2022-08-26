package com.durganmcbroom.artifact.resolver

import arrow.core.Either

public interface ArtifactStubResolver<R: RepositoryStub, T: ArtifactStub<*, R>, out A: ArtifactReference<*, T>> {
    public val factory: RepositoryFactory<*, *, *>
    public val repositoryResolver : RepositoryStubResolver<R, *>

    public fun resolve(stub: T) : Either<ArtifactException, A>
}