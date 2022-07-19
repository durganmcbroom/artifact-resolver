package com.durganmcbroom.artifact.resolver

public interface ArtifactRepository<in D : ArtifactMeta.Descriptor, O : ArtifactResolutionOptions> {
    public fun artifactOf(desc: D, options: O, trace: ArtifactTrace?): Artifact?

    public data class ArtifactTrace(
        public val parent: ArtifactTrace?,
        public val descriptor: ArtifactMeta.Descriptor
    ) {
        public val flattened: List<ArtifactTrace> by lazy { (parent?.flattened ?: listOf()) + this }

        public fun isCyclic(desc: ArtifactMeta.Descriptor): Boolean =
            parent?.descriptor == desc || parent?.isCyclic(desc) == true

        override fun toString(): String =
            flattened.joinToString(separator = " -> ", prefix = "[", postfix = "]") { "'${it.descriptor}'" }
    }
}