package com.durganmcbroom.artifact.resolver

import arrow.core.Either

public interface ArtifactRepository<in R: ArtifactRequest, out A: ArtifactReference<*, *>> {
    public val name: String

    public val handler: MetadataHandler<*, *, *>
    public val factory: RepositoryFactory<*, *, *>
    public val stubResolver: ArtifactStubResolver<*, *, A>

    public fun get(request: R) : Either<ArtifactException, A>
}
