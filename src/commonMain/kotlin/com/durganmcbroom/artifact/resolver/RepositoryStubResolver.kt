package com.durganmcbroom.artifact.resolver

public fun interface RepositoryStubResolver<in R: RepositoryStub, out S: RepositorySettings> {
    public fun resolve(stub: R) : Result<S>
}

public class RepositoryStubResolutionException(reason: String) : ArtifactException("Failed to resolve the given repository stub! The reason was '$reason'.")
