package com.durganmcbroom.artifact.resolver.simple.maven.pom

import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenRepositorySettings
import com.durganmcbroom.artifact.resolver.simple.maven.layout.SimpleMavenDefaultLayout
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomRepositoryUpdatePolicy.*
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes

private const val DEFAULT_SCOPE = "compile"

public data class MavenDependency(
    val groupId: String,
    val artifactId: String,
    val version: String?,
    val classifier: String?,
    val scope: String?,
)

public data class ManagedDependency(
    val groupId: String,
    val artifactId: String,
    val version: String,
    val classifier: String?,
    val scope: String?,
)

public data class PomParent(
    val groupId: String,
    val artifactId: String,
    val version: String
)

public data class PomData(
    val groupId: String?,
    val artifactId: String = "<SUPER_POM>",
    val version: String?,
    val properties: Map<String, String> = mapOf(),
    val parent: PomParent?,
    val dependencyManagement: DependencyManagement = DependencyManagement(),
    val dependencies: Set<MavenDependency> = setOf(),
    val repositories: List<PomRepository> = listOf(),
    val build: PomBuild = PomBuild(),
    val packaging: String = "jar"
)

public data class DependencyManagement(
    val dependencies: Set<ManagedDependency> = setOf()
)

public data class PomRepository(
    val id: String?,
    val name: String?,
    val url: String,
    val layout: String = "default",
    val releases: PomRepositoryPolicy = PomRepositoryPolicy(),
    val snapshots: PomRepositoryPolicy = PomRepositoryPolicy()
) {
    public companion object {
        public fun SimpleMavenRepositorySettings.toPomRepository() : PomRepository? {
            val layout = layout as? SimpleMavenDefaultLayout ?: return null

            return PomRepository(
                null,
                null,
                layout.location,
                "default",
                PomRepositoryPolicy(
                    true
                ),
                PomRepositoryPolicy(
                    true
                )
            )
        }
    }
}

private const val DEFAULT_UPDATE_POLICY = "daily"
private const val INTERVAL_MATCH = "interval:"

public data class PomRepositoryPolicy(
    val enabled: Boolean = true,
    private val updatePolicy: String = DEFAULT_UPDATE_POLICY,
    val checksumPolicy: PomRepositoryChecksumPolicy = PomRepositoryChecksumPolicy.WARN,
) {
    val pomUpdatePolicy: PomRepositoryUpdatePolicy = when {
        updatePolicy == "always" -> Always
        updatePolicy == DEFAULT_UPDATE_POLICY -> Daily
        updatePolicy.startsWith(INTERVAL_MATCH) -> OnInterval(
            updatePolicy.removePrefix(
                INTERVAL_MATCH
            ).toInt()
        )
        updatePolicy == "never" -> Never
        else -> Daily
    }
}

public sealed class PomRepositoryUpdatePolicy(
    public val interval: Duration
) {
    public object Always : PomRepositoryUpdatePolicy(Duration.ZERO)
    public object Daily : PomRepositoryUpdatePolicy(1.0.days)
    public class OnInterval(minutes: Int) : PomRepositoryUpdatePolicy(minutes.minutes)
    public object Never : PomRepositoryUpdatePolicy(Duration.INFINITE)
}

public enum class PomRepositoryChecksumPolicy {
    IGNORE,
    FAIL,
    WARN
}

public data class PomBuild(
    val extensions: List<PomExtension> = listOf(),
    val plugins: List<PomPlugin> = listOf(),
    val pluginManagement: PomPluginManagement = PomPluginManagement()
)

public data class PomPluginManagement(
    val plugins: List<PomPlugin> = listOf()
)

private const val DEFAULT_PLUGIN_GROUP = "org.apache.maven.plugins"

public data class PomPlugin(
    val groupId: String = DEFAULT_PLUGIN_GROUP,
    val artifactId: String,
    val version: String?,
    val extensions: Boolean?,
    val configurations: Map<String, Any> = mapOf()
)

public data class PomExtension(
    val groupId: String,
    val artifactId: String,
    val version: String,
    val configurations: Map<String, Any> = mapOf()
)