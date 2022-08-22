package com.durganmcbroom.artifact.resolver.v2

import arrow.core.Either

public interface ArtifactRepository<in R: ArtifactRequest, out A: ArtifactReference<*, *>> {
    public val handler: MetadataHandler<*, *, *>
    public val factory: RepositoryFactory<*, *, *>

    public fun get(request: R) : Either<ArtifactException, A>
}
