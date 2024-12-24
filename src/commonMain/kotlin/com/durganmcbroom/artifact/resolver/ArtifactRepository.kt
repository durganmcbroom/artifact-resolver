package com.durganmcbroom.artifact.resolver

import com.durganmcbroom.jobs.async.AsyncJob

public interface ArtifactRepository<S: RepositorySettings, R: ArtifactRequest<*>, T: ArtifactMetadata<*, *>> {
    public val name: String

    public val settings: S
    public val factory: RepositoryFactory<S, ArtifactRepository<S, R, T>>

    public fun get(
        request: R
    ) : AsyncJob<T>
}
