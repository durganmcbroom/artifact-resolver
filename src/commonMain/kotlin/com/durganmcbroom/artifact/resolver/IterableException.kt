package com.durganmcbroom.artifact.resolver

public expect class IterableException(
    message: String,
    exceptions: List<Throwable>
) : ArtifactException