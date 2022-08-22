package com.durganmcbroom.artifact.resolver.v2

import arrow.core.Either

public data class Artifact(
    public val metadata: ArtifactMetadata<*, *>,
    public val children: List<Either<ArtifactStub<*, *>, Artifact>>
)
