package com.durganmcbroom.artifact.resolver.simple.maven.pom.stage

import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenArtifactRepository
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomData
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomProcessStage
import com.durganmcbroom.jobs.Job
import com.durganmcbroom.jobs.SuccessfulJob

internal class PrimaryInterpolationStage :
    PomProcessStage<PomInheritanceAssemblyStage.AssembledPomData, PrimaryInterpolationStage.PrimaryInterpolationData> {

    override val name: String = "Primary Interpolation"

    override fun process(i: PomInheritanceAssemblyStage.AssembledPomData): Job<PrimaryInterpolationData> {
        val (data, parents, repo) = i

        return SuccessfulJob {
            PropertyReplacer.of(data, parents) {
                val newData = doInterpolation(data)

                PrimaryInterpolationData(newData, parents, repo)
            }
        }
    }

    data class PrimaryInterpolationData(
        val pomData: PomData,
        val parents: List<PomData>,
        val thisRepo: SimpleMavenArtifactRepository
    ) : PomProcessStage.StageData

}