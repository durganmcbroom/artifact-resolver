package com.durganmcbroom.artifact.resolver


public open class ArtifactComposer {
    public open fun <M: ArtifactMetadata<*, *>> compose(
        ref: ArtifactReference<M, *>,
        children: List<Artifact<M>>,
    ): Artifact<M> = Artifact(ref.metadata, children)
}
