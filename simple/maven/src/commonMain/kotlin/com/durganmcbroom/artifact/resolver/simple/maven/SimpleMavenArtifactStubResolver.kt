package com.durganmcbroom.artifact.resolver.simple.maven

import arrow.core.Either
import arrow.core.continuations.either
import com.durganmcbroom.artifact.resolver.*
import com.durganmcbroom.jobs.JobResult
import com.durganmcbroom.jobs.jobScope

public open class SimpleMavenArtifactStubResolver(
    override val repositoryResolver: RepositoryStubResolver<SimpleMavenRepositoryStub, SimpleMavenRepositorySettings>,
    override val factory: RepositoryFactory<SimpleMavenRepositorySettings, SimpleMavenArtifactRequest, SimpleMavenArtifactStub, SimpleMavenArtifactReference, SimpleMavenArtifactRepository>
) : ArtifactStubResolver<SimpleMavenRepositoryStub, SimpleMavenArtifactStub, SimpleMavenArtifactReference> {
    override suspend fun resolve(
        stub: SimpleMavenArtifactStub
    ): JobResult<SimpleMavenArtifactReference,ArtifactException> = jobScope {
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