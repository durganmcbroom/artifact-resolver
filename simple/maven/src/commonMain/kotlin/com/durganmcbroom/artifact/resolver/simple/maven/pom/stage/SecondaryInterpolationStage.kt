package com.durganmcbroom.artifact.resolver.simple.maven.pom.stage

import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenArtifactRepository
import com.durganmcbroom.artifact.resolver.simple.maven.plugin.SimpleMavenPlugin
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomData
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomProcessStage
import com.durganmcbroom.artifact.resolver.simple.maven.pom.stage.PluginLoadingStage.PluginLoadingData
import com.durganmcbroom.artifact.resolver.simple.maven.pom.stage.SecondaryInterpolationStage.SecondaryInterpolationData

internal class SecondaryInterpolationStage : PomProcessStage<PluginLoadingData, SecondaryInterpolationData> {
    override val name: String = "Secondary interpolation"

    override suspend fun process(i: PluginLoadingData): SecondaryInterpolationData {
        val (data, r, mockPlugins: List<SimpleMavenPlugin>, parents) = i

        return PropertyReplacer.of(data, parents, *mockPlugins.toTypedArray()) {
            val newData = doInterpolation(data)

            SecondaryInterpolationData(newData, r)
        }
    }

    data class SecondaryInterpolationData(
        val data: PomData,
        val thisRepo: SimpleMavenArtifactRepository
    ) : PomProcessStage.StageData
}