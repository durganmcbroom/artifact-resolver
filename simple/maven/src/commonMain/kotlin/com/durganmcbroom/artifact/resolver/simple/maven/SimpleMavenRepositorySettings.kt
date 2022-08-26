package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.RepositorySettings
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenDefaultLayout
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SIMPLE_MAVEN_CENTRAL
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenLocalLayout
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenRepositoryLayout
import com.durganmcbroom.artifact.resolver.simple.maven.plugin.SimplePluginProvider

public open class SimpleMavenRepositorySettings @JvmOverloads constructor(
    public val layout: SimpleMavenRepositoryLayout,
    public val preferredHash: HashType,
    public val pluginProvider: SimplePluginProvider = SimplePluginProvider { _, _, _, _ -> null },
) : RepositorySettings {

    public companion object {
        @JvmStatic
        @JvmOverloads
        public fun default(
            url: String,
            releasesEnabled: Boolean = true,
            snapshotsEnabled: Boolean = true,
            preferredHash: HashType = HashType.SHA256,
            pluginProvider: SimplePluginProvider = SimplePluginProvider { _, _, _, _ -> null },
        ): SimpleMavenRepositorySettings = SimpleMavenRepositorySettings(
            SimpleMavenDefaultLayout(
                url,
                preferredHash, releasesEnabled, snapshotsEnabled
            ),
            preferredHash,
            pluginProvider
        )

        @JvmStatic
        @JvmOverloads
        public fun mavenCentral(
            preferredHash: HashType = HashType.SHA256,
            pluginProvider: SimplePluginProvider = SimplePluginProvider { _, _, _, _ -> null },
        ): SimpleMavenRepositorySettings = default(
            SIMPLE_MAVEN_CENTRAL,
            releasesEnabled = true,
            snapshotsEnabled = false,
            preferredHash,
            pluginProvider
        )

        @JvmStatic
        @JvmOverloads
        public fun local(
            preferredHash: HashType = HashType.SHA256,
            pluginProvider: SimplePluginProvider = SimplePluginProvider { _, _, _, _ -> null },
        ): SimpleMavenRepositorySettings = SimpleMavenRepositorySettings(SimpleMavenLocalLayout, preferredHash, pluginProvider)
    }
}
