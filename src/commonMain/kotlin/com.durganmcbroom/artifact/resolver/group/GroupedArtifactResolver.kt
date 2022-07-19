package com.durganmcbroom.artifact.resolver.group

import com.durganmcbroom.artifact.resolver.ArtifactResolutionConfig
import com.durganmcbroom.artifact.resolver.ArtifactResolver
import com.durganmcbroom.artifact.resolver.ResolutionProvider

public class GroupedArtifactResolver(config: ResolutionGroupConfig) :
    ArtifactResolver<ResolutionGroupConfig, Nothing, Nothing>(
        config, ResolutionGroup
    ) {
    override fun newSettings(): Nothing =
        throw UnsupportedOperationException("Grouped Artifact resolver has no associated settings!")

    override fun processorFor(settings: Nothing): Nothing =
        throw UnsupportedOperationException("Grouped Artifact resolver has no associated processor!")

    public operator fun <T : ArtifactResolver<*, *, *>> get(provider: ResolutionProvider<*, T>): T? =
        config.refs[provider.key]?.let {
            @Suppress("UNCHECKED_CAST")
            (provider as ResolutionProvider<ArtifactResolutionConfig<*, *>, T>).provide(it.config)
        }


}