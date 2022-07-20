@file:JvmName("Transformers")

package com.durganmcbroom.artifact.resolver.group

import com.durganmcbroom.artifact.resolver.ArtifactMetadata
import com.durganmcbroom.artifact.resolver.ArtifactGraphConfig
import com.durganmcbroom.artifact.resolver.ArtifactResolutionOptions
import kotlin.jvm.JvmName
import kotlin.reflect.KClass

public interface Transformer<I : Any, O : Any> {
    public val typeIn: KClass<I>
    public val typeOut: KClass<O>

    public fun transform(t: I): O
}

public interface DescriptionTransformer<I : ArtifactMetadata.Descriptor, O : ArtifactMetadata.Descriptor> : Transformer<I, O>

public interface ResolutionOptionsTransformer<I : ArtifactResolutionOptions, O : ArtifactResolutionOptions> :
    Transformer<I, O>

public inline fun <O : ArtifactMetadata.Descriptor, I : ArtifactMetadata.Descriptor, R : ArtifactResolutionOptions, C : ArtifactGraphConfig<O, R>> ResolutionGroupConfig.ResolutionBuilder<O, R, C>.addDescriptionTransformer(
    i: KClass<I>,
    o: KClass<O>,
    crossinline transformer: (I) -> O
): ResolutionGroupConfig.ResolutionBuilder<O, R, C> = addDescriptionTransformer(object : DescriptionTransformer<I, O> {
    override val typeIn: KClass<I> = i
    override val typeOut: KClass<O> = o

    override fun transform(t: I): O = transformer(t)
})

public inline fun <O : ArtifactResolutionOptions, I : ArtifactResolutionOptions, D : ArtifactMetadata.Descriptor, C : ArtifactGraphConfig<D, O>> ResolutionGroupConfig.ResolutionBuilder<D, O, C>.addResolutionOptionsTransformer(
    i: KClass<I>,
    o: KClass<O>,
    crossinline transformer: (I) -> O
): ResolutionGroupConfig.ResolutionBuilder<D, O, C> =
    addResolutionOptionsTransformer(object : ResolutionOptionsTransformer<I, O> {
        override val typeIn: KClass<I> = i
        override val typeOut: KClass<O> = o

        override fun transform(t: I): O = transformer(t)
    })

