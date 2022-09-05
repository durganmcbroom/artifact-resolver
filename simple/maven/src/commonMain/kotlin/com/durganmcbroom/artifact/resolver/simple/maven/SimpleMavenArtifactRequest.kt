package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.ArtifactRequest

public open class SimpleMavenArtifactRequest @JvmOverloads constructor(
    override val descriptor: SimpleMavenDescriptor,

    public val isTransitive: Boolean = true,
    public val scopes: ScopeControl = ScopeControl { true },
    public val artifacts: ArtifactControl = ArtifactControl { true }
) : ArtifactRequest<SimpleMavenDescriptor> {
    @JvmOverloads
    public constructor(
        descriptor: String,
        isTransitive: Boolean = true,
        scopes: ScopeControl = ScopeControl { true },
        artifacts: ArtifactControl = ArtifactControl { true }
    ) : this(SimpleMavenDescriptor.parseDescription(descriptor)!!, isTransitive, scopes, artifacts)

    public open fun withNewDescriptor(
        descriptor: SimpleMavenDescriptor
    ): SimpleMavenArtifactRequest = SimpleMavenArtifactRequest(
        descriptor,
        isTransitive,
        scopes,
        artifacts
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SimpleMavenArtifactRequest) return false

        if (descriptor != other.descriptor) return false
        if (isTransitive != other.isTransitive) return false
        if (scopes != other.scopes) return false
        if (artifacts != other.artifacts) return false

        return true
    }

    override fun hashCode(): Int {
        var result = descriptor.hashCode()
        result = 31 * result + isTransitive.hashCode()
        result = 31 * result + scopes.hashCode()
        result = 31 * result + artifacts.hashCode()
        return result
    }



    public companion object {
        public fun includeScopes(vararg scopes: String) : ScopeControl {
            return ScopeInclusionControl(scopes.toSet())
        }

        public fun excludeScopes(vararg scopes: String) : ScopeControl {
            return ScopeExclusionControl(scopes.toSet())
        }

        public fun includeArtifacts(vararg artifacts: String) : ArtifactControl {
            return ArtifactInclusionControl(artifacts.toSet())
        }

        public fun excludeArtifacts(vararg artifacts: String) : ArtifactControl {
            return ArtifactExclusionControl(artifacts.toSet())
        }
    }
}
