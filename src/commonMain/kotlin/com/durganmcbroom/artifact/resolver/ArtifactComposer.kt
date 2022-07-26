package com.durganmcbroom.artifact.resolver

import arrow.core.Either

public open class ArtifactComposer {
    public open fun <T : ArtifactStub<*, *>> compose(
        ref: ArtifactReference<*, T>,
        children: List<Either<T, Artifact>>
    ): Artifact = Artifact(ref.metadata, children)
}
