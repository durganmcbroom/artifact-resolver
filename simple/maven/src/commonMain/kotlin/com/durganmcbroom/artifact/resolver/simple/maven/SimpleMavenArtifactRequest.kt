package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.ArtifactRequest
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenArtifactRequest.ArtifactControl
import com.durganmcbroom.artifact.resolver.simple.maven.SimpleMavenArtifactRequest.ScopeControl

public open class SimpleMavenArtifactRequest @JvmOverloads constructor(
    override val descriptor: SimpleMavenDescriptor,

    public val isTransitive: Boolean = true,
    public val processAsync: Boolean = true,
    public val scopes: ScopeControl = ScopeControl { true },
    public val artifacts: ArtifactControl = ArtifactControl { true }
) : ArtifactRequest {
    @JvmOverloads
    public constructor(
        descriptor: String,
        isTransitive: Boolean = true,
        processAsync: Boolean = true,
        scopes: ScopeControl = ScopeControl { true },
        artifacts: ArtifactControl = ArtifactControl { true }
    ) : this(SimpleMavenDescriptor.parseDescription(descriptor)!!, isTransitive, processAsync, scopes, artifacts)

    public open fun withNewDescriptor(
        descriptor: SimpleMavenDescriptor
    ): SimpleMavenArtifactRequest = SimpleMavenArtifactRequest(
        descriptor,
        isTransitive,
        processAsync,
        scopes,
        artifacts
    )

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is SimpleMavenArtifactRequest) return false

        if (descriptor != other.descriptor) return false
        if (isTransitive != other.isTransitive) return false
        if (processAsync != other.processAsync) return false

        return true
    }

    override fun hashCode(): Int {
        var result = descriptor.hashCode()
        result = 31 * result + isTransitive.hashCode()
        result = 31 * result + processAsync.hashCode()
        return result
    }

    public fun interface ScopeControl {
        public fun shouldInclude(scope: String): Boolean
    }

    public fun interface ArtifactControl {
        public fun shouldInclude(artifact: String): Boolean
    }


}
