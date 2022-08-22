package com.durganmcbroom.artifact.resolver.v2

public data class ArtifactStub<out D: ArtifactMetadata.Descriptor, out R: RepositoryStub>(
    public val descriptor: D,
    public val candidates: List<R>
)
