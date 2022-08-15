package com.durganmcbroom.artifact.resolver

public interface RepositoryHandler<D: ArtifactMetadata.Descriptor, out M: ArtifactMetadata<D, *>, out R: RepositorySettings> {
    public val settings: R

    public fun descriptorOf(name: String) : D?

    public fun metadataOf(descriptor: D) : M?
}