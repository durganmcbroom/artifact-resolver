package com.durganmcbroom.artifact.resolver.simple.maven.pom

import com.durganmcbroom.jobs.Job

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

    public suspend fun process(i: I) : O

    public interface StageData
}