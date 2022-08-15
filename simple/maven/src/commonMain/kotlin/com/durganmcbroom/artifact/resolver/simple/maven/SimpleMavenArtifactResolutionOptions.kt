package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.ArtifactResolutionOptions

public open class SimpleMavenArtifactResolutionOptions(
    isTransitive: Boolean = true,
    processAsync: Boolean = true,
    private val _excludes: MutableSet<String> = HashSet(),
    private val _includeScopes: MutableSet<String> = HashSet()
) : ArtifactResolutionOptions() {
    public var isTransitive: Boolean by locking(isTransitive)
    public val excludes: Set<String>
        get() = _excludes.toSet()
    public val includeScopes: Set<String>
        get() = _includeScopes.toSet()
    public var processAsync: Boolean by locking(processAsync)

    public fun exclude(vararg artifacts: String) {
        _excludes.addAll(artifacts)
    }

    public fun includeScopes(vararg scopes: String) {
        _includeScopes.addAll(scopes)
    }
}
