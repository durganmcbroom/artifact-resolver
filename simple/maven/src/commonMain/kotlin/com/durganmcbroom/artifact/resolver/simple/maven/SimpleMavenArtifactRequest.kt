package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.ArtifactRequest
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenArtifactRequest.ArtifactControl
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenArtifactRequest.ScopeControl

public open class SimpleMavenArtifactRequest @JvmOverloads constructor(
    override val descriptor: SimpleMavenDescriptor,

    public val isTransitive: Boolean = true,
    public val scopes: ScopeControl = ScopeControl { true },
    public val artifacts: ArtifactControl = ArtifactControl { true }
) : ArtifactRequest {
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


    // Should implement equals and hashcode
    public fun interface ScopeControl {
        public fun shouldInclude(scope: String): Boolean
    }

    // Should implement equals and hashcode
    public fun interface ArtifactControl {
        public fun shouldInclude(artifact: String): Boolean
    }

    public companion object {
        public fun includeScopes(vararg _scopes: String) : ScopeControl {
            class ScopeInclusionControl : ScopeControl {
                private val scopes : Set<String> = _scopes.toSet()

                override fun shouldInclude(scope: String): Boolean = scopes.contains(scope)

                override fun equals(other: Any?): Boolean {
                    if (this === other) return true
                    if (other !is ScopeInclusionControl) return false

                    if (this.scopes != other.scopes) return false

                    return true
                }

                override fun hashCode(): Int {
                    return this.scopes.hashCode()
                }
            }

            return ScopeInclusionControl()
        }

        public fun excludeScopes(vararg _scopes: String) : ScopeControl {
            class ScopeExclusionControl : ScopeControl {
                private val scopes : Set<String> = _scopes.toSet()

                override fun shouldInclude(scope: String): Boolean = !scopes.contains(scope)

                override fun equals(other: Any?): Boolean {
                    if (this === other) return true
                    if (other !is ScopeExclusionControl) return false

                    if (this.scopes != other.scopes) return false

                    return true
                }

                override fun hashCode(): Int {
                    return this.scopes.hashCode()
                }
            }

            return ScopeExclusionControl()
        }

        public fun includeArtifacts(vararg _artifacts: String) : ArtifactControl {
            class ArtifactInclusionControl : ArtifactControl {
                private val artifacts : Set<String> = _artifacts.toSet()

                override fun shouldInclude(artifact: String): Boolean = artifacts.contains(artifact)

                override fun equals(other: Any?): Boolean {
                    if (this === other) return true
                    if (other !is ArtifactInclusionControl) return false

                    if (artifacts != other.artifacts) return false

                    return true
                }

                override fun hashCode(): Int {
                    return artifacts.hashCode()
                }
            }

            return ArtifactInclusionControl()
        }

        public fun excludeArtifacts(vararg _artifacts: String) : ArtifactControl {
            class ArtifactExclusionControl : ArtifactControl {
                private val artifacts : Set<String> = _artifacts.toSet()

                override fun shouldInclude(artifact: String): Boolean = !artifacts.contains(artifact)

                override fun equals(other: Any?): Boolean {
                    if (this === other) return true
                    if (other !is ArtifactExclusionControl) return false

                    if (artifacts != other.artifacts) return false

                    return true
                }

                override fun hashCode(): Int {
                    return artifacts.hashCode()
                }
            }

            return ArtifactExclusionControl()
        }
    }
}
