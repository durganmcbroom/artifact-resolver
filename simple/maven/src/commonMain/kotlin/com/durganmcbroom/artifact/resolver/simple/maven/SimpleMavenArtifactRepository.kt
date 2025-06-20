package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.ArtifactRepository
import com.durganmcbroom.artifact.resolver.MetadataRequestException
import com.durganmcbroom.artifact.resolver.RepositoryFactory
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenDefaultLayout
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenRepositoryLayout
import com.durganmcbroom.artifact.resolver.simple.maven.pom.parsePom
import com.durganmcbroom.resources.Resource
import com.durganmcbroom.resources.ResourceNotFoundException

public open class SimpleMavenArtifactRepository(
    final override val settings: SimpleMavenRepositorySettings,
    override val factory: RepositoryFactory<SimpleMavenRepositorySettings, SimpleMavenArtifactRepository>,
) : ArtifactRepository<SimpleMavenRepositorySettings, SimpleMavenArtifactRequest, SimpleMavenArtifactMetadata> {
    public open val layout: SimpleMavenRepositoryLayout by settings::layout
    override val name: String = "simple-maven@${settings.layout.name}"

    override suspend fun get(
        request: SimpleMavenArtifactRequest
    ): SimpleMavenArtifactMetadata {
        val metadata = requestMetadata(request.descriptor) {
            request.withNewDescriptor(it)
        }

        val transitiveChildren = if (request.isTransitive) metadata.parents else listOf()

        val scopedChildren = transitiveChildren.filter { request.includeScopes.contains(it.scope) }

        val allowedChildren =
            scopedChildren.filterNot { request.excludeArtifacts.contains(it.request.descriptor.artifact) }

        return SimpleMavenArtifactMetadata(
            metadata.descriptor,
            allowedChildren
        ) {
            metadata.jar()
        }
    }

    protected open suspend fun requestMetadata(
        desc: SimpleMavenDescriptor,
        requestBuilder: (SimpleMavenDescriptor) -> SimpleMavenArtifactRequest
    ): SimpleMavenArtifactMetadata {
        val (group, artifact, version, classifier) = desc

        val pom = try {
            val valueOr = layout.resourceOf(group, artifact, version, null, "pom")

            parsePom(valueOr)
        } catch (e: ResourceNotFoundException) {
            throw MetadataRequestException.MetadataNotFound(desc, "pom", e)
        } catch (e: Exception) {
            throw MetadataRequestException("Failed to request resource for pom: '$desc'", e)
        }

        val dependencies = pom.dependencies

        val repositories = pom.repositories.map {
            SimpleMavenRepositorySettings(
                SimpleMavenDefaultLayout(
                    it.url, settings.preferredHash,
                    it.releases.enabled,
                    it.snapshots.enabled,
                    { classifier, type ->
                        if (type == "pom") false else settings.requireResourceVerification
                    }
                ), settings.preferredHash, settings.pluginProvider, settings.requireResourceVerification
            )
        }

        suspend fun handlePackaging(packaging: String): Resource? {
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
            )
        }

        return SimpleMavenArtifactMetadata(
            desc,
            dependencies.map {
                SimpleMavenParentInfo(
                    requestBuilder(
                        SimpleMavenDescriptor(
                            it.groupId,
                            it.artifactId,
                            it.version!!,
                            it.classifier ?: desc.classifier,
                        )
                    ),
                    repositories,
                    it.scope ?: "compile"
                )
            },
        ) {
            handlePackaging(pom.packaging)
        }
    }
}