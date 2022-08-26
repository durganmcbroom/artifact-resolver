package com.durganmcbroom.artifact.resolver.simple.maven

import arrow.core.Either
import arrow.core.continuations.either
import arrow.core.continuations.ensureNotNull
import arrow.core.rightIfNotNull
import com.durganmcbroom.artifact.resolver.MetadataHandler
import com.durganmcbroom.artifact.resolver.MetadataRequestException
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenRepositoryLayout
import com.durganmcbroom.artifact.resolver.simple.maven.pom.parsePom

public open class SimpleMavenMetadataHandler(
    final override val settings: SimpleMavenRepositorySettings,
) : MetadataHandler<SimpleMavenRepositorySettings, SimpleMavenDescriptor, SimpleMavenArtifactMetadata> {
    public open val layout: SimpleMavenRepositoryLayout by settings::layout

    override fun parseDescriptor(desc: String): Either<MetadataRequestException.DescriptorParseFailed, SimpleMavenDescriptor> =
        SimpleMavenDescriptor.parseDescription(desc).rightIfNotNull { MetadataRequestException.DescriptorParseFailed }

    override fun requestMetadata(
        desc: SimpleMavenDescriptor
    ): Either<MetadataRequestException, SimpleMavenArtifactMetadata> = either.eager {
        val (group, artifact, version, classifier) = desc

        val valueOr = layout.resourceOf(group, artifact, version, null, "pom").bind()

        val pom = parsePom(valueOr).bind()

        val dependencies = pom.dependencies

        val repositories = pom.repositories.map(::SimpleMavenRepositoryStub)

        SimpleMavenArtifactMetadata(
            desc,
            if (pom.packaging != "pom") layout.resourceOf(
                group,
                artifact,
                version,
                classifier,
                pom.packaging
            ).orNull() else null,
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
