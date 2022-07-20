package com.durganmcbroom.artifact.resolver.simple.maven.plugin

import com.durganmcbroom.artifact.resolver.simple.maven.pom.PomData

public data class SimplePluginConfiguration(
    public val values: Map<String, Any>,
    public val pom: PomData
)