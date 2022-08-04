package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.RepositoryHandler
import com.durganmcbroom.artifact.resolver.RepositoryReference
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenRepositoryLayout
import com.durganmcbroom.artifact.resolver.simple.maven.pom.parsePom

public open class SimpleMavenRepositoryHandler(
    public val layout: SimpleMavenRepositoryLayout,
    override val settings: SimpleMavenRepositorySettings,
) : RepositoryHandler<SimpleMavenDescriptor, SimpleMavenArtifactMeta, SimpleMavenRepositorySettings> {

    override fun metaOf(descriptor: SimpleMavenDescriptor): SimpleMavenArtifactMeta? =
        findInternal(descriptor)

    private fun findInternal(desc: SimpleMavenDescriptor): SimpleMavenArtifactMeta? {
        val (group, artifact, version, classifier) = desc

        val valueOr = layout.artifactOf(group, artifact, version, null, "pom")

        val pom = parsePom(valueOr ?: return null)

        val dependencies = pom.dependencies

        val repositories = pom.repositories.toMutableList().apply { add(RepositoryReference(SimpleMaven, settings)) }

        return SimpleMavenArtifactMeta(
            desc,
            if (pom.packaging != "pom") layout.artifactOf(
                group,
                artifact,
                version,
                classifier,
                pom.packaging
            ) else null,
            dependencies.map {
                SimpleMavenTransitiveInfo(
                    SimpleMavenDescriptor(
                        it.groupId,
                        it.artifactId,
                        it.version,
                        it.classifier
                    ), repositories, it.scope
                )
            },
        )
    }

    override fun descriptorOf(name: String): SimpleMavenDescriptor? = SimpleMavenDescriptor.parseDescription(name)
}
