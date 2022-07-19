package com.durganmcbroom.artifact.resolver.mock.maven.pom.stage

import com.durganmcbroom.artifact.resolver.mock.maven.MavenRepositoryHandler
import com.durganmcbroom.artifact.resolver.mock.maven.pom.PomData
import com.durganmcbroom.artifact.resolver.mock.maven.pom.PomProcessStage

internal class PrimaryInterpolationStage :
    PomProcessStage<PomInheritanceAssemblyStage.AssembledPomData, PrimaryInterpolationStage.PrimaryInterpolationData> {
    override fun process(i: PomInheritanceAssemblyStage.AssembledPomData): PrimaryInterpolationData {
        val (data, parents, repo) = i

       return PropertyReplacer.Companion.of(data, parents) {
           val newData = doInterpolation(data)

           PrimaryInterpolationData(newData, parents, repo)
       }
    }

    data class PrimaryInterpolationData(
        val pomData: PomData,
        val parents: List<PomData>,
        val thisRepo: MavenRepositoryHandler
    ) : PomProcessStage.StageData

}