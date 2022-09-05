package com.durganmcbroom.artifact.resolver.simple.maven

// Should implement equals and hashcode
public fun interface ScopeControl {
    public fun shouldInclude(scope: String): Boolean
}

// Should implement equals and hashcode
public fun interface ArtifactControl {
    public fun shouldInclude(artifact: String): Boolean
}

public class ScopeInclusionControl(
    public val scopes : Set<String>
) : ScopeControl {

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

public class ScopeExclusionControl(
    public val scopes : Set<String>
) : ScopeControl {

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

public class ArtifactInclusionControl(
    public val artifacts : Set<String>
) : ArtifactControl {
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

public class ArtifactExclusionControl(
    public val artifacts : Set<String>
) : ArtifactControl {
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