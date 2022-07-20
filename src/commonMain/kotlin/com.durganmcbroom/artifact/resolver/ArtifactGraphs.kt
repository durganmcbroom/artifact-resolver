@file:JvmName("ArtifactGraphs")

package com.durganmcbroom.artifact.resolver

import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads

public inline fun <C : ArtifactGraphConfig<*, *>, R : ArtifactGraph<C, *, *>> ArtifactGraph(
    provider: ArtifactGraphProvider<C, R>,
    config: C.() -> Unit = {}
): R = provider.provide(provider.emptyConfig().apply(config))

@JvmOverloads
public fun <C : ArtifactGraphConfig<*, *>, R : ArtifactGraph<C, *, *>> newGraph(
    provider: ArtifactGraphProvider<C, R>,
    config: C = provider.emptyConfig()
): R = provider.provide(config)
