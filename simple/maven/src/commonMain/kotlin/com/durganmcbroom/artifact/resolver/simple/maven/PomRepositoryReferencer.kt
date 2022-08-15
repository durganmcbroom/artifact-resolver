package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.RepositoryReference
import com.durganmcbroom.artifact.resolver.simple.maven.layout.DefaultSimpleMavenLayout
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenRepositoryLayout
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SnapshotSimpleMavenLayout
import com.durganmcbroom.artifact.resolver.simple.maven.plugin.SimplePluginProvider
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomRepository

public interface PomRepositoryReferencer {
    public fun reference(repo: PomRepository): RepositoryReference<*>?

    // Should not be implemented by any non-pom-compliant repositories.
    public fun referenceLayout(repo: PomRepository): SimpleMavenRepositoryLayout?
}

public class SimpleMavenPomRepositoryReferencer(
    private val preferredHash: HashType,
    private val pluginProvider: SimplePluginProvider,
    private val pomRepositoryReferencer: PomRepositoryReferencer,
) : PomRepositoryReferencer {
    override fun reference(repo: PomRepository): RepositoryReference<*>? = when (repo.layout) {
        "default" -> RepositoryReference(
            SimpleMaven,
            SimpleMavenRepositorySettings(
                preferredHash,
                pluginProvider,
                pomRepositoryReferencer,
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
                pomRepositoryReferencer,
                SnapshotSimpleMavenLayout(
                    repo.url,
                    preferredHash
                )
            )
        )
        else -> null
    }

    override fun referenceLayout(repo: PomRepository): SimpleMavenRepositoryLayout? = when (repo.layout) {
        "default" -> DefaultSimpleMavenLayout(repo.url, preferredHash)
        "snapshot" -> SnapshotSimpleMavenLayout(repo.url, preferredHash)
        else -> null
    }
}