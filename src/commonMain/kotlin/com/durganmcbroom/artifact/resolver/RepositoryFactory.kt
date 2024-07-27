package com.durganmcbroom.artifact.resolver

public interface RepositoryFactory<
        in S : RepositorySettings,
        out R : ArtifactRepository<*, *, *>> {

    public fun createNew(settings: S): R
}
