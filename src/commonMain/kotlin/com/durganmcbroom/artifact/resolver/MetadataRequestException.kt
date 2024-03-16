package com.durganmcbroom.artifact.resolver

public open class MetadataRequestException(
    message: String,
    override val cause: Throwable? = null
) : ArtifactException(message) {
    public object DescriptorParseFailed : MetadataRequestException("Failed to parse given descriptor name into a valid descriptor.")
    public object MetadataNotFound : MetadataRequestException("Failed to find metadata in this handler.")
}
