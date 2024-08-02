package com.durganmcbroom.artifact.resolver.simple.maven.pom.stage

import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenArtifactRepository
import com.durganmcbroom.artifact.resolver.simple.maven.plugin.SimpleMavenPlugin
import com.durganmcbroom.artifact.resolver.simple.maven.plugin.SimplePluginConfiguration
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomData
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomProcessStage
import com.durganmcbroom.jobs.Job
import com.durganmcbroom.jobs.SuccessfulJob

// Maven extensions are also considered plugins
internal class PluginLoadingStage :
    PomProcessStage<PluginManagementInjectionStage.PluginManagementInjectionData, PluginLoadingStage.PluginLoadingData> {
    override val name = "Plugin loading"

    override fun process(
        i: PluginManagementInjectionStage.PluginManagementInjectionData
    ): Job<PluginLoadingData> {
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

        return SuccessfulJob { PluginLoadingData(data, repo, mockPlugins, parents) }
    }

    data class PluginLoadingData(
        val data: PomData,
        val thisRepo: SimpleMavenArtifactRepository,
        val plugins: List<SimpleMavenPlugin>,
        val parents: List<PomData>
    ) : PomProcessStage.StageData
}