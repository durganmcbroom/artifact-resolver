package com.durganmcbroom.artifact.resolver

public interface RepositoryHandler<D: ArtifactMeta.Descriptor, out M: ArtifactMeta<D, *>, out R: RepositorySettings> {
    public val settings: R

    public fun descriptorOf(name: String) : D?

    public fun metaOf(descriptor: D) : M?
}