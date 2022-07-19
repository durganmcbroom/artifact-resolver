package com.durganmcbroom.artifact.resolver.mock.maven

import com.durganmcbroom.artifact.resolver.RepositoryHandler
import com.durganmcbroom.artifact.resolver.RepositoryReference
import com.durganmcbroom.artifact.resolver.mock.maven.layout.MockMavenRepositoryLayout
import com.durganmcbroom.artifact.resolver.mock.maven.plugin.MockMavenPlugin
import com.durganmcbroom.artifact.resolver.mock.maven.plugin.MockPluginConfiguration
import com.durganmcbroom.artifact.resolver.mock.maven.pom.parsePom

public open class MavenRepositoryHandler(
    public val layout: MockMavenRepositoryLayout,
    override val settings: MockMavenRepositorySettings,
//    public val pluginProvider: MockPluginProvider = object : MockPluginProvider {
//        private val plugins: Map<String, Map<MockMavenPlugin.VersionDescriptor, (config: MockPluginConfiguration) -> MockMavenPlugin>> =
//            mapOf("${OsPlugin.MOCKED_GROUP}:${OsPlugin.MOCKED_ARTIFACT}" to mapOf(OsPlugin.MOCKED_VERSION to {
//                OsPlugin(
//                    it
//                )
//            }))
//
//        override fun provide(
//            group: String,
//            artifact: String,
//            version: MockMavenPlugin.VersionDescriptor,
//            configuration: MockPluginConfiguration
//        ): MockMavenPlugin? =
//            plugins["$group:$artifact"]?.entries?.find { version.matches(it.key) }?.value?.invoke(configuration)
//    }
) : RepositoryHandler<MavenDescriptor, MavenArtifactMeta, MockMavenRepositorySettings> {

    override fun metaOf(descriptor: MavenDescriptor): MavenArtifactMeta? =
        findInternal(descriptor)

    private fun findInternal(desc: MavenDescriptor): MavenArtifactMeta? {
        val (group, artifact, version, classifier) = desc

        val valueOr = layout.artifactOf(group, artifact, version, null, "pom")

        val pom = parsePom(valueOr ?: return null)

        val dependencies = pom.dependencies

        val repositories = pom.repositories.toMutableList().apply { add(RepositoryReference(MockMaven, settings)) }

        return MavenArtifactMeta(
            desc,
            if (pom.packaging != "pom") layout.artifactOf(
                group,
                artifact,
                version,
                classifier,
                pom.packaging
            ) else null,
            dependencies.map {
                MavenTransitive(
                    MavenDescriptor(
                        it.groupId,
                        it.artifactId,
                        it.version,
                        it.classifier
                    ), repositories, it.scope
                )
            },
        )
    }

    override fun descriptorOf(name: String): MavenDescriptor? = MavenDescriptor.parseDescription(name)
}

//internal fun MavenRepositorySettings.referenceFor(repo: PomRepository): RepositoryReference<MavenRepositorySettings> = repositoryReferencer.reference(repo) ?: throw IllegalStateException("Failed to reference repo: '$repo'")

internal fun MockMavenRepositorySettings.pluginFor(
    group: String,
    artifact: String,
    version: MockMavenPlugin.VersionDescriptor,
    configuration: MockPluginConfiguration
): MockMavenPlugin? = pluginProvider.provide(group, artifact, version, configuration)