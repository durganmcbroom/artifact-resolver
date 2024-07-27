package com.durganmcbroom.artifact.resolver

import com.durganmcbroom.resources.Resource


public open class ArtifactMetadata<D: ArtifactMetadata.Descriptor, out C: ArtifactMetadata.ChildInfo<ArtifactRequest<D>, *>>(
    public open val descriptor: D,
//    public open val resource: Resource?,
    public open val children: List<C>
) {
    public interface Descriptor {
        public val name: String
    }

    public open class ChildInfo<out R: ArtifactRequest<*>, out S: RepositorySettings>(
        public open val request: R,
        public open val candidates: List<S>
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ChildInfo<*, *>) return false

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
