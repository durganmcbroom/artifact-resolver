package com.durganmcbroom.artifact.resolver.v2


public interface RepositoryFactory<in S : RepositorySettings, out A : ArtifactReference<*, *>, out R : ArtifactRepository<*, *>> {
    public val artifactComposer : ArtifactComposer
        get() = ArtifactComposer()
    public val stubResolver : StubResolver<*, A>

    public fun createNew(settings: S): R
}
