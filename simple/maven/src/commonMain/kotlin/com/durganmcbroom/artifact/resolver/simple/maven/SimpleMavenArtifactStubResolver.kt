package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.*
import com.durganmcbroom.jobs.*

public open class SimpleMavenArtifactStubResolver(
    override val repositoryResolver: RepositoryStubResolver<SimpleMavenRepositoryStub, SimpleMavenRepositorySettings>,
    override val factory: RepositoryFactory<SimpleMavenRepositorySettings, SimpleMavenArtifactRequest, SimpleMavenArtifactStub, SimpleMavenArtifactReference, SimpleMavenArtifactRepository>
) : ArtifactStubResolver<SimpleMavenRepositoryStub, SimpleMavenArtifactStub, SimpleMavenArtifactReference> {

    override fun resolve(
        stub: SimpleMavenArtifactStub
    ): Job<SimpleMavenArtifactReference> = job {
        val settings = stub.candidates
            .map(repositoryResolver::resolve)
            .map { it.merge() }

        val repositories = settings.map(factory::createNew)

        val bind = repositories
            .map { it.get(stub.request)() }
            .firstOrNull(Result<*>::isSuccess)?.merge()

        bind
            ?: throw ArtifactException.ArtifactNotFound(stub.request.descriptor, repositories.map { it.name })
    }
}