package com.durganmcbroom.artifact.resolver

public open class StubResolutionException(message: String) : ArtifactException(message) {
    // When we don't know the type of stub and cannot resolve.
    public object FailedToRealizeStub : StubResolutionException("Failed to realize (resolve, parse) given stub.")
}