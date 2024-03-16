package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.ArtifactRepository
import com.durganmcbroom.artifact.resolver.RepositoryFactory
import com.durganmcbroom.jobs.Job
import com.durganmcbroom.jobs.JobName
import com.durganmcbroom.jobs.job

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
    ): Job<SimpleMavenArtifactReference> =
        job(JobName("Resolve artifact reference for artifact: '${request.descriptor}'")){
        val metadata = handler.requestMetadata(request.descriptor)().merge()

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