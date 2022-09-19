package com.durganmcbroom.artifact.resolver.simple.maven.layout

import com.durganmcbroom.artifact.resolver.MetadataRequestException

public sealed class ResourceRetrievalException(message: String) : MetadataRequestException(message) {
    public class IllegalState(reason: String) : ResourceRetrievalException(reason)

    public class ChecksumFileNotFound(file: String, type: String) :
        ResourceRetrievalException("Failed to find the checksum file: '$file'. Make sure the type (was '$type') is correct and the artifact exists!")

    public class ChecksumValidationFailed(forFile: String, fromChecksum: String) :
        ResourceRetrievalException("Failed to validate checksum for file '$forFile', checksum was '$fromChecksum'.")

    public class NoEnabledFacet(descriptor: String, layout: SimpleMavenRepositoryLayout) :
        ResourceRetrievalException("Failed to find resource because no facet was enabled! The descriptor was: '$descriptor' and the layout was '${layout.name}'.")

    public class ResourceNotFound(location: String) : ResourceRetrievalException("Resource was not found. Its given location was '$location'")

    public class MetadataParseFailed(location: String, reason: String) : ResourceRetrievalException("Failed to parse resource metadata in file '$location'. The reason was '$reason'.")

    public class SnapshotNotFound(classifier: String?, ending: String, basePath: String) : ResourceRetrievalException("Failed to find snapshot version number for artifact in path '$basePath'. Classifier was '${classifier ?: "<NONE>"}', file type was '$ending'.")
}