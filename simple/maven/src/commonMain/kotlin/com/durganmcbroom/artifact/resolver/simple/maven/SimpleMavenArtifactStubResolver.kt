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
        val settings = stub.candidates.map(repositoryResolver::resolve).map { it.merge() }

        val repositories = settings.map(factory::createNew)

        val exceptions = ArrayList<Throwable>()

        val bind = repositories.firstNotNullOfOrNull {
            val r = it.get(stub.request)()

            r.getOrNull() ?: run {
                exceptions.add(r.exceptionOrNull()!!)
                null
            }
        }

        bind ?: (if (exceptions.all { it is MetadataRequestException.MetadataNotFound }) throw ArtifactException.ArtifactNotFound(
                stub.request.descriptor,
                repositories.map { it.name })
            else throw IterableException(
                "Failed to resolve '${stub.request.descriptor}'", exceptions
            ))
    }
}