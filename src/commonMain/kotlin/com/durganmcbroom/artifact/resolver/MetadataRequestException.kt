package com.durganmcbroom.artifact.resolver

public abstract class MetadataRequestException(message: String) : ArtifactException(message) {
    public object DescriptorParseFailed : MetadataRequestException("Failed to parse given descriptor name into a valid descriptor.")
    public object MetadataNotFound : MetadataRequestException("Failed to find metadata in this handler.")
}
