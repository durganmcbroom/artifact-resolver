package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.RepositoryFactory

public object SimpleMaven :
    RepositoryFactory<SimpleMavenRepositorySettings, SimpleMavenArtifactRequest, SimpleMavenArtifactStub, SimpleMavenArtifactReference, SimpleMavenArtifactRepository> {
    override fun createNew(settings: SimpleMavenRepositorySettings): SimpleMavenArtifactRepository =
        SimpleMavenArtifactRepository(
            this, SimpleMavenMetadataHandler(settings), settings
        )
}