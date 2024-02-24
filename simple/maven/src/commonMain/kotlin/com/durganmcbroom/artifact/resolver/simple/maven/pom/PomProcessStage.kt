package com.durganmcbroom.artifact.resolver.simple.maven.pom

import arrow.core.Either
import com.durganmcbroom.jobs.JobResult

// Stages :
// Parent Resolution
// Inheritance Assembly
// Primary Interpolation
// Plugin Loading
// Secondary Interpolation
// Dependency Management Injection
// Finalization

public interface PomProcessStage<in I: PomProcessStage.StageData, out O: PomProcessStage.StageData> {
    public val name: String

    public suspend fun process(i: I) : JobResult<O, PomParsingException>

    public interface StageData
}