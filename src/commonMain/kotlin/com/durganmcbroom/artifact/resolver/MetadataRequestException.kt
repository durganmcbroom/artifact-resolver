package com.durganmcbroom.artifact.resolver

public open class MetadataRequestException(
    message: String,
    override val cause: Throwable? = null
) : ArtifactException(message) {
    public object DescriptorParseFailed :
        MetadataRequestException("Failed to parse given descriptor name into a valid descriptor.") {
        private fun readResolve(): Any = DescriptorParseFailed
    }

    public class MetadataNotFound(
        public val descriptor: ArtifactMetadata.Descriptor,
        public val resource: String,
        cause: Throwable? = null
    ) : MetadataRequestException("Failed to find '$resource' of '$descriptor' in this handler.", cause)
}
