package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.*
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenDefaultLayout
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenRepositoryLayout
import com.durganmcbroom.artifact.resolver.simple.maven.pom.parsePom
import com.durganmcbroom.jobs.Job
import com.durganmcbroom.jobs.job
import com.durganmcbroom.jobs.mapException
import com.durganmcbroom.resources.Resource
import com.durganmcbroom.resources.ResourceNotFoundException

public open class SimpleMavenArtifactRepository(
    final override val settings: SimpleMavenRepositorySettings,
    override val factory: RepositoryFactory<SimpleMavenRepositorySettings, SimpleMavenArtifactRepository>,
) : ArtifactRepository<SimpleMavenRepositorySettings, SimpleMavenArtifactRequest, SimpleMavenArtifactMetadata> {
    public open val layout: SimpleMavenRepositoryLayout by settings::layout
    override val name: String = "simple-maven@${settings.layout.name}"

    override fun get(
        request: SimpleMavenArtifactRequest
    ): Job<SimpleMavenArtifactMetadata> = job {
        val metadata = requestMetadata(request.descriptor) {
            request.withNewDescriptor(it)
        }().merge()

        val transitiveChildren = if (request.isTransitive) metadata.parents else listOf()

        val scopedChildren = transitiveChildren.filter { request.includeScopes.contains(it.scope) }

        val allowedChildren =
            scopedChildren.filterNot { request.excludeArtifacts.contains(it.request.descriptor.artifact) }

        SimpleMavenArtifactMetadata(
            metadata.descriptor,
            metadata.resource,
            allowedChildren,
        )
    }

    protected open fun requestMetadata(
        desc: SimpleMavenDescriptor,
        requestBuilder: (SimpleMavenDescriptor) -> SimpleMavenArtifactRequest
    ): Job<SimpleMavenArtifactMetadata> = job {
        val (group, artifact, version, classifier) = desc

        val valueOr = layout.resourceOf(group, artifact, version, null, "pom")()
            .mapException {
                if (it is ResourceNotFoundException) MetadataRequestException.MetadataNotFound(desc, "pom", it)
                else MetadataRequestException("Failed to request resource for pom: '$desc'", it)
            }.merge()

        val pom = parsePom(valueOr)().merge()

        val dependencies = pom.dependencies

        val repositories = pom.repositories.map {
            SimpleMavenRepositorySettings(
                SimpleMavenDefaultLayout(
                    it.url, settings.preferredHash,
                    it.releases.enabled,
                    it.snapshots.enabled,
                    settings.requireResourceVerification
                ), settings.preferredHash, settings.pluginProvider, settings.requireResourceVerification
            )
        }

        fun handlePackaging(packaging: String): Resource? {
            val ending = when (packaging) {
                "jar" -> "jar"
                "war" -> "war"
                "zip" -> "zip"
                "rar" -> "jar"
                "pom" -> null
                else -> "jar"
            } ?: return null

            return layout.resourceOf(
                group,
                artifact,
                version,
                classifier,
                ending
            )().getOrNull()
        }

        SimpleMavenArtifactMetadata(
            desc,
            handlePackaging(pom.packaging),
            dependencies.map {
                SimpleMavenChildInfo(
                    requestBuilder(
                        SimpleMavenDescriptor(
                            it.groupId,
                            it.artifactId,
                            it.version!!,
                            it.classifier
                        )
                    ),
                    repositories,
                    it.scope ?: "compile"
                )
            },
        )
    }
}