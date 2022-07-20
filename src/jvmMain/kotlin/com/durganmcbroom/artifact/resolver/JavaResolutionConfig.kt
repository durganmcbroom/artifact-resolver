@file:JvmName("JavaResolutionConfig")

package com.durganmcbroom.artifact.resolver

import java.util.function.Consumer

public fun <T: ArtifactGraphConfig<*,*>> config(provider: ArtifactGraphProvider<T, *>, consumer: Consumer<T>): T = provider.emptyConfig().also(consumer::accept)
