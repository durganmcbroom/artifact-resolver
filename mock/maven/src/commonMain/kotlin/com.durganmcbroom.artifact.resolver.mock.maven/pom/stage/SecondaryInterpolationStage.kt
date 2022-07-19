package com.durganmcbroom.artifact.resolver.mock.maven.pom.stage

import com.durganmcbroom.artifact.resolver.mock.maven.MavenRepositoryHandler
import com.durganmcbroom.artifact.resolver.mock.maven.plugin.MockMavenPlugin
import com.durganmcbroom.artifact.resolver.mock.maven.pom.PomData
import com.durganmcbroom.artifact.resolver.mock.maven.pom.PomProcessStage

internal class SecondaryInterpolationStage :
    PomProcessStage<PluginLoadingStage.PluginLoadingData, SecondaryInterpolationStage.SecondaryInterpolationData> {
    override fun process(i: PluginLoadingStage.PluginLoadingData): SecondaryInterpolationData {
        val (data, r, mockPlugins: List<MockMavenPlugin>, parents) = i

        return PropertyReplacer.of(data, parents, *mockPlugins.toTypedArray()) {
            val newData = doInterpolation(data)

            SecondaryInterpolationData(newData, r)
        }
    }

    data class SecondaryInterpolationData(
        val data: PomData,
        val thisRepo: MavenRepositoryHandler
    ) : PomProcessStage.StageData
}