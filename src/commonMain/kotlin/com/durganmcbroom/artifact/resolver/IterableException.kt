package com.durganmcbroom.artifact.resolver

@Deprecated("This should no longer be used.")
public expect class IterableException(
    message: String,
    exceptions: List<Throwable>
) : ArtifactException