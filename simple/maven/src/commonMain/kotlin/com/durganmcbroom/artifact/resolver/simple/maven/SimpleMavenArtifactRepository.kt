package com.durganmcbroom.artifact.resolver.simple.maven

import arrow.core.Either
import arrow.core.continuations.either
import com.durganmcbroom.artifact.resolver.*

public open class SimpleMavenArtifactRepository(
    final override val factory: RepositoryFactory<SimpleMavenRepositorySettings, SimpleMavenArtifactRequest, SimpleMavenArtifactStub, SimpleMavenArtifactReference, SimpleMavenArtifactRepository>,
    final override val handler: SimpleMavenMetadataHandler,
    settings: SimpleMavenRepositorySettings,
) : ArtifactRepository<SimpleMavenArtifactRequest, SimpleMavenArtifactStub, SimpleMavenArtifactReference> {
    override val name: String = "simple-maven@${handler.layout.name}"
    override val stubResolver: SimpleMavenArtifactStubResolver =
        SimpleMavenArtifactStubResolver(
            SimpleMavenRepositoryStubResolver(
                settings.preferredHash,
                settings.pluginProvider
            ),
            factory
        )

    override fun get(
        request: SimpleMavenArtifactRequest,
    ): Either<ArtifactException, SimpleMavenArtifactReference> = either.eager {
        val metadata = handler.requestMetadata(request.descriptor).bind()

        val children = run {
            val rawChildren = metadata.children

            val transitiveChildren = if (request.isTransitive) rawChildren else listOf()

            val scopedChildren = transitiveChildren.filter { request.includeScopes.contains(it.scope) }

            val allowedChildren =
                scopedChildren.filterNot { request.excludeArtifacts.contains(it.descriptor.artifact) }

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