package com.durganmcbroom.artifact.resolver.simple.maven

import arrow.core.Either
import arrow.core.continuations.either
import com.durganmcbroom.artifact.resolver.*

public open class SimpleMavenArtifactRepository(
    final override val factory: RepositoryFactory<SimpleMavenRepositorySettings, SimpleMavenArtifactReference, ArtifactRepository<SimpleMavenArtifactRequest, SimpleMavenArtifactReference>>,
    override val handler: SimpleMavenMetadataHandler,
    settings: SimpleMavenRepositorySettings,
) : ArtifactRepository<SimpleMavenArtifactRequest, SimpleMavenArtifactReference> {
    override val name: String = "Simple Maven"
    override val stubResolver: SimpleMavenArtifactStubResolver =
        SimpleMavenArtifactStubResolver(
            SimpleMavenRepositoryStubResolver(
                settings.preferredHash,
                settings.pluginProvider
            ),
            factory
        )

    override fun get(
        request: SimpleMavenArtifactRequest
    ): Either<ArtifactException, SimpleMavenArtifactReference> = either.eager {
        val metadata = handler.requestMetadata(request.descriptor).bind()

        val children = run {
            val rawChildren = metadata.children

            val transitiveChildren = if (request.isTransitive) rawChildren else listOf()

            val scopedChildren = transitiveChildren.filter { request.scopes.shouldInclude(it.scope) }

            val allowedChildren =
                scopedChildren.filter { request.artifacts.shouldInclude(it.descriptor.artifact) }

            allowedChildren.map {
                SimpleMavenArtifactStub(
                    request.withNewDescriptor(it.descriptor),
                    it.candidates
                )
            }
        }

        SimpleMavenArtifactReference(
            metadata,
            children
        )
    }
}