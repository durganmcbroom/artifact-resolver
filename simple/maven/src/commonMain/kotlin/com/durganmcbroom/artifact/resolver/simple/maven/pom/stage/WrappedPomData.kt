package com.durganmcbroom.artifact.resolver.simple.maven.pom.stage

import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenRepositoryHandler
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomData
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomProcessStage

internal data class WrappedPomData(
    val pomData: PomData,
    val thisRepo: SimpleMavenRepositoryHandler
) : PomProcessStage.StageData