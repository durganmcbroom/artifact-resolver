package com.durganmcbroom.artifact.resolver.mock.maven

import com.durganmcbroom.artifact.resolver.ArtifactResolutionConfig
import com.durganmcbroom.artifact.resolver.RepositoryDeReferencer

public class MockMavenResolutionConfig : ArtifactResolutionConfig<MavenDescriptor, MockMavenArtifactResolutionOptions>() {
    init {
        deReferencer = RepositoryDeReferencer { ref ->
            val resolver = if (ref.provider is MockMaven)
                (ref.provider as MockMaven).provide(this)
            else return@RepositoryDeReferencer null

            check(ref.settings is MockMavenRepositorySettings) { "Invalid Repository settings." }
            resolver.processorFor(ref.settings as MockMavenRepositorySettings)
        }
    }
}