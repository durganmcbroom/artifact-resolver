package com.durganmcbroom.artifact.resolver

import com.durganmcbroom.jobs.JobResult

public interface ArtifactRepository<R: ArtifactRequest<*>, S: ArtifactStub<R, *>, out A: ArtifactReference<*, S>> {
    public val name: String

    public val handler: MetadataHandler<*, *, *>
    public val factory: RepositoryFactory<*, R, S, A, ArtifactRepository<R, S, A>>
    public val stubResolver: ArtifactStubResolver<*, S, A>

    public suspend fun get(request: R) : JobResult<A, ArtifactException>
}
