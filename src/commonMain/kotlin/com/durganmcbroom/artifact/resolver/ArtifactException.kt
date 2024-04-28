package com.durganmcbroom.artifact.resolver

public abstract class ArtifactException(public override val message: String) : Exception() {
    public data class ArtifactNotFound(
        val desc: ArtifactMetadata.Descriptor,
        val searchedIn: List<String>,
        override val cause: Throwable? = null
    ) : ArtifactException(
        """Failed to find the artifact: '$desc'. Looked in places: 
            |${searchedIn.joinToString(separator = "\n") { " - $it" }}
        """.trimMargin()
    )
}