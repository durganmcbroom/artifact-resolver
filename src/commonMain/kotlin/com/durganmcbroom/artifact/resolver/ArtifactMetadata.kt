package com.durganmcbroom.artifact.resolver


public open class ArtifactMetadata<D: ArtifactMetadata.Descriptor, out P: ArtifactMetadata.ParentInfo<ArtifactRequest<D>, *>>(
    public open val descriptor: D,
//    public open val resource: Resource?,
    public open val parents: List<P>
) {
    public interface Descriptor {
        public val name: String
    }

    public open class ParentInfo<out R: ArtifactRequest<*>, out S: RepositorySettings>(
        public open val request: R,
        public open val candidates: List<S>
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ParentInfo<*, *>) return false

            if (request != other.request) return false
            if (candidates != other.candidates) return false

            return true
        }

        override fun hashCode(): Int {
            var result = request.hashCode()
            result = 31 * result + candidates.hashCode()
            return result
        }
    }
}
