package com.durganmcbroom.artifact.resolver

public abstract class ArtifactException(public val message: String) {
    public data class ArtifactNotFound(
        val desc: ArtifactMetadata.Descriptor,
        val searchedIn: List<String>,
    ) : ArtifactException(
        """Failed to find the artifact: '$desc'. Looked in places: 
            |${searchedIn.joinToString(separator = "\n") { " - $it" }}
        """.trimMargin()
    )
}