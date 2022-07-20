package com.durganmcbroom.artifact.resolver

public data class RepositoryReference<S : RepositorySettings>(
    public val provider: ArtifactGraphProvider<*, ArtifactGraph<*, S, *>>,
    public val settings: S
)
