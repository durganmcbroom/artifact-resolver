package com.durganmcbroom.artifact.resolver.group

import com.durganmcbroom.artifact.resolver.ResolutionProvider

public object ResolutionGroup : ResolutionProvider<ResolutionGroupConfig, GroupedArtifactResolver> {
    override val key: ResolutionProvider.Key = GroupKey

    override fun emptyConfig(): ResolutionGroupConfig = ResolutionGroupConfig()

    override fun provide(config: ResolutionGroupConfig): GroupedArtifactResolver = GroupedArtifactResolver(config)

    private object GroupKey : ResolutionProvider.Key()
}