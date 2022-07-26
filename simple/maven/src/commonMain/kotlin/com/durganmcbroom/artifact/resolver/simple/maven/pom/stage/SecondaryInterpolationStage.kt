package com.durganmcbroom.artifact.resolver.simple.maven.pom.stage

import arrow.core.Either
import arrow.core.right
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenMetadataHandler
import com.durganmcbroom.artifact.resolver.simple.maven.plugin.SimpleMavenPlugin
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomData
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomParsingException
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomProcessStage
import com.durganmcbroom.artifact.resolver.simple.maven.pom.stage.PluginLoadingStage.PluginLoadingData
import com.durganmcbroom.artifact.resolver.simple.maven.pom.stage.SecondaryInterpolationStage.SecondaryInterpolationData

internal class SecondaryInterpolationStage : PomProcessStage<PluginLoadingData, SecondaryInterpolationData> {

    override val name: String = "Secondary interpolation"

    override fun process(i: PluginLoadingData): Either<PomParsingException, SecondaryInterpolationData> {
        val (data, r, mockPlugins: List<SimpleMavenPlugin>, parents) = i

        return PropertyReplacer.of(data, parents, *mockPlugins.toTypedArray()) {
            val newData = doInterpolation(data)

            SecondaryInterpolationData(newData, r)
        }.right()
    }

    data class SecondaryInterpolationData(
        val data: PomData,
        val thisRepo: SimpleMavenMetadataHandler
    ) : PomProcessStage.StageData
}