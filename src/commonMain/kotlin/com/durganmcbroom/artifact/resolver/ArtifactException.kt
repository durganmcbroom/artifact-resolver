package com.durganmcbroom.artifact.resolver

public abstract class ArtifactException(public override val message: String) : Exception() {
    public class ArtifactNotFound(
        public val desc: ArtifactMetadata.Descriptor,
        public val searchedIn: List<String>,
        override val cause: Throwable? = null
    ) : ArtifactException(
        """Failed to find the artifact: '$desc'. Looked in places: 
            |${searchedIn.joinToString(separator = "\n") { " - $it" }}
        """.trimMargin()
    ) {
        override fun toString(): String {
            return "ArtifactNotFound(desc='$desc')"
        }
    }


}