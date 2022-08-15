package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.Lockable
import com.durganmcbroom.artifact.resolver.ArtifactGraphProvider

public object SimpleMaven : ArtifactGraphProvider<SimpleMavenArtifactGraphConfig, SimpleMavenArtifactGraph> {
    override val key: ArtifactGraphProvider.Key = MockMavenKey

    override fun emptyConfig(): SimpleMavenArtifactGraphConfig = SimpleMavenArtifactGraphConfig()

    override fun provide(config: SimpleMavenArtifactGraphConfig): SimpleMavenArtifactGraph =
        SimpleMavenArtifactGraph(config.also(Lockable::lock), this)

    private object MockMavenKey : ArtifactGraphProvider.Key()
}