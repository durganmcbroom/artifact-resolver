package com.durganmcbroom.artifact.resolver.simple.maven.pom.stage

import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenArtifactRepository
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomBuild
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomData
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomPlugin
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomProcessStage
import com.durganmcbroom.jobs.Job
import com.durganmcbroom.jobs.SuccessfulJob

internal class PluginManagementInjectionStage :
    PomProcessStage<PrimaryInterpolationStage.PrimaryInterpolationData, PluginManagementInjectionStage.PluginManagementInjectionData> {

    override val name: String = "Plugin management injection"

    override suspend fun process(
        i: PrimaryInterpolationStage.PrimaryInterpolationData
    ): PluginManagementInjectionData {
        val (data, parents, repo) = i

        val build = data.build

        val injectedPlugins = build.plugins.map { p ->
            val managed by lazy {
                build.pluginManagement.plugins.find { p.groupId == it.groupId && p.artifactId == it.artifactId }
            }

            // TODO Technically this injection is not correct as plugin management is only suppose to affect parents and configurations are more complicated than completely overriding.
            PomPlugin(
                p.groupId,
                p.artifactId,
                p.version ?: managed?.version,
                p.extensions ?: managed?.extensions ?: false,
                p.configurations + (managed?.configurations ?: mapOf())
            )
        }

        val newData = data.copy(
            build = PomBuild(
                data.build.extensions,
                injectedPlugins,
                data.build.pluginManagement
            )
        )

        return PluginManagementInjectionData(newData, parents, repo)
    }

    data class PluginManagementInjectionData(
        val pomData: PomData,
        val parents: List<PomData>,
        val thisRepo: SimpleMavenArtifactRepository
    ) : PomProcessStage.StageData
}