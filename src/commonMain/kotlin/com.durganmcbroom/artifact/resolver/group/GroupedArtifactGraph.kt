package com.durganmcbroom.artifact.resolver.group

import com.durganmcbroom.artifact.resolver.ArtifactGraphConfig
import com.durganmcbroom.artifact.resolver.ArtifactGraph
import com.durganmcbroom.artifact.resolver.ArtifactGraphProvider

public class GroupedArtifactGraph(config: ResolutionGroupConfig) :
    ArtifactGraph<ResolutionGroupConfig, Nothing, Nothing>(
        config, ResolutionGroup
    ) {
    override fun newRepoSettings(): Nothing =
        throw UnsupportedOperationException("Grouped Artifact resolver has no associated settings!")

    override fun resolverFor(settings: Nothing): Nothing =
        throw UnsupportedOperationException("Grouped Artifact resolver has no associated processor!")

    public operator fun <T : ArtifactGraph<*, *, *>> get(provider: ArtifactGraphProvider<*, T>): T? =
        config.refs[provider.key]?.let {
            @Suppress("UNCHECKED_CAST")
            (provider as ArtifactGraphProvider<ArtifactGraphConfig<*, *>, T>).provide(it.config)
        }
}