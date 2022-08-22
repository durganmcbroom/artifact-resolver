package com.durganmcbroom.artifact.resolver.v2

public sealed class MetadataRequestException(message: String) : ArtifactException(message) {
    public object DescriptorParseFailed : MetadataRequestException("Failed to parse given descriptor name into a valid descriptor.")
}
