package com.durganmcbroom.artifact.resolver.simple.maven.pom.stage

import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenArtifactRepository
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomData
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomProcessStage

internal class PrimaryInterpolationStage :
    PomProcessStage<PomInheritanceAssemblyStage.AssembledPomData, PrimaryInterpolationStage.PrimaryInterpolationData> {

    override val name: String = "Primary Interpolation"

    override suspend fun process(i: PomInheritanceAssemblyStage.AssembledPomData): PrimaryInterpolationData {
        val (data, parents, repo) = i

        return PropertyReplacer.of(data, parents) {
            val newData = doInterpolation(data)

            PrimaryInterpolationData(newData, parents, repo)
        }
    }

    data class PrimaryInterpolationData(
        val pomData: PomData,
        val parents: List<PomData>,
        val thisRepo: SimpleMavenArtifactRepository
    ) : PomProcessStage.StageData
}