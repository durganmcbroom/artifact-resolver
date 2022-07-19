package com.durganmcbroom.artifact.resolver

public interface RepositoryProvider<S: RepositorySettings, out R: RepositoryHandler<*, *, S>> {
    public fun get(settings: S) : R
}