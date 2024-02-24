package com.durganmcbroom.artifact.resolver.simple.maven.layout

import com.durganmcbroom.artifact.resolver.MetadataRequestException

public open class ResourceRetrievalException(
    message: String = "Previous error(s) were thrown while loading a resource",
    override val cause: Throwable? = null
) : MetadataRequestException(message) {
    public class IllegalState(reason: String) : ResourceRetrievalException(reason, null)

    public class ChecksumFileNotFound(location: String, type: String) :
        ResourceRetrievalException("Failed to find the checksum file: '$location'. Make sure the type (was '$type') is correct and the artifact exists!",null)

    public class ChecksumValidationFailed(location: String, fromChecksum: String) :
        ResourceRetrievalException("Failed to validate checksum for file '$location', checksum was '$fromChecksum'.",null)

    public class NoEnabledFacet(descriptor: String, layout: SimpleMavenRepositoryLayout) :
        ResourceRetrievalException("Failed to find resource because no facet was enabled! The descriptor was: '$descriptor' and the layout was '${layout.name}'.",null)

    public class MetadataParseFailed(location: String, reason: String) :
        ResourceRetrievalException("Failed to parse resource metadata in file '$location'. The reason was '$reason'.",null)

    public class SnapshotNotFound(classifier: String?, ending: String, basePath: String) :
        ResourceRetrievalException("Failed to find snapshot version number for artifact in path '$basePath'. Classifier was '${classifier ?: "<NONE>"}', file type was '$ending'.",null)
}