package com.durganmcbroom.artifact.resolver

public interface ArtifactGraphProvider<C: ArtifactGraphConfig<*, *>, out R: ArtifactGraph<*,*,*>> {
    public val key: Key
    public fun provide(config: C) : R
    public fun emptyConfig() : C

    // Should implement equals and hashcode
    public abstract class Key
}