package com.durganmcbroom.artifact.resolver

public data class ArtifactStub<out R: ArtifactRequest, out S: RepositoryStub>(
    public val request: R,
    public val candidates: List<S>
)
