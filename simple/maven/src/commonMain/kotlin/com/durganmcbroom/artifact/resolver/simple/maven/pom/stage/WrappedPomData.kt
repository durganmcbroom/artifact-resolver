package com.durganmcbroom.artifact.resolver.simple.maven.pom.stage

import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenMetadataHandler
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomData
import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomProcessStage

internal data class WrappedPomData(
    val pomData: PomData,
    val thisRepo: SimpleMavenMetadataHandler
) : PomProcessStage.StageData