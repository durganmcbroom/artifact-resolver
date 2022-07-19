package com.durganmcbroom.artifact.resolver.mock.maven.pom

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
    val url: String,
    val layout: String = "default"
)

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