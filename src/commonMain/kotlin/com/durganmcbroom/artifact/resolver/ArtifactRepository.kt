package com.durganmcbroom.artifact.resolver

public interface ArtifactRepository<S: RepositorySettings, R: ArtifactRequest<*>, T: ArtifactMetadata<*, *>> {
    public val name: String

    public val settings: S
    public val factory: RepositoryFactory<S, ArtifactRepository<S, R, T>>

    public suspend fun get(
        request: R
    ) : T
}
