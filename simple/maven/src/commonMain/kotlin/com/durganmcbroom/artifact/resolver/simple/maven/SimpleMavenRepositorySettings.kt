package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.RepositoryReference
import com.durganmcbroom.artifact.resolver.RepositorySettings
import com.durganmcbroom.artifact.resolver.simple.maven.layout.*
import com.durganmcbroom.artifact.resolver.simple.maven.plugin.SimpleMavenPlugin
import com.durganmcbroom.artifact.resolver.simple.maven.plugin.SimplePluginConfiguration
import com.durganmcbroom.artifact.resolver.simple.maven.plugin.SimplePluginProvider
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomRepository

public class SimpleMavenRepositorySettings(
    _preferredHash: HashType = HashType.SHA1,
    _pluginProvider: SimplePluginProvider = DelegatingMockPluginProvider(),
    _pomRepositoryReferencer: PomRepositoryReferencer = DelegatingPomRepositoryReferencer(),
    _layout: SimpleMavenRepositoryLayout? = null
) : RepositorySettings() {
    public var preferredHash: HashType by locking(_preferredHash)
    public var pluginProvider: SimplePluginProvider by locking(_pluginProvider)
    public var repositoryReferencer: PomRepositoryReferencer by locking(_pomRepositoryReferencer)
    public var layout: SimpleMavenRepositoryLayout by nullableOrLateInitLocking(_layout) { IllegalStateException("A Layout must be provided!") }
    private var isBasicReferencerInstalled: Boolean = false

    public fun installPluginProvider(provider: SimplePluginProvider) {
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
                        SimpleMaven,
                        SimpleMavenRepositorySettings(
                            preferredHash,
                            pluginProvider,
                            repositoryReferencer,
                            DefaultSimpleMavenLayout(
                                repo.url,
                                preferredHash
                            )
                        )
                    )
                    "snapshot" -> RepositoryReference(
                        SimpleMaven,
                        SimpleMavenRepositorySettings(
                            preferredHash,
                            pluginProvider,
                            repositoryReferencer,
                            SnapshotSimpleMavenLayout(
                                repo.url,
                                preferredHash
                            )
                        )
                    )
                    else -> null
                }

                override fun referenceLayout(repo: PomRepository): SimpleMavenRepositoryLayout? = when(repo.layout) {
                    "default" -> DefaultSimpleMavenLayout(repo.url, preferredHash)
                    "snapshot" -> SnapshotSimpleMavenLayout(repo.url, preferredHash)
                    else -> null
                }
            })
        }
    }

    public fun useMavenCentral() {
        useBasicRepoReferencer()
        layout = SimpleMavenCentral(preferredHash)
    }

    public fun useMavenLocal() {
        useBasicRepoReferencer()
        layout = SimpleMavenLocalLayout
    }

    public fun useDefaultLayout(url: String) {
        useBasicRepoReferencer()
        layout = DefaultSimpleMavenLayout(url, preferredHash)
    }

    override fun toString(): String {
        return "MavenRepositorySettings(layout='${layout.type}' preferredHash=$preferredHash, pluginProvider=$pluginProvider, repositoryReferencer=$repositoryReferencer)"
    }

    private class DelegatingMockPluginProvider(
        val providers: MutableList<SimplePluginProvider> = ArrayList()
    ) : SimplePluginProvider {
        override fun provide(
            group: String,
            artifact: String,
            version: SimpleMavenPlugin.VersionDescriptor,
            configuration: SimplePluginConfiguration
        ): SimpleMavenPlugin? = providers.firstNotNullOfOrNull { it.provide(group, artifact, version, configuration) }
    }

    private class DelegatingPomRepositoryReferencer(
        val referencers: MutableList<PomRepositoryReferencer> = ArrayList()
    ) : PomRepositoryReferencer {
        override fun reference(repo: PomRepository): RepositoryReference<*>? =
            referencers.firstNotNullOfOrNull { it.reference(repo) }

        override fun referenceLayout(repo: PomRepository): SimpleMavenRepositoryLayout? = referencers.firstNotNullOfOrNull { it.referenceLayout(repo) }
    }
}
