package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.Lockable
import com.durganmcbroom.artifact.resolver.ArtifactGraphProvider

public object SimpleMaven : ArtifactGraphProvider<SimpleMavenResolutionConfig, SimpleMavenArtifactGraph> {
    override val key: ArtifactGraphProvider.Key = MockMavenKey

    override fun emptyConfig(): SimpleMavenResolutionConfig = SimpleMavenResolutionConfig()

    override fun provide(config: SimpleMavenResolutionConfig): SimpleMavenArtifactGraph =
        SimpleMavenArtifactGraph(config.also(Lockable::lock), this)

    private object MockMavenKey : ArtifactGraphProvider.Key()
}