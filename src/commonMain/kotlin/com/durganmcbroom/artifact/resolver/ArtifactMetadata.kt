package com.durganmcbroom.artifact.resolver


public open class ArtifactMetadata<D: ArtifactMetadata.Descriptor, C: ArtifactMetadata.ChildInfo<D, *>>(
    public open val descriptor: D,
    public open val resource: CheckedResource?,
    public open val children: List<C>
) {
    public interface Descriptor {
        public val name: String
    }

    public open class ChildInfo<D: Descriptor, S: RepositoryStub>(
        public open val descriptor: D,
        public open val candidates: List<S>
    ) {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is ChildInfo<*, *>) return false

            if (descriptor != other.descriptor) return false
            if (candidates != other.candidates) return false

            return true
        }

        override fun hashCode(): Int {
            var result = descriptor.hashCode()
            result = 31 * result + candidates.hashCode()
            return result
        }
    }
}
