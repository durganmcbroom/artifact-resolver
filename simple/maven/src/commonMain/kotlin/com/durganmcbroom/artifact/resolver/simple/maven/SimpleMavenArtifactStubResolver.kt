package com.durganmcbroom.artifact.resolver.simple.maven

import arrow.core.Either
import arrow.core.continuations.either
import com.durganmcbroom.artifact.resolver.*

public open class SimpleMavenArtifactStubResolver(
    override val repositoryResolver: RepositoryStubResolver<SimpleMavenRepositoryStub, SimpleMavenRepositorySettings>,
    override val factory: RepositoryFactory<SimpleMavenRepositorySettings, SimpleMavenArtifactReference, ArtifactRepository<SimpleMavenArtifactRequest, SimpleMavenArtifactReference>>
) : ArtifactStubResolver<SimpleMavenRepositoryStub, SimpleMavenArtifactStub, SimpleMavenArtifactReference> {
    override fun resolve(
        stub: SimpleMavenArtifactStub
    ): Either<ArtifactException, SimpleMavenArtifactReference> = either.eager {
        val settings = stub.candidates
            .map(repositoryResolver::resolve)
            .map { it.bind() }

        val repositories = settings.map(factory::createNew)

        val bind = repositories
            .map { it.get(stub.request) }
            .firstOrNull(Either<*, *>::isRight)?.bind()

        bind
            ?: shift(ArtifactException.ArtifactNotFound(stub.request.descriptor, repositories.map { it.name }))
    }
}