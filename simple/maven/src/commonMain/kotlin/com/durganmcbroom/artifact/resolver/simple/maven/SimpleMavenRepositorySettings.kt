package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.RepositorySettings
import com.durganmcbroom.artifact.resolver.simple.maven.layout.*
import com.durganmcbroom.artifact.resolver.simple.maven.layout.MAVEN_CENTRAL_REPO
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenLocalLayout
import com.durganmcbroom.artifact.resolver.simple.maven.plugin.SimplePluginProvider
import com.durganmcbroom.resources.ResourceAlgorithm

public open class SimpleMavenRepositorySettings @JvmOverloads constructor(
    public val layout: SimpleMavenRepositoryLayout,
    public val preferredHash: ResourceAlgorithm,
    public val pluginProvider: SimplePluginProvider = SimplePluginProvider { _, _, _, _ -> null },
    public val requireResourceVerification: Boolean
) : RepositorySettings {
    public companion object {
        @JvmStatic
        @JvmOverloads
        public fun default(
            url: String,
            releasesEnabled: Boolean = true,
            snapshotsEnabled: Boolean = true,
            preferredHash: ResourceAlgorithm = ResourceAlgorithm.SHA1,
            pluginProvider: SimplePluginProvider = SimplePluginProvider { _, _, _, _ -> null },
            requireResourceVerification: Boolean = false
        ): SimpleMavenRepositorySettings = SimpleMavenRepositorySettings(
            SimpleMavenDefaultLayout(
                url,
                preferredHash, releasesEnabled, snapshotsEnabled
            ) { _, type ->
                if (type == "pom") false else requireResourceVerification
            },
            preferredHash,
            pluginProvider,
            requireResourceVerification
        )

        @JvmStatic
        @JvmOverloads
        public fun mavenCentral(
            preferredHash: ResourceAlgorithm = ResourceAlgorithm.SHA1,
            pluginProvider: SimplePluginProvider = SimplePluginProvider { _, _, _, _ -> null },
        ): SimpleMavenRepositorySettings = default(
            MAVEN_CENTRAL_REPO,
            releasesEnabled = true,
            snapshotsEnabled = false,
            preferredHash,
            pluginProvider,
            true
        )

        @JvmStatic
        @JvmOverloads
        public fun local(
            path: String = mavenLocal,
            preferredHash: ResourceAlgorithm = ResourceAlgorithm.SHA1,
            pluginProvider: SimplePluginProvider = SimplePluginProvider { _, _, _, _ -> null },
            requireResourceVerification: Boolean = true
        ): SimpleMavenRepositorySettings =
            SimpleMavenRepositorySettings(SimpleMavenLocalLayout(path), preferredHash, pluginProvider, requireResourceVerification)
    }

    override fun toString(): String {
        return "SimpleMavenRepositorySettings(layout=${layout.name}, preferredHash=$preferredHash)"
    }
}
