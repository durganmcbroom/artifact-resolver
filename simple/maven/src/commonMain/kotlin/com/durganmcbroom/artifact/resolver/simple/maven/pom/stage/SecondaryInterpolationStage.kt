package com.durganmcbroom.artifact.resolver.simple.maven.pom.stage

import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenRepositoryHandler
import com.durganmcbroom.artifact.resolver.simple.maven.plugin.SimpleMavenPlugin
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomData
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomProcessStage

internal class SecondaryInterpolationStage :
    PomProcessStage<PluginLoadingStage.PluginLoadingData, SecondaryInterpolationStage.SecondaryInterpolationData> {
    override fun process(i: PluginLoadingStage.PluginLoadingData): SecondaryInterpolationData {
        val (data, r, mockPlugins: List<SimpleMavenPlugin>, parents) = i

        return PropertyReplacer.of(data, parents, *mockPlugins.toTypedArray()) {
            val newData = doInterpolation(data)

            SecondaryInterpolationData(newData, r)
        }
    }

    data class SecondaryInterpolationData(
        val data: PomData,
        val thisRepo: SimpleMavenRepositoryHandler
    ) : PomProcessStage.StageData
}