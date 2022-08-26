package com.durganmcbroom.artifact.resolver.simple.maven.pom.stage

import arrow.core.Either
import arrow.core.right
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenMetadataHandler
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomData
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomParsingException
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomProcessStage

internal class PrimaryInterpolationStage :
    PomProcessStage<PomInheritanceAssemblyStage.AssembledPomData, PrimaryInterpolationStage.PrimaryInterpolationData> {

    override val name: String = "Primary Interpolation"

    override fun process(i: PomInheritanceAssemblyStage.AssembledPomData): Either<Nothing, PrimaryInterpolationData> {
        val (data, parents, repo) = i

        return PropertyReplacer.Companion.of(data, parents) {
            val newData = doInterpolation(data)

            PrimaryInterpolationData(newData, parents, repo)
        }.right()
    }

    data class PrimaryInterpolationData(
        val pomData: PomData,
        val parents: List<PomData>,
        val thisRepo: SimpleMavenMetadataHandler
    ) : PomProcessStage.StageData

}