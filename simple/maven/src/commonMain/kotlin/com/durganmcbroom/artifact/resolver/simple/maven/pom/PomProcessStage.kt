package com.durganmcbroom.artifact.resolver.simple.maven.pom

// Stages :
// Parent Resolution
// Inheritance Assembly
// Primary Interpolation
// Plugin Loading
// Secondary Interpolation
// Dependency Management Injection
// Finalization

public interface PomProcessStage<I: PomProcessStage.StageData, O: PomProcessStage.StageData> {
    public fun process(i: I) : O

    public interface StageData
}