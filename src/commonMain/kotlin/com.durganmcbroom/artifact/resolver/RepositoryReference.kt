package com.durganmcbroom.artifact.resolver

public data class RepositoryReference<S : RepositorySettings>(
    public val provider: ResolutionProvider<*, ArtifactResolver<*, S, *>>,
    public val settings: S
)
