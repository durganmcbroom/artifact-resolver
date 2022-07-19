@file:JvmName("JavaResolutionConfig")

package com.durganmcbroom.artifact.resolver

import java.util.function.Consumer

public fun <T: ArtifactResolutionConfig<*,*>> config(provider: ResolutionProvider<T, *>, consumer: Consumer<T>): T = provider.emptyConfig().also(consumer::accept)
