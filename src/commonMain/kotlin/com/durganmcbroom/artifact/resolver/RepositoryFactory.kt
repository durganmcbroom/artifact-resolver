package com.durganmcbroom.artifact.resolver


public interface RepositoryFactory<in S : RepositorySettings, out A : ArtifactReference<*, *>, out R : ArtifactRepository<*, A>> {
    public val artifactComposer: ArtifactComposer
        get() = ArtifactComposer()

    public fun createNew(settings: S): R
}
