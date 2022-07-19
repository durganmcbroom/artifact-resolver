package com.durganmcbroom.artifact.resolver.mock.maven

import com.durganmcbroom.artifact.resolver.RepositoryReference
import com.durganmcbroom.artifact.resolver.RepositorySettings
import com.durganmcbroom.artifact.resolver.mock.maven.layout.*
import com.durganmcbroom.artifact.resolver.mock.maven.plugin.MockMavenPlugin
import com.durganmcbroom.artifact.resolver.mock.maven.plugin.MockPluginConfiguration
import com.durganmcbroom.artifact.resolver.mock.maven.plugin.MockPluginProvider
import com.durganmcbroom.artifact.resolver.mock.maven.pom.PomRepository

public class MockMavenRepositorySettings(
    _preferredHash: HashType = HashType.SHA1,
    _pluginProvider: MockPluginProvider = DelegatingMockPluginProvider(),
    _pomRepositoryReferencer: PomRepositoryReferencer = DelegatingPomRepositoryReferencer(),
    _layout: MockMavenRepositoryLayout? = null
) : RepositorySettings() {
    public var preferredHash: HashType by locking(_preferredHash)
    public var pluginProvider: MockPluginProvider by locking(_pluginProvider)
    public var repositoryReferencer: PomRepositoryReferencer by locking(_pomRepositoryReferencer)
    public var layout: MockMavenRepositoryLayout by nullableOrLateInitLocking(_layout) { IllegalStateException("A Layout must be provided!") }
    private var isBasicReferencerInstalled: Boolean = false

    public fun installPluginProvider(provider: MockPluginProvider) {
        if (!isLocked && pluginProvider is DelegatingMockPluginProvider) (pluginProvider as DelegatingMockPluginProvider).providers.add(
            provider
        )
        else throw IllegalStateException("Cannot install plugin providers in this context.")
    }

    public fun installPomRepoReferencer(referencer: PomRepositoryReferencer) {
        if (!isLocked && repositoryReferencer is DelegatingPomRepositoryReferencer) (repositoryReferencer as DelegatingPomRepositoryReferencer).referencers.add(
            referencer
        )
        else throw IllegalStateException("Cannot install pom references in this context.")
    }

    public fun useBasicRepoReferencer() {
        if (!isBasicReferencerInstalled) {
            isBasicReferencerInstalled = true
            installPomRepoReferencer(object : PomRepositoryReferencer {
                override fun reference(repo: PomRepository): RepositoryReference<*>? = when (repo.layout) {
                    "default" -> RepositoryReference(
                        MockMaven,
                        MockMavenRepositorySettings(
                            preferredHash,
                            pluginProvider,
                            repositoryReferencer,
                            DefaultMockMavenLayout(
                                repo.url,
                                preferredHash
                            )
                        )
                    )
                    "snapshot" -> RepositoryReference(
                        MockMaven,
                        MockMavenRepositorySettings(
                            preferredHash,
                            pluginProvider,
                            repositoryReferencer,
                            SnapshotMockMavenLayout(
                                repo.url,
                                preferredHash
                            )
                        )
                    )
                    else -> null
                }

                override fun referenceLayout(repo: PomRepository): MockMavenRepositoryLayout? = when(repo.layout) {
                    "default" -> DefaultMockMavenLayout(repo.url, preferredHash)
                    "snapshot" -> SnapshotMockMavenLayout(repo.url, preferredHash)
                    else -> null
                }
            })
        }
    }

    public fun useMavenCentral() {
        useBasicRepoReferencer()
        layout = MockMavenCentral(preferredHash)
    }

    public fun useMavenLocal() {
        useBasicRepoReferencer()
        layout = MavenLocalLayout
    }

    override fun toString(): String {
        return "MavenRepositorySettings(layout='${layout.type}' preferredHash=$preferredHash, pluginProvider=$pluginProvider, repositoryReferencer=$repositoryReferencer)"
    }

    private class DelegatingMockPluginProvider(
        val providers: MutableList<MockPluginProvider> = ArrayList()
    ) : MockPluginProvider {
        override fun provide(
            group: String,
            artifact: String,
            version: MockMavenPlugin.VersionDescriptor,
            configuration: MockPluginConfiguration
        ): MockMavenPlugin? = providers.firstNotNullOfOrNull { it.provide(group, artifact, version, configuration) }
    }

    private class DelegatingPomRepositoryReferencer(
        val referencers: MutableList<PomRepositoryReferencer> = ArrayList()
    ) : PomRepositoryReferencer {
        override fun reference(repo: PomRepository): RepositoryReference<*>? =
            referencers.firstNotNullOfOrNull { it.reference(repo) }

        override fun referenceLayout(repo: PomRepository): MockMavenRepositoryLayout? = referencers.firstNotNullOfOrNull { it.referenceLayout(repo) }
    }
}
