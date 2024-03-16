package com.durganmcbroom.artifact.resolver

import com.durganmcbroom.jobs.Job

public interface ArtifactRepository<R: ArtifactRequest<*>, S: ArtifactStub<R, *>, out A: ArtifactReference<*, S>> {
    public val name: String

    public val handler: MetadataHandler<*, *, *>
    public val factory: RepositoryFactory<*, R, S, A, ArtifactRepository<R, S, A>>
    public val stubResolver: ArtifactStubResolver<*, S, A>

    public fun get(request: R) : Job<A>
}
