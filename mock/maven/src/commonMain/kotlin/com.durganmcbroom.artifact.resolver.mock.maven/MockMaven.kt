package com.durganmcbroom.artifact.resolver.mock.maven

import com.durganmcbroom.artifact.resolver.Lockable
import com.durganmcbroom.artifact.resolver.ResolutionProvider

public object MockMaven : ResolutionProvider<MockMavenResolutionConfig, MockMavenArtifactResolver> {
    override val key: ResolutionProvider.Key = MockMavenKey

    override fun emptyConfig(): MockMavenResolutionConfig = MockMavenResolutionConfig()

    override fun provide(config: MockMavenResolutionConfig): MockMavenArtifactResolver =
        MockMavenArtifactResolver(config.also(Lockable::lock), this)

    private object MockMavenKey : ResolutionProvider.Key()
}