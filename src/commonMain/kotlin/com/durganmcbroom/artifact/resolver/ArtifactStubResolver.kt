package com.durganmcbroom.artifact.resolver

import com.durganmcbroom.jobs.JobResult

public interface ArtifactStubResolver<R: RepositoryStub, T: ArtifactStub<*, R>, out A: ArtifactReference<*, T>> {
    public val factory: RepositoryFactory<*, *, T, A, *>
    public val repositoryResolver : RepositoryStubResolver<R, *>

    public suspend fun resolve(stub: T) : JobResult<A, ArtifactException>
}