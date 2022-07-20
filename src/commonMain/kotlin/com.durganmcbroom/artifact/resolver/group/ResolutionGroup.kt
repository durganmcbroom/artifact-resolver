package com.durganmcbroom.artifact.resolver.group

import com.durganmcbroom.artifact.resolver.ArtifactGraphProvider

public object ResolutionGroup : ArtifactGraphProvider<ResolutionGroupConfig, GroupedArtifactGraph> {
    override val key: ArtifactGraphProvider.Key = GroupKey

    override fun emptyConfig(): ResolutionGroupConfig = ResolutionGroupConfig()

    override fun provide(config: ResolutionGroupConfig): GroupedArtifactGraph = GroupedArtifactGraph(config)

    private object GroupKey : ArtifactGraphProvider.Key()
}