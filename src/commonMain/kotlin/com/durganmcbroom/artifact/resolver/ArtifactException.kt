package com.durganmcbroom.artifact.resolver

public abstract class ArtifactException(public override val message: String) : Exception() {
    public class ArtifactNotFound(
        public val desc: ArtifactMetadata.Descriptor,
        public val searchedIn: List<RepositorySettings>,
        public val trace: List<ArtifactMetadata.Descriptor>,
        override val cause: Throwable? = null
    ) : ArtifactException(
        """Failed to find the artifact: '$desc'. Looked in places: 
            |${searchedIn.joinToString(separator = "\n") { " - $it" }}
            |Trace was: ${trace.takeUnless { it.isEmpty() }?.joinToString(separator = " -> ") { it.name } ?: "<empty>"}
        """.trimMargin()
    ) {
        override fun toString(): String {
            return message
        }
    }


}