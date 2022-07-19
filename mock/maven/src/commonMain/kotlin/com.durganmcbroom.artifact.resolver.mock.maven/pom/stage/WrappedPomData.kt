package com.durganmcbroom.artifact.resolver.mock.maven.pom.stage

import com.durganmcbroom.artifact.resolver.mock.maven.MavenRepositoryHandler
import com.durganmcbroom.artifact.resolver.mock.maven.pom.PomData
import com.durganmcbroom.artifact.resolver.mock.maven.pom.PomProcessStage

internal data class WrappedPomData(
    val pomData: PomData,
    val thisRepo: MavenRepositoryHandler
) : PomProcessStage.StageData