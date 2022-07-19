package com.durganmcbroom.artifact.resolver.mock.maven.plugin

import com.durganmcbroom.artifact.resolver.mock.maven.pom.PomData

public data class MockPluginConfiguration(
    public val values: Map<String, Any>,
    public val pom: PomData
)