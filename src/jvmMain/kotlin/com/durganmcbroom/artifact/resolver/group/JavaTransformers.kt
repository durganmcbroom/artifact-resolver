@file:JvmName("JavaTransformers")

package com.durganmcbroom.artifact.resolver.group

import com.durganmcbroom.artifact.resolver.ArtifactMeta
import com.durganmcbroom.artifact.resolver.ArtifactResolutionOptions
import kotlin.reflect.KClass

public fun <O : ArtifactMeta.Descriptor, I : ArtifactMeta.Descriptor> descTransformer(
    i: Class<I>,
    o: Class<O>,
    transformer: java.util.function.Function<I, O>
): DescriptionTransformer<I, O> = object : DescriptionTransformer<I, O> {
    override val typeIn: KClass<I> = i.kotlin
    override val typeOut: KClass<O> = o.kotlin

    override fun transform(t: I): O = transformer.apply(t)
}

public fun <O : ArtifactResolutionOptions, I : ArtifactResolutionOptions> resolutionOptionsTransformer(
    i: Class<I>,
    o: Class<O>,
    transformer: java.util.function.Function<I, O>
): ResolutionOptionsTransformer<I, O> = object : ResolutionOptionsTransformer<I, O> {
    override val typeIn: KClass<I> = i.kotlin
    override val typeOut: KClass<O> = o.kotlin

    override fun transform(t: I): O = transformer.apply(t)
}


