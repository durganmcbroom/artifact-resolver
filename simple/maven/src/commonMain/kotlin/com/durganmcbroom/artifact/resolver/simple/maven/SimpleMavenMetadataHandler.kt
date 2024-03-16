package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.MetadataHandler
import com.durganmcbroom.artifact.resolver.MetadataRequestException
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenRepositoryLayout
import com.durganmcbroom.artifact.resolver.simple.maven.pom.parsePom
import com.durganmcbroom.jobs.*
import com.durganmcbroom.resources.Resource

public open class SimpleMavenMetadataHandler(
    final override val settings: SimpleMavenRepositorySettings,
) : MetadataHandler<SimpleMavenRepositorySettings, SimpleMavenDescriptor, SimpleMavenArtifactMetadata> {
    public open val layout: SimpleMavenRepositoryLayout by settings::layout

    override fun parseDescriptor(desc: String): Result<SimpleMavenDescriptor> = result {
        SimpleMavenDescriptor.parseDescription(desc)
            ?: throw MetadataRequestException.DescriptorParseFailed
    }

    override fun requestMetadata(
        desc: SimpleMavenDescriptor
    ): Job<SimpleMavenArtifactMetadata> =
        job(JobName("Load metadata for maven artifact: '$desc'")) {
            val (group, artifact, version, classifier) = desc

            val valueOr = layout.resourceOf(group, artifact, version, null, "pom")()
                .mapException {
                    MetadataRequestException(
                        "Failed to load pom: '$desc'", it
                    )
                }
                .merge()

            val pom = parsePom(valueOr)().merge()

            val dependencies = pom.dependencies

            val repositories = pom.repositories.map {
                SimpleMavenRepositoryStub(it, settings.requireResourceVerification)
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
                        SimpleMavenDescriptor(
                            it.groupId,
                            it.artifactId,
                            it.version!!,
                            it.classifier
                        ),
                        repositories,
                        it.scope ?: "compile"
                    )
                },
            )
        }
}
