package com.durganmcbroom.artifact.resolver.simple.maven

import com.durganmcbroom.artifact.resolver.ArtifactGraphConfig
import com.durganmcbroom.artifact.resolver.RepositoryDeReferencer

public open class SimpleMavenArtifactGraphConfig : ArtifactGraphConfig<SimpleMavenDescriptor, SimpleMavenArtifactResolutionOptions>() {
    init {
        deReferencer = RepositoryDeReferencer { ref ->
            val resolver = if (ref.provider is SimpleMaven)
                (ref.provider as SimpleMaven).provide(this)
            else return@RepositoryDeReferencer null

            check(ref.settings is SimpleMavenRepositorySettings) { "Invalid Repository settings." }
            resolver.resolverFor(ref.settings as SimpleMavenRepositorySettings)
        }
    }
}