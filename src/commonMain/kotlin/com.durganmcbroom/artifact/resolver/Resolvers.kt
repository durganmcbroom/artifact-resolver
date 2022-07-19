@file:JvmName("Resolvers")

package com.durganmcbroom.artifact.resolver

import kotlin.jvm.JvmName
import kotlin.jvm.JvmOverloads

public inline fun <C : ArtifactResolutionConfig<*, *>, R : ArtifactResolver<C, *, *>> ArtifactResolver(
    provider: ResolutionProvider<C, R>,
    config: C.() -> Unit = {}
): R = provider.provide(provider.emptyConfig().apply(config))

@JvmOverloads
public fun <C : ArtifactResolutionConfig<*, *>, R : ArtifactResolver<C, *, *>> newResolver(
    provider: ResolutionProvider<C, R>,
    config: C = provider.emptyConfig()
): R = provider.provide(config)
