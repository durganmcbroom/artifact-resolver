package com.durganmcbroom.artifact.resolver.simple.maven.pom.stage

import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenRepositoryHandler
import com.durganmcbroom.artifact.resolver.simple.maven.plugin.SimpleMavenPlugin
import com.durganmcbroom.artifact.resolver.simple.maven.plugin.SimplePluginConfiguration
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomData
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomProcessStage

// Maven extensions are also considered plugins
internal class PluginLoadingStage :
    PomProcessStage<PluginManagementInjectionStage.PluginManagementInjectionData, PluginLoadingStage.PluginLoadingData> {
    override fun process(i: PluginManagementInjectionStage.PluginManagementInjectionData): PluginLoadingData {
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

//        + plugins.filter { it.extensions == true }.map { plugin ->
//            val immediateRepos = listOf(repo.layout, CentralMavenLayout) + data.repositories
//                .map(PomRepository::toSettings)
//                .map(MavenLayoutFactory::createLayout)
//
//            val artifact = immediateRepos.firstNotNullOfOrNull {
//                it.artifactOf(
//                    plugin.groupId,
//                    plugin.artifactId,
//                    checkNotNull(plugin.version) { "To load extensions plugin must have an explicit version!" },
//                    null,
//                    "pom"
//                )
//            }
//        }

        return PluginLoadingData(data, repo, mockPlugins, parents)
    }

    data class PluginLoadingData(
        val data: PomData,
        val thisRepo: SimpleMavenRepositoryHandler,
        val plugins: List<SimpleMavenPlugin>,
        val parents: List<PomData>
    ) : PomProcessStage.StageData
}