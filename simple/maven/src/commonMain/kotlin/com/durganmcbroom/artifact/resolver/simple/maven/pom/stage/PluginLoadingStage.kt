package com.durganmcbroom.artifact.resolver.simple.maven.pom.stage

import arrow.core.Either
import arrow.core.right
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenMetadataHandler
import com.durganmcbroom.artifact.resolver.simple.maven.plugin.SimpleMavenPlugin
import com.durganmcbroom.artifact.resolver.simple.maven.plugin.SimplePluginConfiguration
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomData
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomParsingException
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomProcessStage
import com.durganmcbroom.jobs.JobResult

// Maven extensions are also considered plugins
internal class PluginLoadingStage : PomProcessStage<PluginManagementInjectionStage.PluginManagementInjectionData, PluginLoadingStage.PluginLoadingData> {
    override val name = "Plugin loading"

    override suspend fun process(
        i: PluginManagementInjectionStage.PluginManagementInjectionData
    ): JobResult<PluginLoadingData,PomParsingException> {
        val (data, parents, repo) = i

        val plugins = data.build.plugins
        val extensions = data.build.extensions

        val mockPlugins = plugins.mapNotNull {
            repo.settings.pluginProvider.provide(
                it.groupId,
                it.artifactId,
                SimpleMavenPlugin.VersionDescriptor(it.version),
                SimplePluginConfiguration(it.configurations, data)
            )
        } + extensions.mapNotNull {
            repo.settings.pluginProvider.provide(
                it.groupId,
                it.artifactId,
                SimpleMavenPlugin.VersionDescriptor(it.version),
                SimplePluginConfiguration(it.configurations, data)
            )
        }

        return PluginLoadingData(data, repo, mockPlugins, parents).right()
    }

    data class PluginLoadingData(
        val data: PomData,
        val thisRepo: SimpleMavenMetadataHandler,
        val plugins: List<SimpleMavenPlugin>,
        val parents: List<PomData>
    ) : PomProcessStage.StageData
}