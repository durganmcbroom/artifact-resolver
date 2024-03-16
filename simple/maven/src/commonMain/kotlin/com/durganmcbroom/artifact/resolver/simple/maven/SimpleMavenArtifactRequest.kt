package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.ArtifactRequest

public open class SimpleMavenArtifactRequest @JvmOverloads constructor(
    override val descriptor: SimpleMavenDescriptor,

    public val isTransitive: Boolean = true,
    public val includeScopes: Set<String> = setOf("compile", "runtime", "import"),
    public val excludeArtifacts: Set<String> = setOf()
) : ArtifactRequest<SimpleMavenDescriptor> {
    @JvmOverloads
    public constructor(
        descriptor: String,
        isTransitive: Boolean = true,
        includeScopes: Set<String> = setOf("compile", "runtime", "import"),
        excludeArtifacts: Set<String> = setOf()
    ) : this(SimpleMavenDescriptor.parseDescription(descriptor)!!, isTransitive, includeScopes, excludeArtifacts)

    public open fun withNewDescriptor(
        descriptor: SimpleMavenDescriptor
    ): SimpleMavenArtifactRequest = SimpleMavenArtifactRequest(
        descriptor,
        isTransitive,
        includeScopes,
        excludeArtifacts
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SimpleMavenArtifactRequest) return false

        if (descriptor != other.descriptor) return false
        if (isTransitive != other.isTransitive) return false
        if (includeScopes != other.includeScopes) return false
        if (excludeArtifacts != other.excludeArtifacts) return false

        return true
    }

    override fun hashCode(): Int {
        var result = descriptor.hashCode()
        result = 31 * result + isTransitive.hashCode()
        result = 31 * result + includeScopes.hashCode()
        result = 31 * result + excludeArtifacts.hashCode()
        return result
    }

    override fun toString(): String {
        return "SimpleMavenArtifactRequest(descriptor=$descriptor, isTransitive=$isTransitive, includeScopes=$includeScopes, excludeArtifacts=$excludeArtifacts)"
    }


//    public companion object {
//        public fun includeScopes(vararg scopes: String) : ScopeControl {
//            return ScopeInclusionControl(scopes.toSet())
//        }
//
//        public fun excludeScopes(vararg scopes: String) : ScopeControl {
//            return ScopeExclusionControl(scopes.toSet())
//        }
//
//        public fun includeArtifacts(vararg artifacts: String) : ArtifactControl {
//            return ArtifactInclusionControl(artifacts.toSet())
//        }
//
//        public fun excludeArtifacts(vararg artifacts: String) : ArtifactControl {
//            return ArtifactExclusionControl(artifacts.toSet())
//        }
//    }
}
