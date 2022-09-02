package com.durganmcbroom.artifact.resolver


public interface RepositoryFactory<
        in S : RepositorySettings,
        Req : ArtifactRequest,
        Stub : ArtifactStub<Req, *>,
        out Ref : ArtifactReference<*, Stub>,
        out R : ArtifactRepository<Req, Stub, Ref>> {
    public val artifactComposer: ArtifactComposer
        get() = ArtifactComposer()

    public fun createNew(settings: S): R
}
